package org.wa.registry;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static org.wa.common.utils.RandomImpl.getRandomElement;

/**
 * @Auther: XF
 * @Date: 2018/10/6 13:06
 * @Description: 和zk交互的方法
 * zk结构：
 * 所有服务名放在/rootdir
 * 例如：/rootdir/service1
 * /service2
 * ....
 * 在对应serviceX目录下，以子节点的形式放服务提供者的地址
 */
public class ZKRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ZKRegistry.class);

    private boolean isStart;

    private ZKServiceConfig config;

    private ZooKeeper zk;

    private HashMap<String, Set<String>> serviceTable = new HashMap<>();

    public ZKRegistry(){
    }

    public ZKRegistry(ZKServiceConfig config) {
        this.config = config;
    }

    public void start(){
        if(config==null)
            config=new ZKServiceConfig();
        connectZK();
        createRootNodeIfNotExist();
        watchRootNode();
        isStart=true;
    }

    private void connectZK() {
        final CountDownLatch latch = new CountDownLatch(1);
        try {
            if (zk == null || !zk.getState().isAlive()) {
                zk = new ZooKeeper(config.getAddr(), config.getZkSessionTimeout(), new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        latch.countDown();
                    }
                });
                latch.await();
            }
        } catch (IOException | InterruptedException e) {
            logger.error("connect to zk exception,", e.getMessage());
            throw new RuntimeException("failed to connect zk");
        }
    }

    private void createRootNodeIfNotExist() {
        try {
            Stat stat = zk.exists(config.getRootPath(), false);
            if (stat == null) {
                zk.create(config.getRootPath(), new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException | InterruptedException e) {
            logger.error("crate root node on zk exception,", e.getMessage());
            throw new RuntimeException("fail to create root node on zk");
        }
    }

    /**
     * 发现所有的服务,开始监听root节点
     */
    private synchronized void watchRootNode() {
        List<String> serviceNames = null;
        try {
            serviceNames = zk.getChildren(config.getRootPath(), new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    watchRootNode();
                }
            });
        } catch (KeeperException | InterruptedException e) {
            logger.warn("watch root node exception,", e.getMessage());
            return;
        }

        //将zk返回的service列表放进HashSet，方便查找
        Set<String> curServiceName = new HashSet<>(serviceNames);

        //找出并且移除被删除的service
        for (String key : serviceTable.keySet()) {
            if (!curServiceName.contains(key)) {
                serviceTable.remove(key);
                logger.info("service [{}] was deleted.", key);
            }
        }

        //找出新添加的service，并且注册监听器
        for (String serviceName : serviceNames) {
            if (!serviceTable.containsKey(serviceName)) {
                serviceTable.put(serviceName, new HashSet<String>());
                logger.info("service [{}] was created.", serviceName);
                //注册监听器
                watchServiceNode(serviceName);
            }
        }
    }

    /**
     * 监听service节点
     *
     * @param serviceName
     */
    private synchronized void watchServiceNode(final String serviceName) {

        String servicePath = config.getRootPath() + "/" + serviceName;

        List<String> providerAddrs = null;
        try {
            providerAddrs = zk.getChildren(servicePath, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    watchServiceNode(serviceName);
                }
            });
        } catch (KeeperException | InterruptedException e) {
            logger.warn("watch service node exception,maybe its parent changed", e.getMessage());
            return;
        }

        Set<String> curProviders = new HashSet<>(providerAddrs);

        Set<String> oldProviders = serviceTable.get(serviceName);

        //找出并且移除下线的provider
        for (String oldProvider : oldProviders) {
            if (!curProviders.contains(oldProvider)) {
                oldProviders.remove(oldProvider);
                logger.info("provide [{}] offline.", oldProvider);
            }
        }

        //添加新的provide
        for (String providerAddr : providerAddrs) {
            if (!oldProviders.contains(providerAddr)) {
                oldProviders.add(providerAddr);
                logger.info("provide [{}] online.", providerAddr);
            }
        }

    }

    /**
     * @param serviceName 服务名
     * @return 随机返回一个服务名对应的provider
     */
    public synchronized String getProviderAddr(String serviceName) {
        Set<String> serviceProviderAddrs = serviceTable.get(serviceName);
        if(serviceProviderAddrs.isEmpty())
            return null;
        //随机获取一个地址
        return getRandomElement(serviceProviderAddrs);
    }

    /**
     * @param serviceName 服务名
     * @return 返回整个可用的Provider列表
     */
    public synchronized List<String> getProviderList(String serviceName) {
        return new ArrayList<>(serviceTable.get(serviceName));
    }

    /**
     * 向zk注册生产者信息，如果服务名没创建会自动创建
     * @param serviceName 服务名称
     * @param addr        Provider地址,eg:"192.168.66.2:8088"
     * @return 只有当创建节点不发生异常时返回true
     */
    public boolean registerProvider(String serviceName, String addr) {

        String parentPath = config.getRootPath()+"/"+serviceName;

        String path = config.getRootPath() + "/" + serviceName + "/" + addr;

        try {
            //先注册父节点
            Stat stat = zk.exists(parentPath, false);
            if(stat==null){
                registerService(serviceName);
            }
            zk.create(path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch (KeeperException | InterruptedException e) {
            logger.error("create zk node [{}] error [{}]", path, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 创建服务名信息
     * @param serviceName 服务名
     * @return 只有当创建节点不发生异常时返回true
     */
    public boolean registerService(String serviceName){
        String path = config.getRootPath()+"/"+serviceName;
        try {
            zk.create(path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException | InterruptedException e) {
            logger.error("create zk node [{}] error [{}]", path, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 关闭与zk的连接
     */
    public void stop(){
        try {
            if(zk!=null)
                zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        isStart=false;
    }

    public boolean isStart() {
        return isStart;
    }
}
