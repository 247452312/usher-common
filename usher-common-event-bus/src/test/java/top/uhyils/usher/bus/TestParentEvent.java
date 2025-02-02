package top.uhyils.usher.bus;

import java.util.Arrays;
import java.util.List;
import top.uhyils.usher.pojo.cqe.event.base.AbstractParentEvent;
import top.uhyils.usher.pojo.cqe.event.base.BaseEvent;


/**
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年09月29日 20时42分
 */
public class TestParentEvent extends AbstractParentEvent {

    @Override
    protected List<BaseEvent> transToBaseEvent0() {
        return Arrays.asList(
            new TestAEvent()
        );
    }

}
