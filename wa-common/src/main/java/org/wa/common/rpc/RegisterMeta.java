package org.wa.common.rpc;

import io.netty.channel.Channel;
import org.wa.common.transport.body.PublishServiceCustomBody;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @Auther: XF
 * @Date: 2018/10/5 14:07
 * @Description:
 */
public class RegisterMeta {

    private Address address = new Address();

    private String serviceName;

    //是否该服务是VIP服务，如果是，走特定通道
    private boolean isVIPService;

    // 是否支持服务降级
    private boolean isSupportDegradeService;

    // 降级服务的mock方法的路径
    private String degradeServicePath;
    // 降级服务的描述
    private String degradeServiceDesc;
    // 服务的权重
    private volatile int weight;
    // 建议连接数 hashCode()与equals()不把connCount计算在内
    private volatile int connCount;

    private ServiceReviewState isReviewed = ServiceReviewState.HAS_NOT_REVIEWED;

    private boolean hasDegradeService = false;


    public RegisterMeta(Address address, String serviceName,boolean isVIPService, boolean isSupportDegradeService, String degradeServicePath,
                        String degradeServiceDesc, int weight, int connCount) {
        this.address = address;
        this.serviceName = serviceName;
        this.isVIPService = isVIPService;
        this.isSupportDegradeService = isSupportDegradeService;
        this.degradeServicePath = degradeServicePath;
        this.degradeServiceDesc = degradeServiceDesc;
        this.weight = weight;
        this.connCount = connCount;
    }

    public static class Address{

        private String host;

        private int port;

        public Address() {
        }

        public Address(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public void setPort(int port) {
            this.port = port;
        }

        @Override
        public int hashCode() {
            int result = host != null ? host.hashCode() : 0;
            result = 31 * result + port;
            return result;
        }

        @Override
        public String toString() {
            return "Address{" + "host='" + host + '\'' + ", port=" + port + '}';
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        RegisterMeta that = (RegisterMeta) obj;

        return !(address != null ? !address.equals(that.address) : that.address != null)
                && !(serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null);

    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RegisterMeta [address=" + address + ", serviceName=" + serviceName + ", isVIPService=" + isVIPService + ", isSupportDegradeService="
                + isSupportDegradeService + ", degradeServicePath=" + degradeServicePath + ", degradeServiceDesc=" + degradeServiceDesc + ", weight=" + weight
                + ", connCount=" + connCount + ", isReviewed=" + isReviewed + ", hasDegradeService=" + hasDegradeService + "]";
    }

    public static RegisterMeta createRegiserMeta(PublishServiceCustomBody publishServiceCustomBody, Channel channel) {

        if(publishServiceCustomBody.getHost() == null ||publishServiceCustomBody.getHost().length() == 0){
            SocketAddress address = channel.remoteAddress();
            if (address instanceof InetSocketAddress) {
                publishServiceCustomBody.setHost(((InetSocketAddress) address).getAddress().getHostAddress());
            }
        }

        Address address = new Address(publishServiceCustomBody.getHost(),
                publishServiceCustomBody.getPort());

        RegisterMeta registerMeta = new RegisterMeta(address,publishServiceCustomBody.getServiceProviderName(),
                publishServiceCustomBody.isVIPService(),
                publishServiceCustomBody.isSupportDegradeService(),
                publishServiceCustomBody.getDegradeServicePath(),
                publishServiceCustomBody.getDegradeServiceDesc(),
                publishServiceCustomBody.getWeight(),
                publishServiceCustomBody.getConnCount()
        );
        return registerMeta;
    }
}
