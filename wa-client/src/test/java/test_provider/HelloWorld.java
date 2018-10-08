package test_provider;

import org.wa.client.provider.model.DefaultProvider;
import org.wa.client.provider.model.ServiceWrapper;
import org.wa.client.service.HelloService;

import javax.xml.ws.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: XF
 * @Date: 2018/10/7 21:32
 * @Description:
 */
public class HelloWorld {
    public static void main(String[] args) throws InterruptedException {
        String serviceName = HelloService.class.getName() + "$" + "sayHello";
        List<Class<?>[]> list = new ArrayList<>();
        list.add(new Class<?>[0]);
        ServiceWrapper serviceWrapper = new ServiceWrapper(HelloService.class, "sayHello",list);
        DefaultProvider provider = new DefaultProvider();
        provider.start();
        provider.registerService(serviceName, serviceWrapper);
    }
}
