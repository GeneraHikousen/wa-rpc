package org.wa.common.exception.remoting;

/**
 * @Auther: XF
 * @Date: 2018/10/3 19:25
 * @Description:
 */
public class RemotingCommonCustomException extends RemotingException {
    public RemotingCommonCustomException(String message) {
        super(message);
    }

    public RemotingCommonCustomException(String message, Throwable cause) {
        super(message, cause);
    }
}
