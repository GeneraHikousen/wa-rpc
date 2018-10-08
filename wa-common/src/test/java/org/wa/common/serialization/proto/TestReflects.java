package org.wa.common.serialization.proto;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: XF
 * @Date: 2018/10/7 14:16
 * @Description:
 */
public class TestReflects {
    public void fun1(List list){
        System.out.println("list");
    }



    public static void main(String[] args) {
        try {
            TestReflects testReflects = new TestReflects();
            Method fun1 = TestReflects.class.getMethod("fun1", ArrayList.class);
            fun1.invoke(testReflects,new ArrayList<>());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
