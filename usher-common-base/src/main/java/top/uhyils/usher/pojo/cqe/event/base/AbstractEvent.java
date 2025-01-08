package top.uhyils.usher.pojo.cqe.event.base;

import top.uhyils.usher.pojo.cqe.DefaultCQE;
import top.uhyils.usher.util.IdUtil;

/**
 * 抽象事件
 *
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年08月26日 08时24分
 */
public abstract class AbstractEvent extends DefaultCQE implements BaseEvent {


    protected AbstractEvent() {
        setUnique(IdUtil.newId());
    }


}
