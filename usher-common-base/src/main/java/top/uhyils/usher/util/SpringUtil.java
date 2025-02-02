/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.uhyils.usher.util;


import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import top.uhyils.usher.annotation.NotNull;
import top.uhyils.usher.annotation.Nullable;

/**
 * 存储spring上下文缓存的地方
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2020年04月27日 16时46分
 */
public class SpringUtil implements ApplicationContextInitializer, ApplicationListener<ContextRefreshedEvent> {

    private volatile static Boolean atomicBoolean = Boolean.FALSE;

    private static ApplicationContext applicationContext = null;

    /**
     * 根据指定的annotation获取beans
     *
     * @param clazz
     * @param <T>
     *
     * @return
     */
    public static <T extends Annotation> Map<String, Object> getBeansWithAnnotation(Class<T> clazz) {
        return applicationContext.getBeansWithAnnotation(clazz);
    }

    /**
     * 判断spring是否初始化
     *
     * @return
     */
    public static boolean isNotStart() {
        return !isStart();
    }

    /**
     * 判断spring是否初始化
     *
     * @return
     */
    public static boolean isStart() {
        if (applicationContext == null) {
            return false;
        }
        return atomicBoolean;
    }

    /**
     * 通过name获取 Bean.
     *
     * @param name bean名称
     *
     * @return bean
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);

    }

    /**
     * 获取applicationContext
     *
     * @return applicationContext
     */
    public static ApplicationContext getApplicationContext() {
        if (applicationContext != null) {
            return applicationContext;
        }
        return (ApplicationContext) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{ApplicationContext.class}, (proxy, method, args) -> null);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     *
     * @param name  bean名称
     * @param clazz class
     * @param <T>   类型
     *
     * @return 对应的bean
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    /**
     * 通过key 获取环境变量
     *
     * @param key 环境变量的key
     *
     * @return 环境变量的值
     */
    @Nullable
    public static String getProperty(String key) {
        Environment environment = getApplicationContext().getEnvironment();
        if (environment != null) {
            return environment.getProperty(key);
        }
        return null;
    }

    /**
     * 通过key 至少获取一个环境变量
     * 规则:
     * 通过给定的key值数组,从前到后获取,直到获取到第一个值为止,如果均未获取到,则返回null
     *
     * @param keys 环境变量的key合集
     *
     * @return 环境变量的值
     */
    @Nullable
    public static String getProperty(String[] keys) {
        Environment environment = getApplicationContext().getEnvironment();
        if (environment != null) {
            for (String key : keys) {
                String property = environment.getProperty(key, "");
                if (StringUtils.isNotEmpty(property)) {
                    return property;
                }
            }
        }
        return null;
    }

    /**
     * 通过key 获取环境变量
     *
     * @param key          环境变量的key
     * @param defaultValue 默认值
     *
     * @return 环境变量的值
     */
    @NotNull
    public static String getProperty(String key, String defaultValue) {
        Environment environment = getApplicationContext().getEnvironment();
        if (environment != null) {
            return environment.getProperty(key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * 通过key 查询是否存在bean
     *
     * @param beanName bean名称
     *
     * @return 是否存在
     */
    public static Boolean containsBean(String beanName) {
        return getApplicationContext().containsBean(beanName);
    }

    /**
     * 通过key 查询是否存在bean
     *
     * @param beanClass bean名称
     *
     * @return 是否存在
     */
    public static <T> Boolean containsBean(Class<T> beanClass) {
        try {
            getBean(beanClass);
            return Boolean.TRUE;
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }

    /**
     * 通过class获取Bean.
     *
     * @param clazz class
     * @param <T>   类型
     *
     * @return 对应类型的bean
     */
    @Nullable
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 根据注解获取
     *
     * @param annotationClass
     */
    public static Map<String, Object> getByAnnotation(Class<? extends Annotation> annotationClass) {
        return applicationContext.getBeansWithAnnotation(annotationClass);

    }

    public static <T> List<T> getBeans(Class<T> clazz) {
        String[] beanNamesForType = applicationContext.getBeanNamesForType(clazz);
        List<T> result = new ArrayList<>(beanNamesForType.length);
        for (String beanName : beanNamesForType) {
            T bean = applicationContext.getBean(beanName, clazz);
            result.add(bean);
        }
        return result;
    }


    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (SpringUtil.applicationContext == null) {
            LogUtil.info(SpringUtil.class, "set applicationContext");
            SpringUtil.applicationContext = applicationContext;
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        atomicBoolean = true;
    }
}
