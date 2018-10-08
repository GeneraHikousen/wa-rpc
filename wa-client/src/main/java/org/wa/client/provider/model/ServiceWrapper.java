package org.wa.client.provider.model;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.List;

/**
 * @Auther: XF
 * @Date: 2018/10/7 18:08
 * @Description: 定义一个服务
 */
public class ServiceWrapper {

    /**
     * 是否提供已存在的对象
     */
    private boolean isObject;

    /**
     * 提供服务的对象
     */
    private Object object;

    /**
     * 如果不提供已经存在的对象，至少需要有类信息
     */
    private Class<?> clazz;

    private String methodName;

    /**
     * 入参列表
     */
    private List<Class<?>[]> parameters;

    public boolean isObjectSupport(){
        return isObject;
    }

    public ServiceWrapper(Object object, String methodName, List<Class<?>[]> parameters) {
        this.isObject = true;
        this.object = object;
        this.methodName = methodName;
        this.parameters = parameters;
    }

    public ServiceWrapper(Class<?> clazz, String methodName, List<Class<?>[]> parameters) {
        this.isObject = false;
        this.clazz = clazz;
        this.methodName = methodName;
        this.parameters = parameters;
    }

    public Object getObject() {
        return object;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<Class<?>[]> getParameters() {
        return parameters;
    }
}
