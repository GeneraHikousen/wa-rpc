package org.wa.remoting.model;

import org.wa.remoting.InvokeCallback;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: XF
 * @Date: 2018/10/4 12:57
 * @Description: 请求返回对象的包装类
 */
public class RemotingResponse {
    //远程端返回的结果集
    private volatile RemotingTransporter remotingTransporter;

    //该请求抛出的异常，如果存在的话
    private volatile Throwable cause;

    //发送端是否成功发送
    private volatile boolean sendRequestOK = true;

    //请求的opaque
    private final long opaque;

    //默认回调函数
    private final InvokeCallback invokeCallback;

    //请求默认超时时间
    private final long timeoutMillis;

    private final long beginTimestamp = System.currentTimeMillis();
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public RemotingResponse(long opaque,long timeoutMillis,InvokeCallback invokeCallback){
        this.invokeCallback = invokeCallback;
        this.opaque = opaque;
        this.timeoutMillis = timeoutMillis;
    }

    public void executeInvokeCallback(){
        if(invokeCallback!=null){
            invokeCallback.operationComplete(this);
        }
    }

    public boolean isSendRequestOK(){
        return sendRequestOK;
    }

    public void setSendRequestOK(boolean sendRequestOK) {
        this.sendRequestOK = sendRequestOK;
    }

    public long getOpaque() {
        return opaque;
    }

    public RemotingTransporter getRemotingTransporter() {
        return remotingTransporter;
    }

    public void setRemotingTransporter(RemotingTransporter remotingTransporter) {
        this.remotingTransporter = remotingTransporter;
    }

    public Throwable getCause(){
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    public RemotingTransporter waitResponse()throws InterruptedException{
        this.countDownLatch.await(this.timeoutMillis,TimeUnit.MILLISECONDS);
        return this.remotingTransporter;
    }

    /**
     * 当远程结果返回的时候，TCP长连接上层载体channel会将其放入到与requestID对应的Response中
     * @param remotingTransporter
     */
    public void putResponse(final RemotingTransporter remotingTransporter){
        this.remotingTransporter = remotingTransporter;
        countDownLatch.countDown();
    }
}
