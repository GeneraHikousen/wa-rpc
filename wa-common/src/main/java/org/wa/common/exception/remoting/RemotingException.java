package org.wa.common.exception.remoting;

/**
 * @Auther: XF
 * @Date: 2018/10/3 19:28
 * @Description:
 */
public class RemotingException extends Exception {
    public RemotingException(String message){
        super(message);
    }
    public RemotingException(String message,Throwable cause){
        super(message,cause);
    }
}
