package org.wa.client.provider;

import org.wa.client.provider.model.ServiceWrapper;
import org.wa.common.utils.NetUtil;
import org.wa.registry.ZKRegistry;
import org.wa.remoting.netty.NettyRemotingServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Auther: XF
 * @Date: 2018/10/7 19:12
 * @Description: 默认生产者
 */
public class DefaultProvider {

    private NettyRemotingServer server;
    private DefaultRPCRequestProcessor defaultRpcRequestProcessor;
    private ZKRegistry zkRegistry;
    private ExecutorService publicExecutors;

    public void start(){
        /*先初始化注册中心*/
        if(zkRegistry==null)
            zkRegistry = new ZKRegistry();

        /*初始化nettyService*/
        if (server==null)
            server = new NettyRemotingServer();

        /*初始化默认RPC请求处理器*/
        if(defaultRpcRequestProcessor==null)
            defaultRpcRequestProcessor = new DefaultRPCRequestProcessor();

        /*初始化公共执行器*/
        if(publicExecutors==null)
            publicExecutors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);

        zkRegistry.start();

        server.registerDefaultProcessor(defaultRpcRequestProcessor,publicExecutors);
        server.init();
        server.start();
    }

    /**
     * 将一个类注册为服务
     * 每次rpc调用都会通过反射创建一个对象
     * @param clazz
     */
    public void registerService(Class<?> interface_,Class clazz){
        //本机IP+server端口
        String addr = NetUtil.getServerIp()+":"+server.getConfig().getListenPort();
        String serviceName = interface_.getName();
        //先写入注册中心
        zkRegistry.registerProvider(serviceName,addr);
        //把服务添加到rpc处理器中
        defaultRpcRequestProcessor.addService(serviceName,new ServiceWrapper(interface_,clazz));
    }

    /**
     * 将一个对象注册为服务
     * 每一次调用都会调用该对象的方法，不保证线程安全
     */
    public void registerService(Class<?> interface_,Object object){
        //本机IP+server端口
        String addr = NetUtil.getServerIp()+":"+server.getConfig().getListenPort();
        String serviceName = interface_.getName();
        //先写入注册中心
        zkRegistry.registerProvider(serviceName,addr);
        //把服务添加到rpc处理器中
        defaultRpcRequestProcessor.addService(serviceName,new ServiceWrapper(interface_,object));
    }

    public NettyRemotingServer getServer() {
        return server;
    }

    public void setServer(NettyRemotingServer server) {
        this.server = server;
    }

    public DefaultRPCRequestProcessor getDefaultRpcRequestProcessor() {
        return defaultRpcRequestProcessor;
    }

    public void setDefaultRpcRequestProcessor(DefaultRPCRequestProcessor defaultRpcRequestProcessor) {
        this.defaultRpcRequestProcessor = defaultRpcRequestProcessor;
    }

    public ZKRegistry getZkRegistry() {
        return zkRegistry;
    }

    public void setZkRegistry(ZKRegistry zkRegistry) {
        this.zkRegistry = zkRegistry;
    }

    public ExecutorService getPublicExecutors() {
        return publicExecutors;
    }

    public void setPublicExecutors(ExecutorService publicExecutors) {
        this.publicExecutors = publicExecutors;
    }
}
