package top.uhyils.usher.rpc.spring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.util.StringUtils;
import top.uhyils.usher.rpc.annotation.RpcService;
import top.uhyils.usher.rpc.cluster.Cluster;
import top.uhyils.usher.rpc.cluster.ClusterFactory;
import top.uhyils.usher.rpc.config.RpcConfig;
import top.uhyils.usher.rpc.config.RpcConfigFactory;
import top.uhyils.usher.rpc.exception.RpcException;
import top.uhyils.usher.rpc.registry.ProviderRegistry;
import top.uhyils.usher.rpc.registry.RegistryFactory;
import top.uhyils.usher.rpc.spring.util.ClassUtil;
import top.uhyils.usher.util.LogUtil;

/**
 * 初始化RPC需要的一些东西
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2021年01月15日 16时12分
 */
@Configuration
public class RpcAutoConfiguration implements BeanFactoryAware, ApplicationContextAware {

    /**
     * bean name
     */
    private static final String RPC_CONFIGURER = "rpcConfigurer";

    private BeanFactory beanFactory;

    private ApplicationContext applicationContext;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }


    /**
     * 初始化扫描器
     *
     * @return
     */
    @Bean(RPC_CONFIGURER)
    public static RpcConfigurer createRpcConfigurer(BeanFactory beanFactory) {
        // 获取所有的扫描包(spring自带)
        List<String> packages = AutoConfigurationPackages.get(beanFactory);
        RpcConfigurer rpcConfigurer = new RpcConfigurer();
        rpcConfigurer.setBasePackage(StringUtils.collectionToCommaDelimitedString(packages));
        rpcConfigurer.setAnnotationClass(RpcService.class);
        return rpcConfigurer;
    }

    /**
     * 单独初始化这个类的生产者的Cluster
     *
     * @return
     */
    @Bean("providerCluster")
    @DependsOn({"rpcConfig", RPC_CONFIGURER})
    public Cluster createProviderCluster() {
        // 获取配置
        RpcConfig instance = RpcConfigFactory.getInstance();
        // 如果配置不加载服务生产者
        if (!instance.getProvider().isEnable()) {
            return null;
        }
        LogUtil.info("Provider Cluster init!!");

        // 获取指定注解的已经加载为bean的值
        Map<String, Object> springBeans = applicationContext.getBeansWithAnnotation(RpcService.class);
        Map<String, Object> beans = new HashMap<>(springBeans.size());
        // 遍历,获取实际的类,拿掉外层的AOP或代理类的外壳,获取第一个实例的接口父类
        for (Map.Entry<String, Object> entity : springBeans.entrySet()) {
            Object value = entity.getValue();
            Class<?> clazz = null;
            try {
                clazz = ClassUtil.getRealClass(value);
            } catch (Exception e) {
                throw new RpcException(e);
            }
            if (!clazz.isInterface()) {
                Class<?>[] interfaces = clazz.getInterfaces();
                clazz = interfaces[0];
            }
            beans.put(clazz.getName(), value);
        }
        Cluster providerCluster = null;
        try {
            /*
             * 创建生产者 负载均衡器(包含了底层的netty) 注意,正常的代理层下一层为注册层 , 注册层下一步才是负载均衡层,这里初始化单独初始化负载均衡器的原因为:
             * 1.初始化需要获取spring中的bean,需要spring包的东西,在正常的注册层并没有引入spring包
             * 2.生产者的注册机制是每一个接口都要注册一次,所以需要注册多遍,但是底层的netty只需要一个,所以在此加载负载均衡层
             */
            providerCluster = ClusterFactory.createDefaultProviderCluster(instance.getProvider().getPort(), beans);
        } catch (Exception e) {
            LogUtil.error(this, e);
        }
        // 创建生产者的registries
        try {
            for (Map.Entry<String, Object> entry : beans.entrySet()) {
                Object bean = entry.getValue();
                Class<?> clazz = ClassUtil.getRealClass(bean);
                if (!clazz.isInterface()) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    clazz = interfaces[0];
                }
                ProviderRegistry provider = RegistryFactory.createProvider(clazz, beans.get(clazz.getName()));

            }
        } catch (Exception e) {
            LogUtil.error(this, e);
        }
        return providerCluster;
    }

    /**
     * bean 注入consumer注解扫描
     *
     * @return
     */
    @Bean("top.uhyils.usher.rpc.spring.RpcConsumerBeanFieldInjectConfiguration")
    @DependsOn("rpcConfig")
    public static RpcConsumerBeanFieldInjectConfiguration createRpcConsumerBeanFieldInjectConfiguration() {
        return new RpcConsumerBeanFieldInjectConfiguration();
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
