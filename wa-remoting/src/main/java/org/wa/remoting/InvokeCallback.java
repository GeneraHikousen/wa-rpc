package org.wa.remoting;

import org.wa.remoting.model.RemotingResponse;

/**
 * @Auther: XF
 * @Date: 2018/10/4 13:36
 * @Description: 远程调用之后的回调函数
 */
public interface InvokeCallback {

    void operationComplete(final RemotingResponse remotingResponse);

}
