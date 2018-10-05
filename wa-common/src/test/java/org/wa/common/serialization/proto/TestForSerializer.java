package org.wa.common.serialization.proto;

import org.wa.common.serialization.Serializer;
import org.wa.common.serialization.SerializerHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: XF
 * @Date: 2018/10/3 23:20
 * @Description: 测试串行化器
 */
public class TestForSerializer {
    public static class BigObject{
        private  final List<String> text = new ArrayList<>(1000);
        {
             for (int i = 0; i < 1000; i++) {
                 text.add(Math.random()+"\n");
             }
        }
        public void print(){
            for (int i = 0; i < 100; i++) {
                System.out.print(text.get(i));
            }
        }
    }

    public static void main(String[] args) {
        BigObject bigObject = new BigObject();
        byte[] bytes = SerializerHolder.serializerImpl().writeObject(bigObject);
        System.out.println(bytes.length);
        BigObject bigObject1 = SerializerHolder.serializerImpl().readObject(bytes, BigObject.class);
        bigObject1.print();
    }
}
