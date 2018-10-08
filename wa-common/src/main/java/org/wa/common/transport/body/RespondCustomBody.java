package org.wa.common.transport.body;

import org.wa.common.exception.remoting.RemotingCommonCustomException;

/**
 * @Auther: XF
 * @Date: 2018/10/7 00:06
 * @Description:
 */
public class RespondCustomBody implements  CommonCustomBody{



    private Object result;
    private String error;
    private boolean succeed;

    public RespondCustomBody() {
    }

    public RespondCustomBody(Object result, String error, boolean succeed) {
        this.result = result;
        this.error = error;
        this.succeed = succeed;
    }

    @Override
    public void checkFields() throws RemotingCommonCustomException {

    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isSucceed() {
        return succeed;
    }

    public void setSucceed(boolean succeed) {
        this.succeed = succeed;
    }
}
