package org.wa.registry;

/**
 * @Auther: XF
 * @Date: 2018/10/6 18:39
 * @Description:
 */
public class ZKRegistryTest {
    public static void main(String[] args) throws InterruptedException {
        ZKRegistry registry = ZKRegistry.newInstance();
        registry.registerProvider("service111","127.0.0.1:8088");
        Thread.sleep(1000000);
    }
}
