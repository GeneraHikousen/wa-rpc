package org.wa.client.provider.model;

/**
 * @Auther: XF
 * @Date: 2018/10/7 18:08
 * @Description: 定义一个服务
 */
public class ServiceWrapper {

    /**
     * 服务的方法集合
     */
    private Class<?> interface_;

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

    public boolean isObjectSupport(){
        return isObject;
    }

    public ServiceWrapper(Class<?> interface_,Object object) {
        this.interface_ = interface_;
        this.isObject = true;
        this.object = object;
    }

    public ServiceWrapper(Class<?> interface_,Class<?> clazz) {
        this.isObject = false;
        this.clazz = clazz;
    }

    public Object getObject() {
        return object;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Class<?> getInterface_() {
        return interface_;
    }

    public void setInterface_(Class<?> interface_) {
        this.interface_ = interface_;
    }
}
