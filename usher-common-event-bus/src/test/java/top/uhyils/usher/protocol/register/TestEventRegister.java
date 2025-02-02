package top.uhyils.usher.protocol.register;

import java.util.Arrays;
import java.util.List;
import top.uhyils.usher.pojo.cqe.event.base.BaseEvent;
import top.uhyils.usher.protocol.register.base.Register;

/**
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年09月19日 10时24分
 */
@top.uhyils.usher.annotation.Register
public class TestEventRegister implements Register {

    public static Boolean b = false;

    @Override
    public List<Class<? extends BaseEvent>> targetEvent() {
        return Arrays.asList(TestEvent.class);
    }

    @Override
    public void onEvent(BaseEvent event) {
        if (event instanceof TestEvent) {
            TestEvent testEvent = (TestEvent) event;
            b = true;
            System.out.println("接收到testEvent");
        } else {
            System.err.println("错误");
        }
    }
}
