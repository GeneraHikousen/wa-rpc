package org.wa.remoting.model;

import io.netty.channel.ChannelHandlerContext;

/**
 * @Auther: XF
 * @Date: 2018/10/4 15:53
 * @Description:
 */
public interface NettyRpcRequestProcessor {

    void processRPCRequest(ChannelHandlerContext ctx,RemotingTransporter request);

}
