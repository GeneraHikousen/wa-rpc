package org.wa.remoting.netty.encode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.wa.remoting.model.RemotingTransporter;

import static org.wa.common.protocal.WaProtocol.MAGIC;
import static org.wa.common.serialization.SerializerHolder.serializerImpl;


/**
 * @Auther: XF
 * @Date: 2018/10/3 17:14
 * @Description: Netty 对{@link RemotingTransporter}的编码器
 */
public class RemotingTransporterEncoder extends MessageToByteEncoder<RemotingTransporter> {
    protected void encode(ChannelHandlerContext ctx, RemotingTransporter msg, ByteBuf out) throws Exception {
        doEncodeRemotingTransporter(msg, out);
    }

    private void doEncodeRemotingTransporter(RemotingTransporter msg, ByteBuf out) {
        byte[] body = serializerImpl().writeObject(msg.getCustomHeader());

        //协议头
        out.writeShort(MAGIC)   //magic
                .writeByte(msg.getTransporterType())    //传输类型sign，是请求还是响应
                .writeByte(msg.getCode())   //请求类型，表明主题信息的类型，也代表请求的类型
                .writeLong(msg.getOpaque()) //requestId
                .writeInt(body.length)  //bodyLength
                .writeBytes(body);
    }
}
