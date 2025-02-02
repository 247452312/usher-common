package top.uhyils.usher.rpc.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import top.uhyils.usher.rpc.annotation.RpcSpi;
import top.uhyils.usher.rpc.annotation.UsherRpc;
import top.uhyils.usher.rpc.exception.RpcBeanNotFoundException;
import top.uhyils.usher.rpc.exception.RpcRunTimeException;
import top.uhyils.usher.rpc.util.PackageUtils;
import top.uhyils.usher.util.LogUtil;
import top.uhyils.usher.util.MapUtil;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2021年01月18日 11时20分
 */
public class RpcSpiManager {

    /**
     * 加载扩展点时的路径
     */
    private static final String RPC_EXTENSION_PATH = "META-INF/rpc/";

    /**
     * 加载扩展点时的扩展点保存地
     */
    private static final String RPC_EXTENSION_CLASS_PATH = "META-INF/rpcClass";

    /**
     * 已加载的所有扩展点类型
     * map<扩展点最终类,map<扩展点具体名称,扩展点实际类>>
     */
    private static Map<Class<?>, Map<String, Class<?>>> cacheClass = new HashMap<>();

    /**
     * 已加载的所有扩展点类型,缓存,注,此处list是有order的,所以在使用时用LinkedList  此处类似于热点缓存,经常被获取的才在此类中
     * Map<root.class : target.class , 扩展点实例>
     */
    private static Map<String, List<RpcSpiExtension>> hotExtensionCache = new HashMap<>();

    /**
     * cacheClass 对应的实例
     */
    private static Map<Class<? extends RpcSpiExtension>, Map<String, RpcSpiExtension>> cacheClassInstanceMap = new HashMap<>();

    /**
     * 记录是否初始化,原型模式只初始化一次
     */
    private static Map<Class<? extends RpcSpiExtension>, Map<String, Boolean>> init = new HashMap<>();


    static {
        initCacheClass();
    }

    /**
     * 此次加载的扩展点类型
     */
    private Class<?> type;

    public RpcSpiManager(Class<?> type) {
        this.type = type;
    }

    /**
     * 获取启动类名称
     *
     * @return
     */
    public static Class getMainClass() {
        StackTraceElement[] stackTraceElements = new RuntimeException().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if ("main".equals(stackTraceElement.getMethodName())) {
                try {
                    return Class.forName(stackTraceElement.getClassName());
                } catch (ClassNotFoundException e) {
                    LogUtil.error(RpcSpiManager.class, e);
                }
            }
        }
        return null;
    }

    /**
     * 创建时化时顺带初始化
     *
     * @param root
     * @param name
     * @param args
     * @param <T>
     *
     * @return
     */
    public static <T extends RpcSpiExtension> RpcSpiExtension createOrGetExtensionByClass(Class<T> root, String name, Object... args) throws InterruptedException {
        RpcSpiExtension extensionByClass = createOrGetExtensionByClassNoInit(root, name);

        RpcSpi annotation = extensionByClass.getClass().getAnnotation(RpcSpi.class);
        if (annotation != null) {
            boolean single = annotation.single();

            if (single) {
                /*单例的初始化过程*/
                Map<String, Boolean> stringBooleanMap = init.computeIfAbsent(root, k -> new HashMap<>());
                Boolean isInit = stringBooleanMap.computeIfAbsent(name, k -> Boolean.FALSE);

                if (Boolean.FALSE.equals(isInit)) {
                    extensionByClass.init(args);
                    stringBooleanMap.put(name, Boolean.TRUE);
                }
            } else {
                /*原型的初始化过程*/
                extensionByClass.init(args);
            }
        }
        return extensionByClass;
    }

    /**
     * 根据rpcClass中的类作为root,以目标class获取指定的所有类,根据order排序的linkList
     *
     * @param root        rpcClass中定义的类
     * @param targetClass 要获取的类
     *
     * @return
     */
    public static <T extends RpcSpiExtension, E extends T> List<E> createOrGetExtensionListByClassNoInit(Class<T> root, Class<E> targetClass, Object... args) {
        // 看看加载的类中是否有目标root
        if (!cacheClass.containsKey(root)) {
            throw new RpcBeanNotFoundException(root);
        }
        //缓存中的key,如果有 直接返回
        String cacheKey = root.getName() + " : " + targetClass.getName();
        if (hotExtensionCache.containsKey(cacheKey)) {
            List<RpcSpiExtension> objects = hotExtensionCache.get(cacheKey);
            return objects.stream().map(t -> (E) t).map(RpcSpiManager::checkSingleAndGetResult).filter(Objects::nonNull).collect(Collectors.toList());
        }

        /*以下代表 此类在spi中并且没有初始化过,或者初始化过 但是targetClass没有单独拿出来获取过*/

        // 初始化缓存,加速获取用
        Map<String, RpcSpiExtension> cacheMap = MapUtil.putIfAbsent(cacheClassInstanceMap, root, () -> new HashMap<>(cacheClass.get(root).size()), false);

        Map<String, Class<?>> classMap = cacheClass.get(root);
        ArrayList<String> list = new ArrayList<>(classMap.keySet());
        // 获取结果,注 这里面不是克隆的 而是原型模式中的原型 所以不需要clone
        List<RpcSpiExtension> result = list.stream()
                                           //将对应的转换为object 缓存
                                           .map(value -> {
                                               Class<?> clazz = classMap.get(value);
                                               // 筛选出所有rootClass中 targetClass的子类
                                               if (!targetClass.isAssignableFrom(clazz)) {
                                                   return null;
                                               }

                                               //从缓存中拿出已经有的,防止重复初始化
                                               if (cacheMap.containsKey(value)) {
                                                   return cacheMap.get(value);
                                               } else {
                                                   RpcSpiExtension instance = null;
                                                   Constructor<?> constructor;
                                                   try {
                                                       // 获取空的构造器
                                                       constructor = clazz.getConstructor();
                                                   } catch (Exception e) {
                                                       throw new IllegalStateException(root.getName() + " 必须要空构造器");
                                                   }

                                                   try {
                                                       // 初始化
                                                       instance = (RpcSpiExtension) constructor.newInstance();
                                                       instance.init(args);
                                                   } catch (Exception e) {
                                                       LogUtil.error(RpcSpiManager.class, e);
                                                   }
                                                   cacheMap.put(value, instance);
                                                   return instance;
                                               }
                                           })
                                           .filter(Objects::nonNull)
                                           .sorted(Comparator.comparingInt(value -> value.getClass().getAnnotation(RpcSpi.class).order())).collect(Collectors.toList());

        hotExtensionCache.put(cacheKey, result);

        return result.stream().map(t -> (E) t).map(RpcSpiManager::checkSingleAndGetResult).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 初始化cacheClass 只是把class加载进来,并不会初始化bean
     */
    private static void initCacheClass() {
        // 加载所有的需要扩展的扩展点(key是由rpcClass文件提供的类,value是对应文件中的类)
        List<Class> allRpcExtensionClass = loadAllRpcExtensionClass();
        if (allRpcExtensionClass == null) {
            return;
        }
        // 遍历所有扩展点类型
        for (Class value : allRpcExtensionClass) {
            RpcSpiManager rpcExtensionLoader = new RpcSpiManager(value);
            Map<String, Class<?>> load = rpcExtensionLoader.load();
            cacheClass.put(value, load);
        }

        // 扫描启动类下的myRpc注解 然后扫描此注解下的所有spi
        List<Class<?>> rpcSpiList = scanRunClassUsherRpcPackageSpiSubclass();
        /*将扫描出来的类分发到各个类里面去*/
        Set<Class<?>> rootClazz = cacheClass.keySet();
        // 遍历每个spi类
        for (Class<?> clazz : rpcSpiList) {
            // 查询是否有根类,如果没有,则不并入缓存中
            for (Class<?> rootClazzItem : rootClazz) {
                // 如果这个根类是指定类的父类,则加入
                if (rootClazzItem.isAssignableFrom(clazz)) {
                    Map<String, Class<?>> classMap = cacheClass.get(rootClazzItem);
                    //防重
                    if (!classMap.containsValue(clazz)) {
                        classMap.put(getBeanName(clazz), clazz);
                    }
                }
            }
        }
    }

    /**
     * 加载所有的需要扩展的扩展点
     *
     * @return
     */
    private static List<Class> loadAllRpcExtensionClass() {
        List<Class> result = new LinkedList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            // 从META-INF/rpcClass 这个文件中按行取出类
            Enumeration<URL> resources = classLoader.getResources(RPC_EXTENSION_CLASS_PATH);
            if (!resources.hasMoreElements()) {
                resources = ClassLoader.getSystemResources(RPC_EXTENSION_CLASS_PATH);
            }
            if (resources != null) {
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                        String className;
                        while ((className = br.readLine()) != null) {
                            // 删除注释
                            int i = className.indexOf("#");
                            if (i >= 0) {
                                className = className.substring(0, i);
                            }
                            className = className.trim();
                            if (className.length() > 0) {
                                Class<?> targetClass = Class.forName(className, true, classLoader);
                                if (RpcSpiExtension.class.isAssignableFrom(targetClass)) {
                                    result.add(targetClass);
                                } else {
                                    LogUtil.warn(targetClass.getName() + " 没有继承: " + RpcSpiExtension.class.getName());
                                }
                            }
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        LogUtil.error(e);
                    }
                }
            }
        } catch (IOException e) {
            LogUtil.error(RpcSpiManager.class, e);
            return new ArrayList<>();
        }
        return result;
    }

    /**
     * 扫描启动类下的myRpc注解 然后扫描此注解下的所有spi,加载进来
     */
    private static List<Class<?>> scanRunClassUsherRpcPackageSpiSubclass() {
        /*1.获取启动类*/
        /*2.获取启动类包下所有的@UsherRpc注解*/
        /*3.获取myRpc注解下所有的spi,除了手动排除的部分*/

        //获取启动类
        Class mainClass = getMainClass();
        if (mainClass == null) {
            return Collections.emptyList();
        }
        // 扫描main下的类
        Set<Class<?>> mainClassScanCLassNames = null;
        try {
            mainClassScanCLassNames = PackageUtils.getClassByPackageName(mainClass.getPackage().getName(), null, true);
        } catch (IOException | ClassNotFoundException e) {
            LogUtil.error(RpcSpiManager.class, e);
        }
        if (mainClassScanCLassNames == null) {
            return Collections.emptyList();
        }

        // 获取所有@UsherRpc注解
        List<Class<?>> classHaveUsherRpc = mainClassScanCLassNames.stream().filter(t -> t.getAnnotation(UsherRpc.class) != null).collect(Collectors.toList());

        /*扫描包的规则, exclude优先级最高,一定不会扫*/
        // 要扫描的包
        Set<String> packageName = new HashSet<>();
        // 不要扫描的包
        Set<String> excludePackageName = new HashSet<>();
        //  ...... 获取包.
        classHaveUsherRpc.forEach(t -> {
            UsherRpc usherRpc = t.getAnnotation(UsherRpc.class);
            if (usherRpc == null) {
                return;
            }
            String[] scanPackages = usherRpc.baseScanPackage();
            String[] excludePackages = usherRpc.excludePackage();
            String name = t.getPackage().getName();

            packageName.addAll(Arrays.asList(scanPackages));
            excludePackageName.addAll(Arrays.asList(excludePackages));
            packageName.add(name);
        });
        // 获取需要的扫描包
        Set<Class<?>> classByPackageName = PackageUtils.getClassByPackageName(packageName, excludePackageName, true);
        // 所有带有spi注解的类
        List<Class<?>> rpcSpiList = classByPackageName.stream().filter(t -> t.getAnnotation(RpcSpi.class) != null).collect(Collectors.toList());

        return rpcSpiList;
    }

    /**
     * 获取一种首字母小写的bean名称
     *
     * @param clazz
     *
     * @return
     */
    private static String getBeanName(Class<?> clazz) {
        RpcSpi annotation = clazz.getAnnotation(RpcSpi.class);
        if (annotation == null || StringUtils.isEmpty(annotation.name())) {
            String simpleName = clazz.getSimpleName();
            String first = simpleName.substring(0, 1);
            return simpleName.replaceFirst(first, first.toLowerCase(Locale.ROOT));
        }
        return annotation.name();


    }

    /**
     * 判断clazz是否是扩展点
     *
     * @param clazz
     *
     * @return
     */
    private static boolean haveSpi(Class<?> clazz) {
        return clazz.getAnnotation(RpcSpi.class) != null;
    }

    /**
     * 根据名称获取指定扩展点的类
     *
     * @param root 指定扩展点(rpcClass文件中的类)
     * @param name 名称,唯一
     *
     * @return 单例模式返回单例, 原型模式返回clone后的
     */
    private static <T extends RpcSpiExtension> RpcSpiExtension createOrGetExtensionByClassNoInit(Class<T> root, String name) {
        // 看看加载的类中是否有目标root
        if (!cacheClass.containsKey(root)) {
            throw new RpcRunTimeException("rpcSpi 没有类:[" + root.getName() + "] 请在META-INF/rpcClass中添加需要加载的类");
        }

        // 如果应该加载的地方没有此名称
        if (!cacheClass.get(root).containsKey(name)) {
            throw new RpcRunTimeException("rpcSpi 没有加载此扩展,请检查是否在 META-INF/rpc/" + root.getName() + " 文件中添加需要扩展的类,名称: " + name);
        }

        // 初始化缓存,加速获取用
        Map<String, RpcSpiExtension> cacheMap = MapUtil.putIfAbsent(cacheClassInstanceMap, root, () -> new HashMap<>(cacheClass.get(root).size()));

        // 查询之前缓存里有没有
        if (cacheMap.containsKey(name)) {
            return checkSingleAndGetResult(cacheMap.get(name));
        } else {
            synchronized (cacheMap) {
                // 双重检测,防止重复
                if (cacheMap.containsKey(name)) {
                    return checkSingleAndGetResult(cacheMap.get(name));
                }
                // 之前缓存里也没有 只能初始化一次了
                Class<?> clazz = cacheClass.get(root).get(name);
                Constructor<?> constructor;
                try {
                    // 获取空的构造器
                    constructor = clazz.getConstructor();
                } catch (Exception e) {
                    throw new IllegalStateException(root.getName() + " 必须要空构造器");
                }
                RpcSpiExtension instance = null;
                try {
                    // 初始化
                    instance = (RpcSpiExtension) constructor.newInstance();
                } catch (Exception e) {
                    LogUtil.error(RpcSpiManager.class, e);
                }
                //放入缓存
                cacheMap.put(name, instance);
                return checkSingleAndGetResult(instance);
            }
        }
    }

    /**
     * 扫描spi是否是单例,如果不是,则使用clone方法
     *
     * @param obj
     *
     * @return
     */
    private static <T extends RpcSpiExtension> T checkSingleAndGetResult(T obj) {
        RpcSpi annotation = obj.getClass().getAnnotation(RpcSpi.class);
        // 如果是单例模式,直接返回
        if (annotation.single()) {
            return obj;
        } else {
            //调用clone方法
            return (T) obj.rpcClone();
        }
    }

    public Map<String, Class<?>> load() {
        Map<String, Class<?>> extensions = new HashMap<>(16);
        loadDirs(extensions, RPC_EXTENSION_PATH, type.getName());
        return extensions;
    }

    /**
     * 读取执行resource所在的文件夹
     *
     * @param extensions
     * @param dir
     * @param name
     */
    private void loadDirs(Map<String, Class<?>> extensions, String dir, String name) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            String fileName = dir + name;
            Enumeration<URL> resources = classLoader.getResources(fileName);
            if (!resources.hasMoreElements()) {
                resources = ClassLoader.getSystemResources(fileName);
            }
            if (resources != null) {
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    loadResources(extensions, classLoader, url);
                }
            }
        } catch (IOException e) {
            LogUtil.error(e, "找不到资源文件" + dir + name);
        }
    }

    /**
     * 加载每一个resource
     *
     * @param extensions
     * @param classLoader
     * @param url
     */
    private void loadResources(Map<String, Class<?>> extensions, ClassLoader classLoader, URL url) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                int i = line.indexOf("#");
                if (i >= 0) {
                    line = line.substring(0, i);
                }
                line = line.trim();
                if (line.length() > 0) {
                    String name = null;
                    int j = line.indexOf("=");
                    if (j >= 0) {
                        name = line.substring(0, j).trim();
                        line = line.substring(j + 1).trim();
                    }
                    if (line.length() > 0) {
                        loadClass(extensions, Class.forName(line, true, classLoader), name);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            LogUtil.error(e);
        }
    }

    /**
     * 加载resource中的每一个class
     *
     * @param extensions
     * @param clazz
     * @param name
     */
    private void loadClass(Map<String, Class<?>> extensions, Class<?> clazz, String name) {
        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException(clazz.getName() + " 不是 " + type.getName());
        }
        if (haveSpi(clazz)) {
            // 要注入了
            if (name == null) {
                name = findClassName(clazz);
                if (StringUtils.isEmpty(name)) {
                    throw new IllegalStateException("读取扩展配置文件时找不到name:" + clazz.getName());
                }
            }
            extensions.put(name, clazz);
        }
    }

    /**
     * 查询className
     *
     * @param clazz
     *
     * @return
     */
    private String findClassName(Class<?> clazz) {
        RpcSpi annotation = clazz.getAnnotation(RpcSpi.class);
        if (annotation == null || StringUtils.isEmpty(annotation.name())) {
            String name = clazz.getSimpleName();
            if (name.endsWith(type.getSimpleName())) {
                name = name.substring(0, name.length() - type.getSimpleName().length());
            }
            return name.toLowerCase();
        }
        return annotation.name();
    }


}
