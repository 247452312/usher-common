package top.uhyils.usher.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2021年12月13日 08时57分
 */
@Documented
@Target(value = {ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Nullable {

    /**
     * 说明, 可以理解为备注
     *
     * @return
     */
    String value() default "";
}
