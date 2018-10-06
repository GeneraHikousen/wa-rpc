package org.wa.remoting.netty.idle;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.wa.remoting.model.Heartbeats;

/**
 * @Auther: XF
 * @Date: 2018/10/5 13:10
 * @Description:
 */
@ChannelHandler.Sharable
public class ConnectorIdleStateTrigger extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(Heartbeats.heartbeatContent());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
