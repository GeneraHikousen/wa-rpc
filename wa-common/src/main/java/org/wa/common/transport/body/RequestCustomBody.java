package org.wa.common.transport.body;

import org.wa.common.exception.remoting.RemotingCommonCustomException;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @Auther: XF
 * @Date: 2018/10/6 22:59
 * @Description: 定义一个请求
 */
public class RequestCustomBody implements CommonCustomBody{


    private static final AtomicLong invokeIdGenerator = new AtomicLong(0);

    private final long invokeID;    //调用ID
    private String serviceName; //服务名
    private Object[] args;  //参数
    private long timestamp;

    public RequestCustomBody(){
        this(invokeIdGenerator.getAndIncrement());
    }

    public RequestCustomBody(long invokeID){
        this.invokeID=invokeID;
    }

    public long getInvokeID() {
        return invokeID;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }



    @Override
    public void checkFields() throws RemotingCommonCustomException {

    }
}
