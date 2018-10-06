package org.wa.remoting.netty;

/**
 * @Auther: XF
 * @Date: 2018/10/5 12:35
 * @Description:
 */
public class NettyClientConfig {
    private int clientWorkerThreads = 4;
    private int ClientCallbackExecutorThreads = Runtime.getRuntime().availableProcessors();
    private long connectTimeout = 3000L;
    private long channelNotActiveInterval = 1000*60;

    //format host:port,host:port
    private String defaultAddress;

    //最大闲置时间（秒）
    private int clientChannelMaxIdleTimeSeconds = 120;

    private int clientSocketSndBufSize = -1;
    private int clientSocketRcvBufSize = -1;

    private int writeBufferLowWaterMark = -1;
    private int writeBufferHighWaterMark = -1;

    public int getClientWorkerThreads() {
        return clientWorkerThreads;
    }

    public int getClientCallbackExecutorThreads() {
        return ClientCallbackExecutorThreads;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public long getChannelNotActiveInterval() {
        return channelNotActiveInterval;
    }

    public String getDefaultAddress() {
        return defaultAddress;
    }

    public int getClientChannelMaxIdleTimeSeconds() {
        return clientChannelMaxIdleTimeSeconds;
    }

    public int getClientSocketSndBufSize() {
        return clientSocketSndBufSize;
    }

    public int getClientSocketRcvBufSize() {
        return clientSocketRcvBufSize;
    }

    public int getWriteBufferLowWaterMark() {
        return writeBufferLowWaterMark;
    }

    public int getWriteBufferHighWaterMark() {
        return writeBufferHighWaterMark;
    }

    public void setClientWorkerThreads(int clientWorkerThread) {
        this.clientWorkerThreads = clientWorkerThread;
    }

    public void setClientCallbackExecutorThreads(int clientCallbackExecutorThreads) {
        ClientCallbackExecutorThreads = clientCallbackExecutorThreads;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setChannelNotActiveInterval(long channelNotActiveInterval) {
        this.channelNotActiveInterval = channelNotActiveInterval;
    }

    public void setDefaultAddress(String defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public void setClientChannelMaxIdleTimeSeconds(int clientChannelMaxIdleTimeSeconds) {
        this.clientChannelMaxIdleTimeSeconds = clientChannelMaxIdleTimeSeconds;
    }

    public void setClientSocketSndBufSize(int clientSocketSndBufSize) {
        this.clientSocketSndBufSize = clientSocketSndBufSize;
    }

    public void setClientSocketRcvBufSize(int clientSocketRcvBufSize) {
        this.clientSocketRcvBufSize = clientSocketRcvBufSize;
    }

    public void setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        this.writeBufferLowWaterMark = writeBufferLowWaterMark;
    }

    public void setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        this.writeBufferHighWaterMark = writeBufferHighWaterMark;
    }
}
