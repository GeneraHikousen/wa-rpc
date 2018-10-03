package org.wa.common.spi;

import java.util.ServiceLoader;

/**
 * @Auther: XF
 * @Date: 2018/10/3 20:02
 * @Description:
 */
public class BaseServiceLoader {
    public static <S> S load (Class<S> serviceClass){
        return ServiceLoader.load(serviceClass).iterator().next();
    }
}
