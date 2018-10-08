package org.wa.common.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * @Auther: XF
 * @Date: 2018/10/7 19:26
 * @Description:
 */
public class NetUtil {
    /**
     * 获取服务器IP地址
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String  getServerIp(){
        String ip = null;
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            ip = inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return ip;
    }
}
