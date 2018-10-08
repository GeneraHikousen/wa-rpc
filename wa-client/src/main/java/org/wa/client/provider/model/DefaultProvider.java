package org.wa.client.provider.model;

import org.wa.client.provider.DefaultRPCRequestProcessor;
import org.wa.common.protocal.WaProtocal;
import org.wa.common.utils.NetUtil;
import org.wa.registry.ZKRegistry;
import org.wa.remoting.netty.NettyRemotingServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Auther: XF
 * @Date: 2018/10/7 19:12
 * @Description:
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

    public void registerService(String serviceName,ServiceWrapper serviceWrapper){
        //本机IP+server端口
        String addr = NetUtil.getServerIp()+":"+server.getConfig().getListenPort();
        //先写入注册中心
        zkRegistry.registerProvider(serviceName,addr);
        //把服务添加到rpc处理器中
        defaultRpcRequestProcessor.addService(serviceName,serviceWrapper);
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
