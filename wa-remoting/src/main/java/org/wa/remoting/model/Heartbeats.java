package org.wa.remoting.model;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static org.wa.common.protocal.WaProtocol.HEAD_LENGTH;
import static org.wa.common.protocal.WaProtocol.HEARTBEAT;
import static org.wa.common.protocal.WaProtocol.MAGIC;

/**
 * @Auther: XF
 * @Date: 2018/10/4 16:01
 * @Description:
 */
public class Heartbeats {
    private static final ByteBuf HEARTBEAT_BUF;

    static {
        ByteBuf buf = Unpooled.buffer(HEAD_LENGTH);
        buf.writeShort(MAGIC);
        buf.writeByte(HEARTBEAT);
        buf.writeByte(0);
        buf.writeLong(0);
        buf.writeInt(0);
        HEARTBEAT_BUF=Unpooled.unmodifiableBuffer(buf);
    }

    /**
     * @return the shared heartbeat content
     */
    public static ByteBuf heartbeatContent(){
        return HEARTBEAT_BUF.duplicate();
    }
}
