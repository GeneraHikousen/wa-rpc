package test_provider;

import org.wa.client.customer.DefaultCustomer;
import org.wa.client.service.GetPrimeNumber;
import org.wa.client.service.HelloService;

/**
 * @Auther: XF
 * @Date: 2018/10/7 22:00
 * @Description:
 */
public class HelloWorldClient {
    public static void main(String[] args) {
        /*创建并启动消费者*/
        DefaultCustomer customer = new DefaultCustomer();
        customer.start();

        /*获得动态代理的对象，调用远程的方法，就像调用本地方法一样*/
        HelloService helloService = (HelloService) customer.getRemotingProxy(HelloService.class);
        String s = helloService.sayHello();
        System.out.println(s);

        System.out.println("--------------");

        GetPrimeNumber getPrimeNumber = (GetPrimeNumber) customer.getRemotingProxy(GetPrimeNumber.class);
        int n = getPrimeNumber.NthPrime(10);
        System.out.println("prime:" + n);
    }
}
