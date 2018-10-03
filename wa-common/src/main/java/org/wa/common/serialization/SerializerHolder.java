package org.wa.common.serialization;

import org.wa.common.spi.BaseServiceLoader;

/**
 * @Auther: XF
 * @Date: 2018/10/3 19:43
 * @Description:
 */
public final class SerializerHolder {
    //SPI
    private static final Serializer serializer = BaseServiceLoader.load(Serializer.class);
    public static Serializer serializerImpl(){
        return serializer;
    }
}
