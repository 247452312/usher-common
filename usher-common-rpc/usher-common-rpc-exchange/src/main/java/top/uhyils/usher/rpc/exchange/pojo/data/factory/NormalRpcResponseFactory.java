package top.uhyils.usher.rpc.exchange.pojo.data.factory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import top.uhyils.usher.exception.AssertException;
import top.uhyils.usher.pojo.DTO.base.ServiceResult;
import top.uhyils.usher.rpc.annotation.RpcSpi;
import top.uhyils.usher.rpc.config.RpcConfigFactory;
import top.uhyils.usher.rpc.content.RpcHeaderContext;
import top.uhyils.usher.rpc.enums.RpcResponseTypeEnum;
import top.uhyils.usher.rpc.enums.RpcStatusEnum;
import top.uhyils.usher.rpc.enums.RpcTypeEnum;
import top.uhyils.usher.rpc.exception.RpcBusinessException;
import top.uhyils.usher.rpc.exchange.content.UsherRpcContent;
import top.uhyils.usher.rpc.exchange.pojo.content.RpcContent;
import top.uhyils.usher.rpc.exchange.pojo.content.RpcResponseContentFactory;
import top.uhyils.usher.rpc.exchange.pojo.data.NormalResponseRpcData;
import top.uhyils.usher.rpc.exchange.pojo.data.RpcData;
import top.uhyils.usher.rpc.exchange.pojo.head.RpcHeader;
import top.uhyils.usher.rpc.spi.RpcSpiManager;

/**
 * rpc响应体工厂
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2020年12月18日 12时47分
 */
@RpcSpi
public class NormalRpcResponseFactory extends AbstractRpcFactory implements ResponseRpcFactory {

    private static final String RPC_RESPONSE_DEFAULT_NAME = "RPC_RESPONSE_DEFAULT_NAME";

    private static final String RPC_RESPONSE_SPI_NAME = "RPC_RESPONSE_SPI_NAME";

    public static NormalResponseRpcData createNewNormalResponseRpcData() throws InterruptedException {
        // spi 获取消费者的注册者信息
        String registryName = (String) RpcConfigFactory.getCustomOrDefault(RPC_RESPONSE_SPI_NAME, RPC_RESPONSE_DEFAULT_NAME);
        // 返回一个构造完成的消费者
        return (NormalResponseRpcData) RpcSpiManager.createOrGetExtensionByClass(RpcData.class, registryName);
    }

    @Override
    public RpcData createByBytes(byte[] data) throws InterruptedException {
        // spi 获取消费者的注册者信息
        String registryName = (String) RpcConfigFactory.getCustomOrDefault(RPC_RESPONSE_SPI_NAME, RPC_RESPONSE_DEFAULT_NAME);
        // 返回一个构造完成的消费者
        return (RpcData) RpcSpiManager.createOrGetExtensionByClass(RpcData.class, registryName, data);
    }

    @Override
    public RpcData createByInfo(Long unique, Object[] others, RpcHeader[] rpcHeaders, String... contentArray) throws InterruptedException {
        // spi 获取消费者的注册者信息
        NormalResponseRpcData rpcNormalRequest = createNewNormalResponseRpcData();

        rpcNormalRequest.setType(RpcTypeEnum.RESPONSE.getCode());
        rpcNormalRequest.setVersion(UsherRpcContent.VERSION);
        rpcNormalRequest.setHeaders(rpcHeaders);
        rpcNormalRequest.setContentArray(contentArray);
        rpcNormalRequest.setStatus((Byte) others[0]);
        rpcNormalRequest.setUnique(unique);

        RpcContent content = RpcResponseContentFactory.createByContentArray(rpcNormalRequest, contentArray);
        rpcNormalRequest.setContent(content);
        rpcNormalRequest.setSize(content.toString().getBytes(StandardCharsets.UTF_8).length);
        return rpcNormalRequest;
    }

    @Override
    public RpcData createAssertExceptionResponse(RpcData requestRpcData, AssertException cause) throws InterruptedException {
        // spi 获取消费者的注册者信息
        NormalResponseRpcData rpcNormalRequest = createNewNormalResponseRpcData();

        rpcNormalRequest.setType(RpcTypeEnum.RESPONSE.getCode());
        rpcNormalRequest.setVersion(UsherRpcContent.VERSION);
        Map<String, String> rpcHeaderMap = RpcHeaderContext.get();
        RpcHeader[] rpcHeaderArray = rpcHeaderMap.entrySet().stream().map(t -> new RpcHeader(t.getKey(), t.getValue())).toArray(RpcHeader[]::new);
        rpcNormalRequest.setHeaders(rpcHeaderArray);
        ServiceResult<?> serviceResult = ServiceResult.buildAssertFailedResult(cause.getMessage());
        String[] contentArray = {String.valueOf(RpcResponseTypeEnum.STRING_BACK.getCode()), JSON.toJSONString(serviceResult, SerializerFeature.WriteClassName)};
        rpcNormalRequest.setContentArray(contentArray);
        rpcNormalRequest.setStatus(RpcStatusEnum.OK.getCode());
        rpcNormalRequest.setUnique(requestRpcData.unique());

        RpcContent content = RpcResponseContentFactory.createByContentArray(rpcNormalRequest, contentArray);
        rpcNormalRequest.setContent(content);
        rpcNormalRequest.setSize(content.toString().getBytes(StandardCharsets.UTF_8).length);
        return rpcNormalRequest;
    }

    @Override
    public RpcData createTimeoutResponse(RpcData request, Long timeout) throws InterruptedException {
        // spi 获取消费者的注册者信息
        NormalResponseRpcData rpcNormalRequest = createNewNormalResponseRpcData();

        rpcNormalRequest.setType(RpcTypeEnum.REQUEST.getCode());
        rpcNormalRequest.setVersion(UsherRpcContent.VERSION);
        rpcNormalRequest.setHeaders(request.rpcHeaders());
        String[] contentArray = {String.valueOf(RpcResponseTypeEnum.EXCEPTION.getCode()), "生产者超时:" + timeout};
        rpcNormalRequest.setContentArray(contentArray);
        rpcNormalRequest.setStatus(RpcStatusEnum.PROVIDER_TIMEOUT.getCode());
        rpcNormalRequest.setUnique(request.unique());
        RpcContent content = RpcResponseContentFactory.createByContentArray(rpcNormalRequest, contentArray);
        rpcNormalRequest.setContent(content);
        rpcNormalRequest.setSize(content.toString().getBytes(StandardCharsets.UTF_8).length);
        return rpcNormalRequest;
    }


    /**
     * 创建一个错误返回
     *
     * @param unique     唯一标示
     * @param e          异常
     * @param rpcHeaders rpcHeader
     *
     * @return 包含错误信息的返回
     */
    @Override
    public RpcData createErrorResponse(Long unique, Throwable e, RpcHeader[] rpcHeaders) throws InterruptedException {
        NormalResponseRpcData rpcNormalRequest = createNewNormalResponseRpcData();

        rpcNormalRequest.setType(RpcTypeEnum.RESPONSE.getCode());
        rpcNormalRequest.setVersion(UsherRpcContent.VERSION);
        rpcNormalRequest.setHeaders(rpcHeaders);
        String exceptionStr = JSON.toJSONString(e);
        String[] contentArray = new String[]{RpcResponseTypeEnum.EXCEPTION.getCode().toString(), e == null ? RpcStatusEnum.PROVIDER_ERROR.getName() : exceptionStr};
        rpcNormalRequest.setContentArray(contentArray);
        rpcNormalRequest.setStatus(RpcStatusEnum.PROVIDER_ERROR.getCode());
        rpcNormalRequest.setUnique(unique);

        RpcContent content = RpcResponseContentFactory.createByContentArray(rpcNormalRequest, contentArray);
        rpcNormalRequest.setContent(content);
        rpcNormalRequest.setSize(content.toString().getBytes(StandardCharsets.UTF_8).length);
        return rpcNormalRequest;

    }

    /**
     * 创建一个
     *
     * @param unique
     * @param businessException
     * @param rpcHeaders
     *
     * @return
     */
    public RpcData createErrorResponseByBusinessExceptionException(Long unique, RpcBusinessException businessException, RpcHeader[] rpcHeaders) throws InterruptedException {
        NormalResponseRpcData rpcNormalRequest = createNewNormalResponseRpcData();

        rpcNormalRequest.setType(RpcTypeEnum.RESPONSE.getCode());
        rpcNormalRequest.setVersion(UsherRpcContent.VERSION);
        rpcNormalRequest.setHeaders(rpcHeaders);
        Throwable cause = businessException.getCause();
        String exceptionStr = JSON.toJSONString(cause);
        String[] contentArray = new String[]{RpcResponseTypeEnum.EXCEPTION.getCode().toString(), cause == null ? RpcStatusEnum.PROVIDER_ERROR.getName() : exceptionStr};
        rpcNormalRequest.setContentArray(contentArray);
        rpcNormalRequest.setStatus(RpcStatusEnum.PROVIDER_ERROR.getCode());
        rpcNormalRequest.setUnique(unique);

        RpcContent content = RpcResponseContentFactory.createByContentArray(rpcNormalRequest, contentArray);
        rpcNormalRequest.setContent(content);
        rpcNormalRequest.setSize(content.toString().getBytes(StandardCharsets.UTF_8).length);
        return rpcNormalRequest;
    }

    @Override
    protected RpcTypeEnum getRpcType() {
        return RpcTypeEnum.RESPONSE;
    }
}
