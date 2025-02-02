package top.uhyils.usher.rpc.cluster;

import java.util.Map;
import top.uhyils.usher.rpc.config.RpcConfigFactory;
import top.uhyils.usher.rpc.netty.pojo.NettyInitDto;
import top.uhyils.usher.rpc.spi.RpcSpiManager;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2020年12月25日 18时50分
 */
public class ClusterFactory {

    private static final String PROVIDER_DEFAULT_NAME = "default_provider_name";

    private static final String PROVIDER_SPI_NAME = "providerSpiName";

    private static final String CONSUMER_DEFAULT_NAME = "default_consumer_name";

    private static final String CONSUMER_SPI_NAME = "consumerSpiName";

    private ClusterFactory() {
    }

    /**
     * 如果在同一个服务中,那么共用同一个ProviderCluster
     *
     * @param port
     * @param beans
     *
     * @return
     */
    public static Cluster createDefaultProviderCluster(Integer port, Map<String, Object> beans) throws InterruptedException {
        // spi 获取消费者的注册者信息
        String registryName = (String) RpcConfigFactory.getCustomOrDefault(PROVIDER_SPI_NAME, PROVIDER_DEFAULT_NAME);
        // 返回一个构造完成的消费者
        return (Cluster) RpcSpiManager.createOrGetExtensionByClass(Cluster.class, registryName, port, beans);
    }

    public static Cluster createDefaultConsumerCluster(Class<?> clazz, NettyInitDto nettyInit) throws InterruptedException {
        return createDefaultConsumerCluster(clazz, new NettyInitDto[]{nettyInit});
    }

    /**
     * 创建一个consumer端的cluster
     *
     * @param clazz      要使用的class
     * @param nettyInits netty 初始化需要的东西
     *
     * @return
     */
    public static Cluster createDefaultConsumerCluster(Class<?> clazz, NettyInitDto... nettyInits) throws InterruptedException {
        // spi 获取消费者的注册者信息
        String registryName = (String) RpcConfigFactory.getCustomOrDefault(CONSUMER_SPI_NAME, CONSUMER_DEFAULT_NAME);
        // 返回一个构造完成的消费者
        return (Cluster) RpcSpiManager.createOrGetExtensionByClass(Cluster.class, registryName, clazz, nettyInits);
    }


}
