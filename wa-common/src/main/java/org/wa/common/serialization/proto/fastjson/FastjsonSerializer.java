package org.wa.common.serialization.proto.fastjson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.wa.common.serialization.Serializer;

/**
 * @Auther: XF
 * @Date: 2018/10/9 13:16
 * @Description:
 */
public class FastjsonSerializer implements Serializer {
    @Override
    public <T> byte[] writeObject(T object) {
        return JSON.toJSONBytes(object,SerializerFeature.SortField);
    }

    @Override
    public <T> T readObject(byte[] bytes, Class<T> clazz) {
        return JSON.parseObject(bytes,clazz, Feature.SortFeidFastMatch);
    }
}
