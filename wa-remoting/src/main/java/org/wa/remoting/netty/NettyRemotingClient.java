package org.wa.remoting.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wa.common.exception.remoting.RemotingException;
import org.wa.common.exception.remoting.RemotingSendRequestException;
import org.wa.common.exception.remoting.RemotingTimeoutException;
import org.wa.common.utils.NamedThreadFactory;
import org.wa.common.utils.NativeSupport;
import org.wa.common.utils.Pair;
import org.wa.remoting.ConnectionUtils;
import org.wa.remoting.NettyRemotingBase;
import org.wa.remoting.RPCHook;
import org.wa.remoting.model.NettyChannelInactiveProcessor;
import org.wa.remoting.model.NettyRequestProcessor;
import org.wa.remoting.model.RemotingTransporter;
import org.wa.remoting.netty.decode.RemotingTransporterDecoder;
import org.wa.remoting.netty.encode.RemotingTransporterEncoder;
import org.wa.remoting.netty.idle.ConnectorIdleStateTrigger;
import org.wa.remoting.netty.idle.IdleStateChecker;
import org.wa.remoting.watcher.ConnectionWatchdog;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.wa.common.utils.Constants.WRITER_IDLE_TIME_SECONDS;

/**
 * @Auther: XF
 * @Date: 2018/10/4 20:36
 * @Description:
 */
public class NettyRemotingClient extends NettyRemotingBase implements RemotingClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyRemotingClient.class);
    private Bootstrap bootstrap;

    private EventLoopGroup worker;
    private int nWorkers;

    //负责分配缓冲区
    protected volatile ByteBufAllocator allocator;

    private final Lock lockChannelTables = new ReentrantLock();

    private static final long lockTimeoutMillis = 3000L;

    private DefaultEventExecutorGroup defaultEventExecutorGroup;

    private final NettyClientConfig nettyClientConfig;

    private volatile int writeBufferHighWaterMark = -1; //调优参数
    private volatile int writeBufferLowWaterMark = -1;  //调优参数

    private final ConnectorIdleStateTrigger idleStateTrigger = new ConnectorIdleStateTrigger();

    protected HashedWheelTimer timer = new HashedWheelTimer(new NamedThreadFactory("Netty.timer"));

    private RPCHook rpcHook;

    private final ConcurrentHashMap<String /* addr */, ChannelWrapper> channelTables = new ConcurrentHashMap<>();

    private boolean isReconnect = true;

    public NettyRemotingClient(NettyClientConfig nettyClientConfig) {
        this.nettyClientConfig = new NettyClientConfig();
        if (nettyClientConfig != null) {
            nWorkers = nettyClientConfig.getClientWorkerThreads();
            writeBufferLowWaterMark = nettyClientConfig.getWriteBufferLowWaterMark();
            writeBufferHighWaterMark = nettyClientConfig.getWriteBufferHighWaterMark();
        }
        init();
    }

    @Override
    protected RPCHook getRPCHook() {
        return rpcHook;
    }

    @Override
    public RemotingTransporter invokeSync(String addr, RemotingTransporter request, long timeoutMillis) throws InterruptedException, RemotingException {
        final Channel channel = this.getAndCreateChannel(addr);
        if (channel != null && channel.isActive()) {
            try {
                if (this.rpcHook != null) {
                    this.rpcHook.doBeforeRequest(addr, request);
                }
                RemotingTransporter respond = this.invokeSyncImpl(channel, request, timeoutMillis);
                if (this.rpcHook != null) {
                    this.rpcHook.doAfterResponse(addr, request, respond);
                }
                return respond;
            } catch (RemotingSendRequestException e) {
                logger.warn("invokeSync: send request exception, so close the channel[{}]", addr);
                this.closeChannel(addr, channel);
                throw e;
            } catch (RemotingTimeoutException e) {
                logger.warn("invokeSync: wait response timeout exception, the channel[{}]", addr);
                throw e;
            }
        } else {
            //如果channel处于不健康状态，之前是好的，现在要用不行了
            this.closeChannel(addr, channel);
            throw new RemotingException(addr + " connection exception");
        }
    }

    //channel的编织包装类
    class ChannelWrapper {
        private ChannelFuture channelFuture;

        public ChannelWrapper(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }

        public boolean isOK() {
            return this.channelFuture != null && this.channelFuture.channel().isActive();
        }

        public boolean isWritable() {
            return this.channelFuture.channel().isWritable();
        }

        private Channel getChannel() {
            return this.channelFuture.channel();
        }

        public ChannelFuture getChannelFuture() {
            return channelFuture;
        }
    }

    private Channel getAndCreateChannel(final String addr) throws InterruptedException {
        if (null == addr) {
            logger.warn("address is null");
            return null;
        }

        ChannelWrapper channelWrapper = this.channelTables.get(addr);

        if (channelWrapper != null && channelWrapper.isOK()) {
            return channelWrapper.getChannel();
        }

        return this.createChannel(addr);
    }

    private Channel createChannel(String addr) throws InterruptedException {
        ChannelWrapper channelWrapper = this.channelTables.get(addr);
        if (channelWrapper != null && channelWrapper.isOK()) {
            return channelWrapper.getChannel();
        }

        //缓存中没有，锁住channelTables，防止其他线程重复初始化channel
        if (this.lockChannelTables.tryLock(lockTimeoutMillis, TimeUnit.MILLISECONDS)) {
            try {
                boolean createNewConnection = false;
                channelWrapper = this.channelTables.get(addr);
                if (channelWrapper != null) {
                    if (channelWrapper.isOK()) {
                        return channelWrapper.getChannel();
                    } else if (!channelWrapper.getChannelFuture().isDone()) {
                        createNewConnection = false;
                    } else {
                        //缓存中channel的状态不正确，将此channel移除，标志需要重新创建
                        this.channelTables.remove(addr);
                        createNewConnection = true;
                    }
                } else {
                    createNewConnection = true;
                }

                if (createNewConnection) {
                    ChannelFuture channelFuture = this.bootstrap.connect(ConnectionUtils.string2SocketAddress(addr));
                    logger.info("create channel: begin to connect remote host [{}] asynchronously", addr);
                    channelWrapper = new ChannelWrapper(channelFuture);
                    this.channelTables.put(addr, channelWrapper);
                }
            } catch (Exception e) {
                logger.error("createChannel: create channel exception", e);
            } finally {
                this.lockChannelTables.unlock();
            }
        } else {
            //锁失败
            logger.warn("createChannel: try to lock channel table, but timeout, {}ms", lockTimeoutMillis);
        }

        if (channelWrapper != null) {
            ChannelFuture channelFuture = channelWrapper.getChannelFuture();
            if (channelFuture.awaitUninterruptibly(this.nettyClientConfig.getConnectTimeout())) {
                if (channelWrapper.isOK()) {
                    logger.info("createChannel:connect remote host[{}] success,{}", addr, channelFuture.toString());
                    return channelWrapper.getChannel();
                }
            } else {
                logger.warn("createChannel: connect remote host[{}] timeout {}ms, {}", addr, this.nettyClientConfig.getConnectTimeout(),
                        channelFuture.toString());
            }
        }
        return null;
    }

    private void closeChannel(String addr, Channel channel) {
        if (channel == null) {
            return;
        }
        final String addrRemote = (addr == null) ? ConnectionUtils.parseChannelRemoteAddr(channel) : addr;

        try {
            if (this.lockChannelTables.tryLock(lockTimeoutMillis, MILLISECONDS)) {
                try {
                    boolean removeItemFromTable = true;
                    final ChannelWrapper prevCW = this.channelTables.get(addr);
                    logger.info("begin close the channel [{}] Found:{}", addrRemote, prevCW != null);
                    if (prevCW == null) {
                        logger.info("closeChannel: the channel[{}] has been removed from the channel table before", addrRemote);
                        removeItemFromTable = false;
                    } else if (prevCW.getChannel() != channel) {
                        logger.info("closeChannel: the channel[{}] has been closed before, and has been created again, nothing to do.", addrRemote);
                        removeItemFromTable = false;
                    }

                    if (removeItemFromTable) {
                        this.channelTables.remove(addrRemote);
                        logger.info("closeChannel: the channel[{}] was removed from channel table", addrRemote);
                    }
                    ConnectionUtils.closeChannel(channel);
                } catch (Exception e) {
                    logger.error("closeChannel: close the channel exception", e);
                } finally {
                    this.lockChannelTables.unlock();
                }
            } else {
                logger.warn("closeChannel: try to lock channel table, but timeout, {}ms", lockTimeoutMillis);
            }
        } catch (InterruptedException e) {
            logger.error("closeChannel exception", e);
        }
    }


    @Override
    public void registerProcessor(byte requestCode, NettyRequestProcessor processor, ExecutorService executor) {
        ExecutorService executorThis = executor;
        if (executor == null) {
            executorThis = this.publicExecutor;
        }
        Pair<NettyRequestProcessor, ExecutorService> pair = new Pair<>(processor, executor);
        this.processorTable.put(requestCode, pair);
    }

    @Override
    public void registerChannelInactiveProcessor(NettyChannelInactiveProcessor processor, ExecutorService executor) {
        if (null == executor)
            executor = publicExecutor;
        this.defaultChannelInactiveProcessor = new Pair<NettyChannelInactiveProcessor, ExecutorService>(processor, executor);
    }

    @Override
    //addr对应的channel是否可写
    public boolean isChannelWritable(String addr) {
        ChannelWrapper channelWrapper = channelTables.get(addr);
        if (channelWrapper != null && channelWrapper.isOK()) {
            return channelWrapper.isWritable();
        }
        return true;
    }

    @Override
    public void setReconnect(boolean isReconnect) {
        this.isReconnect = isReconnect;
    }

    @Override
    public void init() {
        ThreadFactory workerFactory = new DefaultThreadFactory("netty.client");
        //epoll模型只有在linux kernel 2.6以上才能支持，在windows和mac都是不支持的
        worker = initEventLoopGroup(nWorkers, workerFactory);
        bootstrap = new Bootstrap().group(worker);

        if (worker instanceof EpollEventLoopGroup) {
            ((EpollEventLoopGroup) worker).setIoRatio(100);
        } else if (worker instanceof NioEventLoopGroup) {
            ((NioEventLoopGroup) worker).setIoRatio(100);
        }

        bootstrap.option(ChannelOption.ALLOCATOR, allocator)
                .option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) SECONDS.toMillis(3));

        bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOW_HALF_CLOSURE, false);

        if (writeBufferLowWaterMark >= 0 && writeBufferHighWaterMark > 0) {
            WriteBufferWaterMark waterMark = new WriteBufferWaterMark(writeBufferLowWaterMark, writeBufferHighWaterMark);
            bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, waterMark);
        }
    }


    private EventLoopGroup initEventLoopGroup(int nWorkers, ThreadFactory workerFactory) {
        return isNativeEt() ? new EpollEventLoopGroup(nWorkers, workerFactory) : new NioEventLoopGroup(nWorkers, workerFactory);
    }

    private boolean isNativeEt() {
        return NativeSupport.isSupportNativeET();
    }

    @Override
    public void start() {
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                nettyClientConfig.getClientWorkerThreads(),
                new ThreadFactory() {
                    private AtomicInteger threadIndex = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "NettyClientWorkerThread_" + this.threadIndex.incrementAndGet());
                    }
                }
        );
        if (isNativeEt()) {
            bootstrap.channel(EpollSocketChannel.class);
        } else {
            bootstrap.channel(NioSocketChannel.class);
        }

        final ConnectionWatchdog watchdog = new ConnectionWatchdog(bootstrap, timer) {
            @Override
            public ChannelHandler[] handlers() {
                return new ChannelHandler[]{
                        this,
                        new RemotingTransporterDecoder(),
                        new RemotingTransporterEncoder(),
                        new IdleStateChecker(timer, 0, WRITER_IDLE_TIME_SECONDS, 0),
                        idleStateTrigger,
                        new NettyClientHandler()
                };
            }
        };

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(defaultEventExecutorGroup, watchdog.handlers());
            }
        });
    }


    class NettyClientHandler extends SimpleChannelInboundHandler<RemotingTransporter> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RemotingTransporter msg) throws Exception {
            processMessageReceived(ctx, msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            processChannelInactive(ctx);
        }
    }


    @Override
    public void shutdown() {
        try{
            this.timer.stop();
            this.timer=null;
            for (ChannelWrapper cw :this.channelTables.values()){
                this.closeChannel(null,cw.getChannel());
            }

            this.channelTables.clear();

            this.worker.shutdownGracefully();

            if(this.defaultEventExecutorGroup!=null){
                defaultEventExecutorGroup.shutdownGracefully();
            }
        }catch (Exception e){
            logger.error("NettyRemotingClient shutdown exception",e);
        }

        if(this.publicExecutor!=null){
            try {
                this.publicExecutor.shutdown();
            } catch (Exception e) {
                logger.error("NettyRemotingClient shutdown exception, ", e);
            } this.publicExecutor.shutdown();
        }
    }

    @Override
    public void registerRPCHook(RPCHook rpcHook) {
        this.rpcHook = rpcHook;
    }
}
