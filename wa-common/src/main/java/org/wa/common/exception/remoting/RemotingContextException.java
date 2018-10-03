package org.wa.common.exception.remoting;

/**
 * @Auther: XF
 * @Date: 2018/10/3 21:13
 * @Description:
 */
public class RemotingContextException extends RemotingException {

    public RemotingContextException(String message) {
        super(message);
    }

    public RemotingContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
