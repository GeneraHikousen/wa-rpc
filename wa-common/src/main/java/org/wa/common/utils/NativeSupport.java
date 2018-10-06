package org.wa.common.utils;
import io.netty.channel.epoll.Native;
/**
 * @Auther: XF
 * @Date: 2018/10/5 13:20
 * @Description:
 */
public class NativeSupport {
    private static final boolean SUPPORT_NATIVE_ET;

    static {
        boolean epoll = false;
        try{
            Class.forName("io.netty.channel.epoll.Native");
            epoll = true;
        }catch (Throwable e){
            epoll=false;
        }
        SUPPORT_NATIVE_ET = epoll;
    }

    /**
     * The native socket transport for Linux using JNI.
     */
    public static boolean isSupportNativeET() {
        return SUPPORT_NATIVE_ET;
    }
}
