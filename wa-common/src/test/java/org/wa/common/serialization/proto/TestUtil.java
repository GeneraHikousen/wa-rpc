package org.wa.common.serialization.proto;

import org.wa.common.utils.NetUtil;

/**
 * @Auther: XF
 * @Date: 2018/10/7 19:27
 * @Description:
 */
public class TestUtil {
    public static void main(String[] args) {
        String addr = NetUtil.getServerIp();
        System.out.println(addr);
    }
}
