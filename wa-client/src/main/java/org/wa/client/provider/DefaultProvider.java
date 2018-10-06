package org.wa.client.provider;

import org.wa.remoting.model.RemotingTransporter;
import org.wa.remoting.netty.NettyRemotingServer;

/**
 * @Auther: XF
 * @Date: 2018/10/5 21:57
 * @Description:
 */
public class DefaultProvider implements Provider{

    private NettyRemotingServer nettyRemotingServer;


    public void start() {

    }

    public void publishedAndStartProvider() {

    }

    public void serviceListenPort(int exposePort) {

    }

    public Provider registryAddress(String registryAddress) {
        return null;
    }

    public Provider monitorAddress(String monitorAddress) {
        return null;
    }

    public Provider publishService(Object... obj) {
        return null;
    }

    public void handlerRequest(RemotingTransporter remotingTransport) {

    }
}
