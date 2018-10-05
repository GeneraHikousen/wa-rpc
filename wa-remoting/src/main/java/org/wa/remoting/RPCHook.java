package org.wa.remoting;

import org.wa.remoting.model.RemotingTransporter;

/**
 * @Auther: XF
 * @Date: 2018/10/4 14:42
 * @Description: RPC的回调钩子，在发送请求和接收请求的时候触发，增加程序的健壮性和灵活性
 */
public interface RPCHook {

    void doBeforeRequest(final String remoteAddr, final RemotingTransporter request);

    void doAfterResponse(final String remoteAddr,final RemotingTransporter request,final RemotingTransporter response);

}
