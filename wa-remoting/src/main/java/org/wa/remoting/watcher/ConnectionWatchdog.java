package org.wa.remoting.watcher;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

/**
 * @Auther: XF
 * @Date: 2018/10/4 22:50
 * @Description:
 */
public class ConnectionWatchdog extends ChannelInboundHandlerAdapter implements TimerTask, ChannelHandlerHolder {

    @Override
    public ChannelHandler[] handlers() {
        return new ChannelHandler[0];
    }

    @Override
    public void run(Timeout timeout) throws Exception {

    }
}
