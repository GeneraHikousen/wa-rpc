package org.wa.remoting.model;

import org.wa.common.protocal.WaProtocal;
import org.wa.common.transport.body.CommonCustomBody;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @Auther: XF
 * @Date: 2018/10/3 17:12
 * @Description: 网络传输的唯一对象
 */
public class RemotingTransporter extends ByteHolder{

    private static final AtomicLong requestID = new AtomicLong(0L);

    //请求的类型
    private byte code;

    /**
     * 请求的主体消息，
     */
    private transient CommonCustomBody customHeader;

    //请求的时间戳
    private transient long timestamp;

    //请求的ID
    private long opaque = requestID.getAndIncrement();

    //定义该传输对象是请求还是响应
    private byte transporterType;

    protected RemotingTransporter(){
    }

    /**
     * 创建请求传输对象
     * @param code  请求对象的类型
     * @param customHeader  正文
     * @return
     */
    public static RemotingTransporter createRequestTransporter(byte code,CommonCustomBody customHeader){
        RemotingTransporter remotingTransporter = new RemotingTransporter();
        remotingTransporter.setCode(code);
        remotingTransporter.customHeader = customHeader;
        remotingTransporter.transporterType = WaProtocal.REQUEST_REMOTING;
        return remotingTransporter;
    }

    /**
     * 创建响应传输对象
     * @param code  类型
     * @param customHeader  正文
     * @param opaque    此响应对象对应的请求对象ID
     * @return
     */
    public static RemotingTransporter createResponseTransporter(byte code,CommonCustomBody customHeader,long opaque){
        RemotingTransporter remotingTransporter = new RemotingTransporter();
        remotingTransporter.setCode(code);
        remotingTransporter.customHeader = customHeader;
        remotingTransporter.setOpaque(opaque);
        remotingTransporter.transporterType = WaProtocal.RESPONSE_REMOTING;
        return remotingTransporter;
    }

    public byte getTransporterType() {
        return transporterType;
    }

    public void setTransporterType(byte transporterType) {
        this.transporterType = transporterType;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public long getOpaque() {
        return opaque;
    }

    public void setOpaque(long opaque) {
        this.opaque = opaque;
    }

    public long timestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public CommonCustomBody getCustomHeader() {
        return customHeader;
    }

    public void setCustomHeader(CommonCustomBody customHeader) {
        this.customHeader = customHeader;
    }

    public static RemotingTransporter newInstance(long id,byte sign,byte type,byte[] bytes){
        RemotingTransporter remotingTransporter = new RemotingTransporter();
        remotingTransporter.setCode(sign);
        remotingTransporter.setOpaque(id);
        remotingTransporter.setTransporterType(type);
        remotingTransporter.bytes(bytes);
        return remotingTransporter;
    }

    @Override
    public String toString() {
        return "RemotingTransporter [code=" + code + ", customHeader=" + customHeader + ", timestamp=" + timestamp + ", opaque=" + opaque
                + ", transporterType=" + transporterType + "]";
    }
}
