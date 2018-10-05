package org.wa.remoting.netty;

import org.wa.common.exception.remoting.RemotingException;
import org.wa.common.exception.remoting.RemotingSendRequestException;
import org.wa.common.exception.remoting.RemotingTimeoutException;
import org.wa.remoting.model.NettyChannelInactiveProcessor;
import org.wa.remoting.model.NettyRequestProcessor;
import org.wa.remoting.model.RemotingTransporter;

import java.util.concurrent.ExecutorService;

/**
 * @Auther: XF
 * @Date: 2018/10/4 12:16
 * @Description:
 */
public interface RemotingClient extends BaseRemotingService{
    /**
     * 向某个地址发送request请求，并且远程返回 #RemotingTransporter 结果，调用超时为timeoutMillis
     * @param addr 远程地址，例如127.0.0.1:8080
     * @param request   请求入参
     * @param timeoutMillis     超时时间
     * @return
     * @throws RemotingTimeoutException
     * @throws RemotingSendRequestException
     * @throws InterruptedException
     * @throws RemotingException
     */
    public RemotingTransporter invokeSync(final String addr,final RemotingTransporter request,final long timeoutMillis) throws RemotingTimeoutException,RemotingSendRequestException,InterruptedException,RemotingException;

    /**
     * 注入处理器，例如某个Netty的client实例，这个实例是consumer端的，它需要处理订阅返回的信息
     * 假如订阅的requestCode是100，那么指定requestCode特定的处理器ProcessorA，且指定该处理器线程的执行器是executorA
     * 这样的好处是业务逻辑清晰，不同业务对应不同处理器
     * 一般场景下，不是高并发场景下，executor可以复用，这样可以减少上下文的切换
     * @param requestCode
     * @param processor
     * @param executor
     */
    void registerProcessor(final byte requestCode, final NettyRequestProcessor processor, final ExecutorService executor);

    /**
     * 注册channel inactive的处理器
     * @param processor
     * @param executor
     */
    void registerChannelInactiveProcessor (NettyChannelInactiveProcessor processor, ExecutorService executor);

    /**
     * 某个地址的长连接是否可写
     * @param addr
     * @return
     */
    boolean isChannelWritable (final String addr);

    /**
     * 当与server的channel inactive 时，是否主动连接Netty的server端
     */
    void setReconnect(boolean isReconnect);
}
