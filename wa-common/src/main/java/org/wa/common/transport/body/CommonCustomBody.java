package org.wa.common.transport.body;
import org.wa.common.exception.remoting.RemotingCommonCustomException;
/**
 * @Auther: XF
 * @Date: 2018/10/3 19:23
 * @Description:
 */
public interface CommonCustomBody {
    void checkFields()throws RemotingCommonCustomException;
}
