package org.wa.common.exception.remoting;

/**
 * @Auther: XF
 * @Date: 2018/10/4 13:39
 * @Description:
 */
public class RemotingTimeoutException extends RemotingException{
    public RemotingTimeoutException(String message) {
        super(message);
    }

    public RemotingTimeoutException(String message, long timeoutMillis) {
        this(message, timeoutMillis,null);
    }

    public RemotingTimeoutException(String addr,long timeoutMillis,Throwable cause){
        super("wait response on the channel <" + addr + "> timeout, " + timeoutMillis + "(ms)", cause);
    }

}
