package org.wa.remoting.model;

import io.netty.channel.ChannelHandlerContext;
import org.wa.common.exception.remoting.RemotingSendRequestException;
import org.wa.common.exception.remoting.RemotingTimeoutException;

/**
 * @Auther: XF
 * @Date: 2018/10/4 15:18
 * @Description: 处理channel关闭或者inactive的状态的时候的改变
 */
public interface NettyChannelInactiveProcessor {

    void processChannelInactive(ChannelHandlerContext ctx) throws RemotingSendRequestException,RemotingTimeoutException,InterruptedException;

}
