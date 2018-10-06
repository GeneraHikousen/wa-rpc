package org.wa.remoting.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wa.common.exception.remoting.RemotingSendRequestException;
import org.wa.common.exception.remoting.RemotingTimeoutException;
import org.wa.common.utils.NamedThreadFactory;
import org.wa.common.utils.NativeSupport;
import org.wa.common.utils.Pair;
import org.wa.remoting.NettyRemotingBase;
import org.wa.remoting.NettyServerConfig;
import org.wa.remoting.RPCHook;
import org.wa.remoting.model.NettyChannelInactiveProcessor;
import org.wa.remoting.model.NettyRequestProcessor;
import org.wa.remoting.model.RemotingTransporter;
import org.wa.remoting.netty.decode.RemotingTransporterDecoder;
import org.wa.remoting.netty.encode.RemotingTransporterEncoder;
import org.wa.remoting.netty.idle.AcceptorIdleStateTrigger;
import org.wa.remoting.netty.idle.IdleStateChecker;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static org.wa.common.utils.Constants.AVAILABLE_PROCESSORS;
import static org.wa.common.utils.Constants.READER_IDLE_TIME_SECONDS;

/**
 * @Auther: XF
 * @Date: 2018/10/5 17:02
 * @Description:
 */
public class NettyRemotingServer extends NettyRemotingBase implements RemotingServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyRemotingServer.class);

    private ServerBootstrap serverBootstrap;

    private EventLoopGroup boss;

    private EventLoopGroup worker;

    private int workerNum;
    private int writeBufferLowWaterMark;
    private int writeBufferHighWaterMark;

    protected HashedWheelTimer timer = new HashedWheelTimer(new NamedThreadFactory("netty.acceptor.timer"));

    protected volatile ByteBufAllocator allocator;

    private final NettyServerConfig nettyServerConfig;

    private DefaultEventExecutorGroup defaultEventExecutorGroup;

    private final ExecutorService publicExecutor;

    private final AcceptorIdleStateTrigger idleStateTrigger = new AcceptorIdleStateTrigger();

    private RPCHook rpcHook;

    public NettyRemotingServer() {
        this(new NettyServerConfig());
    }

    public NettyRemotingServer(NettyServerConfig nettyServerConfig) {
        this.nettyServerConfig = nettyServerConfig;
        if (null != nettyServerConfig) {
            workerNum = nettyServerConfig.getServerWorkerThreads();
            writeBufferLowWaterMark = nettyServerConfig.getWriteBufferLowWaterMark();
            writeBufferHighWaterMark = nettyServerConfig.getWriteBufferHighWaterMark();
        }
        this.publicExecutor = Executors.newFixedThreadPool(4, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "NettyServerPublicExecutor_" + this.threadIndex.incrementAndGet());
            }
        });
        init();
    }

    private EventLoopGroup initEventLoopGroup(int nums, ThreadFactory threadFactory) {
        return isNativeEt() ? new EpollEventLoopGroup(nums, threadFactory) : new NioEventLoopGroup(nums, threadFactory);
    }

    private boolean isNativeEt() {
        return NativeSupport.isSupportNativeET();
    }


    @Override
    protected RPCHook getRPCHook() {
        return null;
    }

    @Override
    public void registerProcessor(byte requestCode, NettyRequestProcessor processor, ExecutorService executor) {
        ExecutorService executorThis = executor;
        if(executorThis==null){
            executorThis = this.publicExecutor;
        }
        Pair<NettyRequestProcessor,ExecutorService> pair = new Pair<>(processor,executorThis);
        this.processorTable.put(requestCode,pair);
    }

    @Override
    public void registerChannelInactiveProcessor(NettyChannelInactiveProcessor processor, ExecutorService executor) {
        ExecutorService executorThis = executor;
        if(executorThis==null){
            executorThis = this.publicExecutor;
        }
        Pair<NettyChannelInactiveProcessor,ExecutorService> pair = new Pair<>(processor,executor);
        this.defaultChannelInactiveProcessor = pair;
    }

    @Override
    public void registerDefaultProcessor(NettyRequestProcessor processor, ExecutorService executor) {
        this.defaultRequestProcessor = new Pair<NettyRequestProcessor, ExecutorService>(processor, executor);
    }

    @Override
    public Pair<NettyRequestProcessor, ExecutorService> getProcessorPair(int requestCode) {
        return processorTable.get(requestCode);
    }

    @Override
    public RemotingTransporter invokeSync(Channel channel, RemotingTransporter request, long timeoutMillis) throws InterruptedException, RemotingSendRequestException, RemotingTimeoutException {
        return super.invokeSyncImpl(channel,request,timeoutMillis);
    }

    @Override
    public void init() {
        ThreadFactory bossFactory = new DefaultThreadFactory("netty.boss");
        ThreadFactory workerFactory = new DefaultThreadFactory("netty.worker");

        boss = initEventLoopGroup(1, bossFactory);

        if (workerNum <= 0) {
            workerNum = Runtime.getRuntime().availableProcessors() << 1;
        }
        worker = initEventLoopGroup(workerNum, workerFactory);

        serverBootstrap = new ServerBootstrap().group(boss, worker);

        allocator = new PooledByteBufAllocator(PlatformDependent.directBufferPreferred());

        serverBootstrap.childOption(ChannelOption.ALLOCATOR, allocator)
                .childOption(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);

        if (boss instanceof EpollEventLoopGroup) {
            ((EpollEventLoopGroup) boss).setIoRatio(100);
        } else if (boss instanceof NioEventLoopGroup) {
            ((NioEventLoopGroup) boss).setIoRatio(100);
        }
        if (worker instanceof EpollEventLoopGroup) {
            ((EpollEventLoopGroup) worker).setIoRatio(100);
        } else if (worker instanceof NioEventLoopGroup) {
            ((NioEventLoopGroup) worker).setIoRatio(100);
        }

        serverBootstrap.option(ChannelOption.SO_BACKLOG, 32768);
        serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);

        // child options
        serverBootstrap.childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, false);


        if (writeBufferLowWaterMark >= 0 && writeBufferHighWaterMark > 0) {
            WriteBufferWaterMark waterMark = new WriteBufferWaterMark(writeBufferLowWaterMark, writeBufferHighWaterMark);
            serverBootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, waterMark);
        }

    }

    @Override
    public void start() {
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(AVAILABLE_PROCESSORS, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r,"NettyServerWorkerThread_"+this.threadIndex.incrementAndGet());
            }
        });
        if(isNativeEt()){
            serverBootstrap.channel(EpollServerSocketChannel.class);
        }else{
            serverBootstrap.channel(NioServerSocketChannel.class);
        }

        serverBootstrap.localAddress(new InetSocketAddress(this.nettyServerConfig.getListenPort()))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                defaultEventExecutorGroup,
                                new IdleStateChecker(timer,READER_IDLE_TIME_SECONDS, 0, 0),
                                idleStateTrigger,
                                new RemotingTransporterEncoder(),
                                new RemotingTransporterDecoder(),
                                new NettyServerHandler()
                        );
                    }
                });

        try{
            logger.info("netty bind serverBootstrap start...",this.nettyServerConfig.getListenPort());
            //阻塞等待future完成
            serverBootstrap.bind().sync();
            logger.info("netty start success at port [{}]",this.nettyServerConfig.getListenPort());
        } catch (InterruptedException e) {
            logger.error("start serverBootstrap exception [{}]",e.getMessage());
            throw new RuntimeException("this.serverBootstrap.bind().sync() InterruptedException", e);
        }
    }

    @Override
    public void shutdown() {
        try {
            if(this.timer!=null){
                timer.stop();
            }
            this.boss.shutdownGracefully();
            this.worker.shutdownGracefully();

            if(this.defaultEventExecutorGroup!=null){
                defaultEventExecutorGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            logger.error("NettyRemotingServer shutdown exception,{}",e.getMessage());
        }finally {
            try {
                if(this.publicExecutor!=null){
                    this.publicExecutor.shutdown();
                }
            } catch (Exception e) {
                logger.error("NettyRemotingServer shutdown exception,{}",e.getMessage());
            }
        }
    }

    @Override
    public void registerRPCHook(RPCHook rpcHook) {
        this.rpcHook = rpcHook;
    }

    class NettyServerHandler extends SimpleChannelInboundHandler<RemotingTransporter>{
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RemotingTransporter msg) throws Exception {
            processMessageReceived(ctx,msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            processChannelInactive(ctx);
        }
    }
}
