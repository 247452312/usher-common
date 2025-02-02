package top.uhyils.usher.pojo.DO.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.util.Objects;
import top.uhyils.usher.context.LoginInfoHelper;
import top.uhyils.usher.pojo.DTO.UserDTO;
import top.uhyils.usher.pojo.cqe.DefaultCQE;
import top.uhyils.usher.util.IdUtil;
import top.uhyils.usher.util.SpringUtil;

/**
 * 以id为主键的类都应该继承这个类
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2020年06月23日 14时23分
 */
public abstract class BaseIdDO implements BaseDbSaveable {

    /**
     * id 一定是uuid的格式
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 插入之前执行方法，需要手动调用
     */
    @Override
    public void preInsert(DefaultCQE request) {
        preInsert(request.getUser());
    }

    @Override
    public void preInsert(UserDTO userDO) {
        IdUtil bean = SpringUtil.getBean(IdUtil.class);
        id = bean.newId();
    }

    /**
     * 插入之前执行方法，需要手动调用
     */
    @Override
    public void preInsert() {
        preInsert(LoginInfoHelper.doGet());
    }

    @Override
    public void preUpdate(DefaultCQE request) {
        // 只有id的修改前没有方法,但是也要执行
    }

    @Override
    public void preUpdate(UserDTO userDO) {
    }

    @Override
    public void preUpdate() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return Boolean.TRUE;
        }
        if (o == null || getClass() != o.getClass()) {
            return Boolean.FALSE;
        }
        BaseIdDO that = (BaseIdDO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
