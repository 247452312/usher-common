package top.uhyils.usher.rpc.netty.spi.filter.filter;

import java.util.List;
import top.uhyils.usher.rpc.netty.spi.filter.RpcFilter;
import top.uhyils.usher.rpc.netty.spi.filter.invoker.RpcInvoker;
import top.uhyils.usher.rpc.spi.RpcSpiManager;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2021年01月19日 07时52分
 */
public class InvokerChainBuilder {

    private InvokerChainBuilder() {
    }

    public static RpcInvoker buildProviderAroundInvokerChain(RpcInvoker lastDefaultInvoker) {
        // 最后一个连接器,系统类,必须执行
        RpcInvoker last = lastDefaultInvoker;
        // 获取已经排序好的所有的拦截器
        List<ProviderFilter> chain = RpcSpiManager.createOrGetExtensionListByClassNoInit(RpcFilter.class, ProviderFilter.class);

        for (int i = chain.size() - 1; i >= 0; i--) {
            ProviderFilter providerInvoker = chain.get(i);
            RpcInvoker next = last;
            last = context -> providerInvoker.invoke(next, context);
        }
        return last;
    }

    public static RpcInvoker buildConsumerSendInvokerChain(RpcInvoker lastConsumerInvoker) {
        RpcInvoker last = lastConsumerInvoker;
        List<ConsumerFilter> chain = RpcSpiManager.createOrGetExtensionListByClassNoInit(RpcFilter.class, ConsumerFilter.class);
        for (int i = chain.size() - 1; i >= 0; i--) {
            ConsumerFilter providerInvoker = chain.get(i);
            RpcInvoker next = last;
            last = context -> providerInvoker.invoke(next, context);
        }
        return last;
    }
}
