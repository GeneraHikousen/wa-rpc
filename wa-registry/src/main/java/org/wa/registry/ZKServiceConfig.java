package org.wa.registry;

/**
 * @Auther: XF
 * @Date: 2018/10/5 22:46
 * @Description: zk注册中心的配置
 */
public class ZKServiceConfig {

    /*eg: 127.0.0.1:2181*/
    private String addr;

    /*注册中心在zk的目录,默认是  {@link DEFAULT_ROOT_PATH}*/
    private String rootPath;

    private int zkSessionTimeout;

    private static final String DEFAULT_ADDR = "hadoop2:2181";
    private static final String DEFAULT_ROOT_PATH = "/rpcService";

    private static final int DEFAULT_ZK_SESSION_TIMEOUT = 5000;

    public ZKServiceConfig(){
        this(DEFAULT_ADDR,DEFAULT_ROOT_PATH);
    }

    public ZKServiceConfig(String addr){
        this(addr,DEFAULT_ROOT_PATH);
    }

    public ZKServiceConfig(String addr, String rootPath) {
        this.addr = addr;
        this.rootPath = rootPath;
        this.zkSessionTimeout = DEFAULT_ZK_SESSION_TIMEOUT;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public int getZkSessionTimeout() {
        return zkSessionTimeout;
    }

    public void setZkSessionTimeout(int zkSessionTimeout) {
        this.zkSessionTimeout = zkSessionTimeout;
    }

}
