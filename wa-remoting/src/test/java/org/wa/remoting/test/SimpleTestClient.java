package org.wa.remoting.test;

import org.wa.common.exception.remoting.RemotingCommonCustomException;
import org.wa.common.exception.remoting.RemotingException;
import org.wa.common.protocal.WaProtocol;
import org.wa.common.transport.body.CommonCustomBody;
import org.wa.remoting.model.RemotingTransporter;
import org.wa.remoting.netty.NettyClientConfig;
import org.wa.remoting.netty.NettyRemotingClient;

/**
 * @Auther: XF
 * @Date: 2018/10/5 20:09
 * @Description:
 */
public class SimpleTestClient {
    public static void main(String[] args) {
        NettyRemotingClient client = new NettyRemotingClient(new NettyClientConfig());
        client.init();
        client.start();
        RemotingTransporter request = RemotingTransporter.createRequestTransporter(WaProtocol.REQUEST_REMOTING, new CommonCustomBody() {
            @Override
            public void checkFields() throws RemotingCommonCustomException {

            }
        });
        try {
            client.invokeSync("localhost:8888",request,1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RemotingException e) {
            e.printStackTrace();
        }
    }
}
