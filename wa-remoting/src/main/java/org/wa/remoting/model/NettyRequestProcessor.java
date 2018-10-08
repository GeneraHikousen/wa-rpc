package org.wa.remoting.model;

import io.netty.channel.ChannelHandlerContext;

/**
 * @Auther: XF
 * @Date: 2018/10/4 15:14
 * @Description:
 */
public interface NettyRequestProcessor {

    RemotingTransporter processRequest(ChannelHandlerContext ctx, RemotingTransporter request) throws Exception;

}
