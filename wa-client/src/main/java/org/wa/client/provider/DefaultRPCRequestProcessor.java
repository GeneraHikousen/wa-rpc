package org.wa.client.provider;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wa.client.provider.model.ServiceWrapper;
import org.wa.common.protocal.WaProtocal;
import org.wa.common.transport.body.RequestCustomBody;
import org.wa.common.transport.body.RespondCustomBody;
import org.wa.remoting.model.NettyRequestProcessor;
import org.wa.remoting.model.RemotingTransporter;

import java.util.concurrent.ConcurrentHashMap;

import static org.wa.common.serialization.SerializerHolder.serializerImpl;
import static org.wa.common.utils.Reflects.invoke;
import static org.wa.common.utils.Reflects.newInstance;

/**
 * @Auther: XF
 * @Date: 2018/10/7 17:19
 * @Description: 处理RPC请求{code=RPC_REQUEST}，默认实现类
 */
public class DefaultRPCRequestProcessor implements NettyRequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRPCRequestProcessor.class);

    private static final ConcurrentHashMap<String, ServiceWrapper> serviceTable = new ConcurrentHashMap<>();

    public void addService(String serviceName,ServiceWrapper serviceWrapper){
        serviceTable.put(serviceName,serviceWrapper);
    }

    public void delService(String serviceName){
        serviceTable.remove(serviceName);
    }

    @Override
    public RemotingTransporter processRequest(ChannelHandlerContext ctx, final RemotingTransporter request) throws Exception {
        RequestCustomBody requestBody = serializerImpl().readObject(request.bytes(), RequestCustomBody.class);
        ServiceWrapper serviceWrapper = serviceTable.get(requestBody.getServiceName());
        if(serviceWrapper==null){
            logger.error("cannot find service [{}]",requestBody.getServiceName());
            return null;
        }else{
            logger.info("new request [{}]",request);
        }
        Object result = null;
        Object serviceObject = null;
        if (serviceWrapper.isObjectSupport()) {   //以对象形式提供服务
            serviceObject = serviceWrapper.getObject();
        } else {    //需要初始化对象
            serviceObject = newInstance(serviceWrapper.getClazz());
        }

        String methodName = requestBody.getMethodName();
        Class<?>[] parameterTypes = requestBody.getParameterTypes();
        Object[] args = requestBody.getArgs();
        result = invoke(serviceObject, methodName, parameterTypes, args);
        RespondCustomBody respondCustomBody = new RespondCustomBody(result, null, true);

        RemotingTransporter respond = RemotingTransporter.createResponseTransporter(WaProtocal.RPC_RESPONSE, respondCustomBody, request.getOpaque());

        ctx.writeAndFlush(respond).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info("respond request [{}] succeed ", request);
                } else {
                    logger.info("respond request [{}] failed");
                }
            }
        });

        return null;
    }
}
