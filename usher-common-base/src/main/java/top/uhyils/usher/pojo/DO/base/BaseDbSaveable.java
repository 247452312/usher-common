package top.uhyils.usher.pojo.DO.base;

import java.io.Serializable;
import top.uhyils.usher.pojo.DTO.UserDTO;
import top.uhyils.usher.pojo.cqe.DefaultCQE;

/**
 * 什么都不干, 标记这是一个在数据库中有对应关系的类 其他数据库基类应该全部继承这个类
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2020年06月23日 14时19分
 */
public interface BaseDbSaveable extends Serializable {

    /**
     * 插入前时使用的方法
     *
     * @param request 入参
     */
    void preInsert(DefaultCQE request);

    /**
     * 插入前时使用的方法
     *
     * @param userDO 用户
     */
    void preInsert(UserDTO userDO);

    /**
     * 插入前时使用的方法(使用系统管理账号)
     */
    void preInsert();

    /**
     * 修改前时使用的方法
     *
     * @param request 请求
     */
    void preUpdate(DefaultCQE request);

    /**
     * 修改前时使用的方法
     *
     * @param userDO 用户
     */
    void preUpdate(UserDTO userDO);

    /**
     * 修改前时使用的方法
     */
    void preUpdate();
}
