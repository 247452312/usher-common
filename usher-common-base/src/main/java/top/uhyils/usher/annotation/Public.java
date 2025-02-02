package top.uhyils.usher.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识是否是公开的接口 如果是,则不需要解析token来判断是否登录或者是否游客, 直接可以访问
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年06月20日 11时01分
 */
@Inherited  //可以被继承
@Retention(RetentionPolicy.RUNTIME) // 反射可以读取
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Public {

}
