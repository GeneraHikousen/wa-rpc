package org.wa.client.provider;

import org.wa.remoting.model.RemotingTransporter;

/**
 * @Auther: XF
 * @Date: 2018/10/5 20:48
 * @Description:
 */
public interface Provider {
    /**
     * 启动provider实例
     */
    void start();

    /**
     * 发布服务
     */
    void publishedAndStartProvider();

    /**
     * 暴露服务器地址
     */
    void serviceListenPort(int exposePort);

    /**
     * 设置注册中心地址
     */
    Provider registryAddress(String registryAddress);

    /**
     * 监控中心的地址，不是强依赖，不设置也没有关系
     * @param monitorAddress
     * @return
     */
    Provider monitorAddress(String monitorAddress);

    /**
     * 需要暴露的实例
     * @param obj
     */
    Provider publishService(Object ...obj);

    /**
     * 处理消费者rpc请求
     * @param remotingTransport
     */
    void handlerRequest(RemotingTransporter remotingTransport);

}
