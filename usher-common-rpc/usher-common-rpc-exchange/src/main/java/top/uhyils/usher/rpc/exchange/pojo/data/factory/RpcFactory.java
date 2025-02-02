package top.uhyils.usher.rpc.exchange.pojo.data.factory;

import io.netty.buffer.ByteBuf;
import top.uhyils.usher.rpc.exchange.content.UsherRpcContent;
import top.uhyils.usher.rpc.exchange.pojo.data.RpcData;
import top.uhyils.usher.rpc.exchange.pojo.head.RpcHeader;
import top.uhyils.usher.rpc.spi.RpcSpiExtension;

/**
 * rpc工厂抽象接口
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2020年12月18日 12时39分
 */
public interface RpcFactory extends RpcSpiExtension {

    /**
     * 使用数据流新建一个rpc体
     *
     * @param data 数据流
     *
     * @return 创建之后的pojo
     */
    RpcData createByBytes(byte[] data) throws InterruptedException;

    /**
     * 根据数据流新建一个rpc体
     *
     * @param data
     *
     * @return
     */
    RpcData createByByteBuf(ByteBuf data) throws InterruptedException;

    /**
     * 根据一些必要的信息创建RPC体
     *
     * @param rpcHeaders   rpc头
     * @param unique       唯一标示
     * @param others       其他
     * @param contentArray rpc内容体以及其他内容
     *
     * @return
     */
    RpcData createByInfo(Long unique, Object[] others, RpcHeader[] rpcHeaders, String... contentArray) throws InterruptedException;

    /**
     * 创建一个超时的rpc返回体
     *
     * @param request 请求
     * @param timeout 超时时间
     *
     * @return
     */
    RpcData createTimeoutResponse(RpcData request, Long timeout) throws InterruptedException;

    /**
     * 获取RPC心跳包请求
     *
     * @return
     */
    default RpcData getHealth() {
        return UsherRpcContent.RPC_HEALTH_DATA;
    }

}
