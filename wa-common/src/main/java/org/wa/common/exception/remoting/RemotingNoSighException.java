package org.wa.common.exception.remoting;

/**
 * @Auther: XF
 * @Date: 2018/10/5 13:09
 * @Description:
 */
public class RemotingNoSighException extends RemotingException {

    private static final long serialVersionUID = -1661779813708564404L;


    public RemotingNoSighException(String message) {
        super(message, null);
    }


    public RemotingNoSighException(String message, Throwable cause) {
        super(message, cause);
    }

}
