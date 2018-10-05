package org.wa.remoting.netty;

import org.wa.remoting.RPCHook;

/**
 * @Auther: XF
 * @Date: 2018/10/4 12:04
 * @Description: 定义client端和server都需要实现的方法集合
 */
public interface BaseRemotingService {
    /**
     * Netty的一些参数的初始化
     */
    void init();

    /**
     * 启动Netty
     */
    void start();

    /**
     * 关闭Netty C/S 实例
     */
    void shutdown();

    /**
     * 注入钩子，Netty在处理过程中可以嵌入一些方法，增加代码的灵活性
     */
    void registerRPCHook(RPCHook rpcHook);
}
