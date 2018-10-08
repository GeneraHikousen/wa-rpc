package org.wa.remoting.test;

import org.wa.remoting.netty.NettyRemotingServer;
import org.wa.remoting.netty.NettyServerConfig;

/**
 * @Auther: XF
 * @Date: 2018/10/5 19:42
 * @Description:
 */
public class SimpleTestServer {
    public static void main(String[] args) {
        NettyRemotingServer server = new NettyRemotingServer(new NettyServerConfig());
        server.init();
        server.start();

    }
}
