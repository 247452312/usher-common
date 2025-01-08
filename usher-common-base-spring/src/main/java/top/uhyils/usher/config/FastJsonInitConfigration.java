package top.uhyils.usher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月08日 09时19分
 */
@Configuration
public class FastJsonInitConfigration {


    @Bean
    public FastJsonConfig fastJsonConfig() {
        return new FastJsonConfig();
    }

}
