package org.wa.remoting.watcher;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @Auther: XF
 * @Date: 2018/10/4 22:50
 * @Description: 监控重连狗
 */
@ChannelHandler.Sharable
public abstract class ConnectionWatchdog extends ChannelInboundHandlerAdapter implements TimerTask, ChannelHandlerHolder {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionWatchdog.class);

    private final Bootstrap bootstrap;

    private final Timer timer;

    private boolean firstConnection = true;

    private volatile SocketAddress remoteAddress;

    private volatile boolean reconnect = true;
    private int attempts;

    public ConnectionWatchdog(Bootstrap bootstrap,Timer timer){
        this.bootstrap = bootstrap;
        this.timer = timer;
    }

    public boolean isReconnect() {
        return reconnect;
    }

    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();

        this.attempts=0;
        this.firstConnection=true;

        logger.info("Connects with {}." ,channel);

        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("当前channel {} inactive,将关闭连接");
        boolean doReconnect = reconnect;
        if(doReconnect){
            if(firstConnection){
                remoteAddress = ctx.channel().remoteAddress();
                firstConnection = false;
            }
            if(attempts<12){
                attempts++;
            }
            long timeout = 2<<attempts;
            logger.info("因为channel关闭所以进行重连");
            timer.newTimeout(this,timeout,MILLISECONDS);
        }

        logger.warn("Disconnects with {}, address: {}, reconnect: {}.", ctx.channel(), remoteAddress, doReconnect);

        ctx.fireChannelActive();
    }


    @Override
    public void run(Timeout timeout) throws Exception {
        logger.info("重新连接");
        ChannelFuture future;
        synchronized (bootstrap){
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(handlers());
                }
            });
            future = bootstrap.connect();
        }
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                boolean succeed = future.isSuccess();
                logger.warn("Reconnects with {}, {}.", remoteAddress, succeed ? "succeed" : "failed");
                if (!succeed){
                    future.channel().pipeline().fireChannelActive();
                }
            }
        });
    }
}
