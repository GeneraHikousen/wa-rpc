package org.wa.common.exception.remoting;

/**
 * @Auther: XF
 * @Date: 2018/10/4 13:44
 * @Description:
 */
public class RemotingSendRequestException extends RemotingException {
    public RemotingSendRequestException(String addr) {
        this(addr,null);
    }

    public RemotingSendRequestException(String addr, Throwable cause) {
        super("send request to <" + addr + "> failed", cause);
    }
}
