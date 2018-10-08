package test_provider;

import org.wa.client.service.HelloService;
import org.wa.common.exception.remoting.RemotingException;
import org.wa.common.protocal.WaProtocal;
import org.wa.common.serialization.SerializerHolder;
import org.wa.common.transport.body.RequestCustomBody;
import org.wa.common.transport.body.RespondCustomBody;
import org.wa.remoting.model.RemotingTransporter;
import org.wa.remoting.netty.NettyClientConfig;
import org.wa.remoting.netty.NettyRemotingClient;

/**
 * @Auther: XF
 * @Date: 2018/10/7 22:00
 * @Description:
 */
public class HelloWorldClient {
    public static void main(String[] args) {
        NettyRemotingClient client = new NettyRemotingClient(new NettyClientConfig());
        client.init();
        client.start();
        RequestCustomBody body = new RequestCustomBody();
        body.setServiceName(HelloService.class.getName() + "$" + "sayHello");
        body.setArgs(new Object[0]);
        body.setTimestamp(System.currentTimeMillis());
        RemotingTransporter requestTransporter = RemotingTransporter.createRequestTransporter(WaProtocal.RPC_REQUEST, body);
        try {
            RemotingTransporter respond = client.invokeSync("127.0.0.1:6666", requestTransporter, 2000);
            RespondCustomBody respondCustomBody = SerializerHolder.serializerImpl().readObject(respond.bytes(), RespondCustomBody.class);
            System.out.println((String)respondCustomBody.getResult());
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        } catch (RemotingException e) {
            e.printStackTrace();
            return;
        }
    }
}
