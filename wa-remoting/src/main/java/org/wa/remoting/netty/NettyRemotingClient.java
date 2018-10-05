package org.wa.remoting.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wa.common.exception.remoting.RemotingException;
import org.wa.common.exception.remoting.RemotingSendRequestException;
import org.wa.common.exception.remoting.RemotingTimeoutException;
import org.wa.remoting.NettyRemotingBase;
import org.wa.remoting.RPCHook;
import org.wa.remoting.model.NettyChannelInactiveProcessor;
import org.wa.remoting.model.NettyRequestProcessor;
import org.wa.remoting.model.RemotingTransporter;
import org.wa.remoting.watcher.ConnectionWatchdog;

import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.SECONDS;

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

    private volatile int writeBufferHighWaterMark = -1;
    private volatile int writeBufferLowWaterMark = -1;

    private final ConnectorIdleStateTrigger idleStateTrigger = new ConnectorIdleStateTrigger();

    protected HashedWheelTimer timer = new HashedWheelTimer(new NamedThreadFactory("Netty.timer"));

    private RPCHook rpcHook;

    private final ConcurrentHashMap<String /* addr */,ChannelWrapper> channelTables = new ConcurrentHashMap<>();

    private boolean isReconnect = true;

    public NettyRemotingClient(NettyClientConif nettyClientConif){
        this.nettyClientConfig = new NettyClientConfig();
        if(nettyClientConif!=null){
            nWorkers = nettyClientConif.getClientWorkerThreads();
            writeBufferLowWaterMark  = nettyClientConif.getWriteBufferLowWaterMark();
            writeBufferHighWaterMark = nettyClientConif.writeBufferHighWaterMark();
        }
        init();
    }

    @Override
    protected RPCHook getRPCHook() {
        return null;
    }

    @Override
    public RemotingTransporter invokeSync(String addr, RemotingTransporter request, long timeoutMillis) throws RemotingTimeoutException, RemotingSendRequestException, InterruptedException, RemotingException {
        return null;
    }

    @Override
    public void registerProcessor(byte requestCode, NettyRequestProcessor processor, ExecutorService executor) {

    }

    @Override
    public void registerChannelInactiveProcessor(NettyChannelInactiveProcessor processor, ExecutorService executor) {

    }

    @Override
    public boolean isChannelWritable(String addr) {
        return false;
    }

    @Override
    public void setReconnect(boolean isReconnect) {

    }

    @Override
    public void init() {
        ThreadFactory workerFactory = new DefaultThreadFactory("netty.client");
        //epoll模型只有在linux kernel 2.6以上才能支持，在windows和mac都是不支持的
        worker = initEventLoopGroup(nWorkers,workerFactory);
        bootstrap = new Bootstrap().group(worker);
        if(worker instanceof EpollEventLoopGroup){
            ((EpollEventLoopGroup) worker).setIoRatio(100);
        }else if(worker instanceof NioEventLoopGroup){
            ((NioEventLoopGroup) worker).setIoRatio(100);
        }

        bootstrap.option(ChannelOption.ALLOCATOR,allocator)
                .option(ChannelOption.MESSAGE_SIZE_ESTIMATOR,DefaultMessageSizeEstimator.DEFAULT)
                .option(ChannelOption.SO_REUSEADDR,true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,(int)SECONDS.toMillis(3));

        bootstrap.option(ChannelOption.SO_KEEPALIVE,true)
                .option(ChannelOption.TCP_NODELAY,true)
                .option(ChannelOption.ALLOW_HALF_CLOSURE,false);

        if (writeBufferLowWaterMark >= 0 && writeBufferHighWaterMark > 0) {
            WriteBufferWaterMark waterMark = new WriteBufferWaterMark(writeBufferLowWaterMark, writeBufferHighWaterMark);
            bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, waterMark);
        }
    }


    private EventLoopGroup initEventLoopGroup(int nWorkers, ThreadFactory workerFactory) {
        return isNativeEt()? new EpollEventLoopGroup(nWorkers,workerFactory):new NioEventLoopGroup(nWorkers,workerFactory);
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
                        return new Thread(r,"NettyClientWorkerThread_"+this.threadIndex.incrementAndGet());
                    }
                }
        );
        if(isNativeEt()){
            bootstrap.channel(EpollSocketChannel.class);
        }else{
            bootstrap.channel(NioSocketChannel.class);
        }

        final ConnectionWatchdog watchdog = new ConnectionWatchdog(bootstrap,timer);



    }



    @Override
    public void shutdown() {

    }

    @Override
    public void registerRPCHook(RPCHook rpcHook) {

    }
}
