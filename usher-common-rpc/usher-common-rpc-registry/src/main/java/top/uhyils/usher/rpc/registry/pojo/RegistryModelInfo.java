package top.uhyils.usher.rpc.registry.pojo;

/**
 * 缓存的注册中心信息
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2023年03月30日 17时06分
 */
public class RegistryModelInfo {

    /**
     * 服务基本数据
     */
    private final RegistryProviderNecessaryInfo necessaryInfo;

    /**
     * 元数据
     */
    private RegistryMetadata metadata;

    private RegistryModelInfo(RegistryProviderNecessaryInfo necessaryInfo) {
        this.necessaryInfo = necessaryInfo;
    }

    /**
     * 快捷创建
     */
    public static RegistryModelInfo build(RegistryProviderNecessaryInfo necessaryInfo) {
        return new RegistryModelInfo(necessaryInfo);
    }

    /**
     * 快捷创建
     */
    public static RegistryModelInfo build(RegistryProviderNecessaryInfo necessaryInfo, RegistryMetadata metadata) {
        RegistryModelInfo registryModelInfo = new RegistryModelInfo(necessaryInfo);
        registryModelInfo.setMetadata(metadata);
        return registryModelInfo;
    }

    public RegistryProviderNecessaryInfo getNecessaryInfo() {
        return necessaryInfo;
    }

    public RegistryMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(RegistryMetadata metadata) {
        this.metadata = metadata;
    }
}
