package org.wa.common.serialization.proto;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.wa.common.serialization.Serializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: XF
 * @Date: 2018/10/3 20:23
 * @Description: 使用protoStuff序列化
 * 序列化的对象不需要实现java.io.Serializable 也不需要有默认的构造函数
 */
public class ProtoStuffSerializer implements Serializer {

    private static Map<Class<?>,Schema<?>> cachedSchema = new ConcurrentHashMap<Class<?>, Schema<?>>();

    private static Objenesis objenesis = new ObjenesisStd(true);

    @Override
    public <T> byte[] writeObject(T object) {
        Class<T> cls = (Class<T>) object.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try{
            Schema<T> schema = getSchema(cls);
            return ProtobufIOUtil.toByteArray(object,schema,buffer);
        }catch (Exception e){
            throw new IllegalStateException(e.getMessage(),e);
        }finally {
            buffer.clear();
        }
    }

    @Override
    public <T> T readObject(byte[] bytes, Class<T> clazz) {
        try{
            //创建一个对象，不需要构造器
            T message = objenesis.newInstance(clazz);
            Schema<T> schema = getSchema(clazz);
            ProtobufIOUtil.mergeFrom(bytes,message,schema);
            return message;
        }catch (Exception e){
            throw new IllegalStateException(e.getMessage(),e);
        }
    }

    private static <T> Schema<T> getSchema(Class<T> cls){
        //先在缓冲区查找
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if(schema==null){   //缓冲区找不到，根据Class对象获得
            schema = RuntimeSchema.createFrom(cls);
            cachedSchema.put(cls,schema);
        }
        return schema;
    }
}
