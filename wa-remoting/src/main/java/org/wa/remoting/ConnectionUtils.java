package org.wa.remoting;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;


/**
 * @Auther: XF
 * @Date: 2018/10/4 18:18
 * @Description:
 */
public class ConnectionUtils {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionUtils.class);

    public static String parseChannelRemotingAddr(final Channel channel) {
        if (channel == null)
            return "";
        final SocketAddress remote = channel.remoteAddress();
        final String addr = remote == null ? "" : remote.toString();
        if(addr.length()>0){
            int index = addr.lastIndexOf("/");
            if(index>0){
                return addr.substring(index+1);
            }
            return addr;
        }
        return "";
    }

}
