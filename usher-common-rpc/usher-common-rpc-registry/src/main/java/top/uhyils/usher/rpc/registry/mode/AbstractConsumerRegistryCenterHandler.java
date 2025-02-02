package top.uhyils.usher.rpc.registry.mode;

import com.alibaba.fastjson.JSON;
import java.util.List;
import java.util.function.Consumer;
import top.uhyils.usher.rpc.registry.pojo.RegistryModelInfo;
import top.uhyils.usher.rpc.registry.pojo.event.RegistryEvent;
import top.uhyils.usher.util.LogUtil;

/**
 * 消费者注册中心句柄模板
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2023年04月23日 11时10分
 */
public abstract class AbstractConsumerRegistryCenterHandler extends AbstractRegistryCenterHandler implements ConsumerRegistryCenterHandler {

    /**
     * 没有注册时默认回调
     */
    private final Consumer<RegistryEvent> DEFAULT_CONSUMER = registryEvent -> {
        LogUtil.debug("回调时服务未注册,服务信息:{}", JSON.toJSONString(registryEvent));
    };

    /**
     * 回调事件保存 可以在初始化时通过{@link AbstractConsumerRegistryCenterHandler#registerEvent(Consumer)} 进行事件的注册
     */
    private Consumer<RegistryEvent> callBacks;

    @Override
    public void registerEvent(Consumer<RegistryEvent> function) {
        this.callBacks = function;
    }

    @Override
    public List<RegistryModelInfo> cacheInfo() {
        return registryModelInfo;
    }

    @Override
    protected void otherDoInit() {
        initRegistryInfo();
        addConsumerListener();
    }

    /**
     * 添加消费者监听对应的消息
     */
    protected abstract void addConsumerListener();

    /**
     * 事件接收时通过注册好的回调事件进行逻辑处理
     *
     * @param event
     */
    protected synchronized void onEvent(RegistryEvent event) {
        if (callBacks != null) {
            callBacks.accept(event);
        } else {
            DEFAULT_CONSUMER.accept(event);
        }
    }
}
