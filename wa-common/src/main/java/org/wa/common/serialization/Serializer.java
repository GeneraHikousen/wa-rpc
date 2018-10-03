package org.wa.common.serialization;

/**
 * @Auther: XF
 * @Date: 2018/10/3 19:46
 * @Description:
 */
public interface Serializer {
    //将对象序列化成字节数组
    <T> byte[] writeObject(T object);

    //将byte[]数组反序列化成对象
    <T> T readObject(byte[] bytes,Class<T> clazz);

}
