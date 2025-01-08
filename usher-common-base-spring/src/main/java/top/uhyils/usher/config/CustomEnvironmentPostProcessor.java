package top.uhyils.usher.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月08日 09时31分
 */
public class CustomEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, String> environmentProperties = new HashMap<>();

        // 获取所有属性源中的属性
        environment.getPropertySources().forEach(propertySource -> {
            if (propertySource instanceof MapPropertySource) {
                MapPropertySource mapPropertySource = (MapPropertySource) propertySource;
                mapPropertySource.getSource().forEach((key, value) -> {
                    // 将所有值转换为字符串
                    String stringValue = String.valueOf(value);
                    environmentProperties.put(key, stringValue);
                });
            }
        });

        // 将属性添加到系统属性中
        environmentProperties.forEach(System::setProperty);
    }
}
