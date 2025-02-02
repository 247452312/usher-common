package top.uhyils.usher.rpc.registry.manager;

import java.util.stream.Collectors;
import top.uhyils.usher.rpc.annotation.RpcSpi;
import top.uhyils.usher.rpc.registry.ProviderRegistry;
import top.uhyils.usher.rpc.registry.Registry;
import top.uhyils.usher.rpc.registry.content.RegistryContent;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年08月04日 09时22分
 */
@RpcSpi
public class UsherRpcRegistryManagerImpl implements UsherRpcRegistryManager {

    /**
     * 允许应用对外提供服务
     */
    @Override
    public void allowProviderToPublish() {
        for (ProviderRegistry byClass : RegistryContent.PROVIDER_REGISTRY) {
            byClass.allowToPublish();
        }
    }

    /**
     * 禁止应用对外提供服务
     */
    @Override
    public void notAllowProviderToPublish() {
        for (ProviderRegistry byClass : RegistryContent.PROVIDER_REGISTRY) {
            byClass.notAllowToPublish();
        }
    }


    /**
     * 应用是否在完整向外发布服务
     *
     * @return
     */
    @Override
    public boolean isPublish() {
        for (ProviderRegistry byClass : RegistryContent.PROVIDER_REGISTRY) {
            if (Boolean.FALSE.equals(byClass.publishStatus())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 应用关闭通知回调
     */
    @Override
    public void closeHook() {
        RegistryContent.PROVIDER_REGISTRY.parallelStream().map(Registry::close).collect(Collectors.toList());
        RegistryContent.CONSUMER_REGISTRY.parallelStream().map(Registry::close).collect(Collectors.toList());

    }
}
