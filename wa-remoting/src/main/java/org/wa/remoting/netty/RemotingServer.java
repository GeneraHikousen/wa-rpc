package org.wa.remoting.netty;

import io.netty.channel.Channel;
import org.wa.common.exception.remoting.RemotingSendRequestException;
import org.wa.common.exception.remoting.RemotingTimeoutException;
import org.wa.common.utils.Pair;
import org.wa.remoting.model.NettyChannelInactiveProcessor;
import org.wa.remoting.model.NettyRequestProcessor;
import org.wa.remoting.model.RemotingTransporter;

import java.util.concurrent.ExecutorService;

/**
 * @Auther: XF
 * @Date: 2018/10/4 14:53
 * @Description: netty服务端的一些抽象方法
 * a.作为服务端要处理来自客户端的一些请求，每个请求都有一个与之对应的处理器
 * b.这样的好处是简化了Netty handler的配置，将handler的配置放到每个对应的处理器中来
 */
public interface RemotingServer extends BaseRemotingService{

    void registerProcessor(final byte requestCode, final NettyRequestProcessor processor, final ExecutorService executor);

    void registerChannelInactiveProcessor(final NettyChannelInactiveProcessor processor, final ExecutorService executor);

    void registerDefaultProcessor(final NettyRequestProcessor processor, final ExecutorService executor);

    Pair<NettyRequestProcessor, ExecutorService> getProcessorPair(final int requestCode);

    RemotingTransporter invokeSync(final Channel channel, final RemotingTransporter request, final long timeoutMillis) throws InterruptedException, RemotingSendRequestException, RemotingTimeoutException;

}
