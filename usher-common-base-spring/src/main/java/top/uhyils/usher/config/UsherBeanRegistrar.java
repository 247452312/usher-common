package top.uhyils.usher.config;

import java.util.List;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import top.uhyils.usher.annotation.Convert;
import top.uhyils.usher.annotation.Facade;
import top.uhyils.usher.annotation.Repository;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月08日 08时53分
 */
@Configuration
public class UsherBeanRegistrar {

    public static final String FACADE_CONFIGURER = "facadeConfigurer";

    public static final String CONVERT_CONFIGURER = "convertConfigurer";

    public static final String REPOSITORY_CONFIGURER = "repositoryConfigurer";

    /**
     * 初始化扫描器
     *
     * @return
     */
    @Bean(FACADE_CONFIGURER)
    public static ScanConfigurer createFacadeConfigurer(BeanFactory beanFactory) {
        // 获取所有的扫描包(spring自带)
        List<String> packages = AutoConfigurationPackages.get(beanFactory);
        ScanConfigurer rpcConfigurer = new ScanConfigurer();
        rpcConfigurer.setBasePackage(StringUtils.collectionToCommaDelimitedString(packages));
        rpcConfigurer.setAnnotationClass(Facade.class);
        return rpcConfigurer;
    }

    /**
     * 初始化扫描器
     *
     * @return
     */
    @Bean(CONVERT_CONFIGURER)
    public static ScanConfigurer createConvertConfigurer(BeanFactory beanFactory) {
        // 获取所有的扫描包(spring自带)
        List<String> packages = AutoConfigurationPackages.get(beanFactory);
        ScanConfigurer rpcConfigurer = new ScanConfigurer();
        rpcConfigurer.setBasePackage(StringUtils.collectionToCommaDelimitedString(packages));
        rpcConfigurer.setAnnotationClass(Convert.class);
        return rpcConfigurer;
    }

    /**
     * 初始化扫描器
     *
     * @return
     */
    @Bean(REPOSITORY_CONFIGURER)
    public static ScanConfigurer createRepositoryConfigurer(BeanFactory beanFactory) {
        // 获取所有的扫描包(spring自带)
        List<String> packages = AutoConfigurationPackages.get(beanFactory);
        ScanConfigurer rpcConfigurer = new ScanConfigurer();
        rpcConfigurer.setBasePackage(StringUtils.collectionToCommaDelimitedString(packages));
        rpcConfigurer.setAnnotationClass(Repository.class);
        return rpcConfigurer;
    }
}
