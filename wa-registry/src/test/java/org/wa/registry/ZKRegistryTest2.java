package org.wa.registry;

import java.util.Scanner;

/**
 * @Auther: XF
 * @Date: 2018/10/6 18:54
 * @Description:
 */
public class ZKRegistryTest2 {
    public static void main(String[] args) throws InterruptedException {

        Scanner sc = new Scanner(System.in);
        while(sc.nextInt()==1){
            fun();
        }
    }
    static void fun(){
        ZKRegistry registry = ZKRegistry.newInstance();
        String service01 = registry.getProviderAddr("service01");
        System.out.println(service01);
    }
}
