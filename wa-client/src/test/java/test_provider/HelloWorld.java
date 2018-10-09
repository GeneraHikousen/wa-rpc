package test_provider;

import org.wa.client.provider.DefaultProvider;
import org.wa.client.provider.model.ServiceWrapper;
import org.wa.client.service.GetPrimeNumber;
import org.wa.client.service.GetPrimeNumberService;
import org.wa.client.service.HelloService;
import org.wa.client.service.HelloServiceImpl;

/**
 * @Auther: XF
 * @Date: 2018/10/7 21:32
 * @Description: 测试类
 */
public class HelloWorld {
    public static void main(String[] args) throws InterruptedException {
        /*创建、启动生产者*/
        DefaultProvider provider = new DefaultProvider();
        provider.start();

        /*将对象发布为服务*/
        HelloServiceImpl helloServiceImpl = new HelloServiceImpl();
        provider.registerService(HelloService.class,helloServiceImpl);

        /*将类发布为服务*/
        provider.registerService(GetPrimeNumber.class,GetPrimeNumberService.class);
    }
}
