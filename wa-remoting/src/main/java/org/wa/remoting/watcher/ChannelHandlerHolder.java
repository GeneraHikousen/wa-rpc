package org.wa.remoting.watcher;

import io.netty.channel.ChannelHandler;

/**
 * @Auther: XF
 * @Date: 2018/10/4 22:53
 * @Description:
 */
public interface ChannelHandlerHolder {
    ChannelHandler[] handlers();
}
