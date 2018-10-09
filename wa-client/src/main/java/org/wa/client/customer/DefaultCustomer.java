package org.wa.client.customer;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wa.common.exception.remoting.RemotingException;
import org.wa.common.protocal.WaProtocal;
import org.wa.common.serialization.SerializerHolder;
import org.wa.common.transport.body.RequestCustomBody;
import org.wa.common.transport.body.RespondCustomBody;
import org.wa.registry.ZKRegistry;
import org.wa.remoting.model.RemotingTransporter;
import org.wa.remoting.netty.NettyRemotingClient;

import java.lang.reflect.Method;

/**
 * @Auther: XF
 * @Date: 2018/10/7 18:14
 * @Description:
 */
public class DefaultCustomer<T> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCustomer.class);

    private ZKRegistry zkRegistry;

    private NettyRemotingClient client;

    public DefaultCustomer(){
    }

    public void start(){
        if(zkRegistry == null)
            zkRegistry=new ZKRegistry();
        if(!zkRegistry.isStart())
            zkRegistry.start();
        if (client==null)
            client = new NettyRemotingClient();
        client.start();
    }

    class CglibProxy<T> implements MethodInterceptor{

        private Class<T> clazz;

        public CglibProxy(Class<T> clazz){
            this.clazz = clazz;
        }

        public  T getProxy(){
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(clazz);
            enhancer.setCallback(this);
            return (T) enhancer.create();
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            System.out.println(clazz.getName());
            String addr = zkRegistry.getProviderAddr(clazz.getName());

            if(addr==null){ //没有找到提供服务的远程主机
                logger.error("no provider for service [{}] on line");
                throw new RemotingException("no provide on line");
            }

            /*初始化一个请求对象*/
            RequestCustomBody body = new RequestCustomBody();
            body.setServiceName(clazz.getName());
            body.setArgs(objects);
            body.setParameterTypes(method.getParameterTypes());
            body.setMethodName(method.getName());

            /*发送给远程主机，获得返回结果*/
            RemotingTransporter requestTransporter = RemotingTransporter.createRequestTransporter(WaProtocal.RPC_REQUEST,body);
            RemotingTransporter respondTransporter = client.invokeSync(addr, requestTransporter, 1000);
            RespondCustomBody respondCustomBody = SerializerHolder.serializerImpl().readObject(respondTransporter.bytes(),RespondCustomBody.class);
            return respondCustomBody.getResult();
        }
    }

    public ZKRegistry getZkRegistry() {
        return zkRegistry;
    }

    public void setZkRegistry(ZKRegistry zkRegistry) {
        this.zkRegistry = zkRegistry;
    }

    public NettyRemotingClient getClient() {
        return client;
    }

    public void setClient(NettyRemotingClient client) {
        this.client = client;
    }


    public Object getRemotingProxy(Class clazz){
        return new CglibProxy(clazz).getProxy();
    }

    public static void main(String[] args) {



    }
}
