package top.uhyils.usher.protocol.register;

import java.util.ArrayList;
import java.util.List;
import top.uhyils.usher.pojo.cqe.event.base.AbstractParentEvent;
import top.uhyils.usher.pojo.cqe.event.base.BaseEvent;


/**
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年09月19日 10时36分
 */
public class TestParentEventBlank extends AbstractParentEvent {

    @Override
    protected List<BaseEvent> transToBaseEvent0() {
        return new ArrayList<>();
    }
}
