package top.uhyils.usher.config;

import java.lang.annotation.Annotation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月08日 08时54分
 */
public class ScanConfigurer implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {
    /**
     * 要扫描的类
     */
    private Class<? extends Annotation> annotationClass;

    /**
     * 公共父类
     */
    private Class<?> superInterface;

    /**
     * spring上下文
     */
    private ApplicationContext applicationContext;

    /**
     * 加载时的基本类
     */
    private String basePackage;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        SpringBeanScanner scanner = new SpringBeanScanner(registry);
        scanner.setAnnotationClass(this.annotationClass);
        scanner.setSuperInterface(this.superInterface);
        scanner.setResourceLoader(this.applicationContext);
        // 默认为方法扫描
        scanner.setBeanNameGenerator(null);
        scanner.registerFilters();
        scanner.scan(
            StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //无
    }

    public Class<? extends Annotation> getAnnotationClass() {
        return annotationClass;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public Class<?> getSuperInterface() {
        return superInterface;
    }

    public void setSuperInterface(Class<?> superInterface) {
        this.superInterface = superInterface;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }
}
