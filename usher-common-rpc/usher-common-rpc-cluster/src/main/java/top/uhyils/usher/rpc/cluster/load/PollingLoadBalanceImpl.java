package top.uhyils.usher.rpc.cluster.load;

import java.util.concurrent.atomic.AtomicInteger;
import top.uhyils.usher.rpc.annotation.RpcSpi;
import top.uhyils.usher.rpc.cluster.pojo.SendInfo;

/**
 * 轮询调用
 *
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年06月10日 08时55分
 */
@RpcSpi
public class PollingLoadBalanceImpl extends AbstractLoadBalance {

    /**
     * 如果是轮询时的标记
     */
    private final AtomicInteger pollingMark = new AtomicInteger(0);

    @Override
    protected int getIndex(SendInfo info, int size) {
        int pollIndex = pollingMark.getAndAdd(1);
        if (pollIndex > size) {
            pollingMark.set(0);
            return 0;
        } else {
            return pollIndex;
        }
    }

    @Override
    protected int getType() {
        return INDEX_TYPE;
    }
}
