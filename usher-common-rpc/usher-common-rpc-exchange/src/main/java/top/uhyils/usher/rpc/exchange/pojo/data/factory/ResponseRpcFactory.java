package top.uhyils.usher.rpc.exchange.pojo.data.factory;

import top.uhyils.usher.exception.AssertException;
import top.uhyils.usher.rpc.exchange.pojo.data.RpcData;
import top.uhyils.usher.rpc.exchange.pojo.head.RpcHeader;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年07月06日 15时31分
 */
public interface ResponseRpcFactory extends RpcFactory {


    /**
     * 创建一个断言异常返回
     *
     * @param cause
     *
     * @return
     */
    RpcData createAssertExceptionResponse(RpcData requestRpcData, AssertException cause) throws InterruptedException;

    RpcData createErrorResponse(Long unique, Throwable e, RpcHeader[] rpcHeaders) throws InterruptedException;
}
