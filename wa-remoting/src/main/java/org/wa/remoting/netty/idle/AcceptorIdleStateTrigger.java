package org.wa.remoting.netty.idle;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.wa.common.exception.remoting.RemotingNoSighException;

/**
 * @Auther: XF
 * @Date: 2018/10/5 13:07
 * @Description:
 */
@ChannelHandler.Sharable
public class AcceptorIdleStateTrigger extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("accept heartbeat");
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                throw new RemotingNoSighException("no sign");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
