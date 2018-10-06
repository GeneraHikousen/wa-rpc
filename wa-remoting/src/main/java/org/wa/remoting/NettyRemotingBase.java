package org.wa.remoting;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wa.common.exception.remoting.RemotingSendRequestException;
import org.wa.common.exception.remoting.RemotingTimeoutException;
import org.wa.common.protocal.WaProtocol;
import org.wa.common.utils.Pair;
import org.wa.remoting.model.NettyChannelInactiveProcessor;
import org.wa.remoting.model.NettyRequestProcessor;
import org.wa.remoting.model.RemotingResponse;
import org.wa.remoting.model.RemotingTransporter;

import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.wa.common.protocal.WaProtocol.REQUEST_REMOTING;
import static org.wa.common.protocal.WaProtocol.RESPONSE_REMOTING;

/**
 * @Auther: XF
 * @Date: 2018/10/4 16:21
 * @Description: netty C/S 端的客户端的提取，子类去完成Netty的一些创建的事情，该抽象类则去完成子类创建好的channel和远程端交互
 */
public abstract class NettyRemotingBase {

    private static final Logger logger = LoggerFactory.getLogger(NettyRemotingBase.class);

    //key为请求的opaque,value为远程返回结果的封装类
    private static final ConcurrentHashMap<Long, RemotingResponse> responseTable = new ConcurrentHashMap<>(256);

    //如果没有对创建的Netty网络段注入默认的处理器时，默认使用该处理器
    protected Pair<NettyRequestProcessor, ExecutorService> defaultRequestProcessor;

    //netty网络段channelInactive时间发生时的处理器
    protected Pair<NettyChannelInactiveProcessor, ExecutorService> defaultChannelInactiveProcessor;

    protected final ExecutorService publicExecutor = Executors.newFixedThreadPool(4, new ThreadFactory() {
        private AtomicInteger threadIndex = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "NettyClientPublicExecutor_" + this.threadIndex.incrementAndGet());
        }
    });

    //requestCode和对应的Processor
    protected HashMap<Byte, Pair<NettyRequestProcessor, ExecutorService>> processorTable = new HashMap<>(64);

    //远程端调用的具体实现
    public RemotingTransporter invokeSyncImpl(final Channel channel, final RemotingTransporter request, final long timeoutMillis) throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException {
        try {
            //构造一个response的封装体，ID和requestID对应
            final RemotingResponse response = new RemotingResponse(request.getOpaque(), timeoutMillis, null);

            //将response放到“篮子”中，等待远端结果填充response
            responseTable.put(request.getOpaque(), response);

            //发送请求
            channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) { //如果发送成功，则设置状态为成功
                        response.setSendRequestOK(true);
                    } else {
                        response.setSendRequestOK(false);
                        //发送失败，将respond从“篮子”中移除
                        responseTable.remove(request.getOpaque());
                        //设置失败的异常信息
                        response.setCause(future.cause());
                        //设置当前请求的返回主体为null
                        response.setRemotingTransporter(null);
                        logger.warn("user channel [{}] send msg [{}] failed reason is [{}]", channel, request, future.cause().getMessage());
                    }
                }
            });

            //阻塞等待客户端返回或者timeout
            RemotingTransporter remotingTransporter = response.waitResponse();
            if (remotingTransporter == null) {
                if (response.isSendRequestOK()) { //客户端发送成功，但服务器端处理超时
                    throw new RemotingTimeoutException(ConnectionUtils.parseChannelRemoteAddr(channel), timeoutMillis, response.getCause());
                } else {  //客户端发送失败
                    throw new RemotingSendRequestException(ConnectionUtils.parseChannelRemoteAddr(channel), response.getCause());
                }
            }
            return remotingTransporter;
        } finally {
            responseTable.remove(request.getOpaque());
        }
    }

    //channelRead0()对应的具体实现
    public void processMessageReceived(ChannelHandlerContext ctx, RemotingTransporter msg) {
        if (logger.isDebugEnabled()) {
            logger.debug("channel [] received RemotingTransporter is [{}]", ctx.channel(), msg);
        }
        final RemotingTransporter remotingTransporter = msg;

        if (remotingTransporter != null) {
            switch (remotingTransporter.getTransporterType()) {
                case REQUEST_REMOTING:
                    processRemotingRequest(ctx, remotingTransporter);
                    break;
                case RESPONSE_REMOTING:
                    processRemotingResponse(ctx, remotingTransporter);
                    break;
                default:
                    break;
            }
        }
    }


    protected void processChannelInactive(final ChannelHandlerContext ctx) {
        final Pair<NettyChannelInactiveProcessor, ExecutorService> pair = this.defaultChannelInactiveProcessor;
        if (pair != null) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        pair.getKey().processChannelInactive(ctx);
                    } catch (RemotingSendRequestException | InterruptedException | RemotingTimeoutException e) {
                        logger.error("server occur exception [{}]", e.getMessage());
                    }
                }
            };
            try {
                pair.getValue().submit(run);
            } catch (Exception e) {
                logger.error("server is busy,[{}]", e.getMessage());
            }
        }
    }

    protected void processRemotingRequest(final ChannelHandlerContext ctx, final RemotingTransporter remotingTransporter) {
        Pair<NettyRequestProcessor, ExecutorService> matchedPair = processorTable.get(remotingTransporter.getCode());
        final Pair<NettyRequestProcessor, ExecutorService> pair = matchedPair != null ? matchedPair : NettyRemotingBase.this.defaultRequestProcessor;
        if (pair != null) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        RPCHook rpcHook = NettyRemotingBase.this.getRPCHook();
                        if (rpcHook != null) {
                            rpcHook.doBeforeRequest(ConnectionUtils.parseChannelRemoteAddr(ctx.channel()), remotingTransporter);
                        }
                        final RemotingTransporter response = pair.getKey().processRequest(ctx, remotingTransporter);
                        if (rpcHook != null) {
                            rpcHook.doAfterResponse(ConnectionUtils.parseChannelRemoteAddr(ctx.channel()), remotingTransporter, response);
                        }
                        if (null != response) {
                            ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future) throws Exception {
                                    if (!future.isSuccess()) {
                                        logger.error("fail send respond,exception is [{}]", future.cause().getMessage());
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        logger.error("process occur exception [{}]", e.getMessage());
                        //向客户端发送出错信息
                        final RemotingTransporter response = RemotingTransporter.newInstance(remotingTransporter.getOpaque(), WaProtocol.RESPONSE_REMOTING, WaProtocol.HANDLER_ERROR, null);
                        ctx.writeAndFlush(response);
                    }
                }
            };
            try {
                pair.getValue().submit(run);
            } catch (Exception e) {
                logger.error("server is busy,[{}]", e.getMessage());
                final RemotingTransporter response = RemotingTransporter.newInstance(remotingTransporter.getOpaque(), WaProtocol.RESPONSE_REMOTING, WaProtocol.HANDLER_BUSY, null);
                ctx.writeAndFlush(response);
            }
        }
    }

    protected abstract RPCHook getRPCHook();

    protected void processRemotingResponse(ChannelHandlerContext ctx,RemotingTransporter remotingTransporter){
        RemotingResponse response = responseTable.get(remotingTransporter.getOpaque());
        //如果没有超时，response不为null
        if(response!=null){
//            response.setRemotingTransporter(remotingTransporter);
            //将server返回的消息放到respond中，并且countdown
            response.putResponse(remotingTransporter);
            //将response移除
            responseTable.remove(remotingTransporter.getOpaque());
        }else{  //超时了，response已经被移除
            logger.warn("received response but matched ID is removed for the responseTable maybe timeout");
            logger.warn(remotingTransporter.toString());
        }
    }
}
