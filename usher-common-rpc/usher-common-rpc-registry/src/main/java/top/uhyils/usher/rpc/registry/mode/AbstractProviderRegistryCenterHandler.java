package top.uhyils.usher.rpc.registry.mode;

import java.util.Collections;
import top.uhyils.usher.rpc.registry.exception.RegistryException;
import top.uhyils.usher.rpc.registry.pojo.RegistryModelInfo;
import top.uhyils.usher.rpc.registry.pojo.RegistryProviderNecessaryInfo;
import top.uhyils.usher.util.Asserts;
import top.uhyils.usher.util.CollectionUtil;

/**
 * 生产者注册中心句柄
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2023年04月23日 15时24分
 */
public abstract class AbstractProviderRegistryCenterHandler extends AbstractRegistryCenterHandler implements ProviderRegistryCenterHandler {

    @Override
    public Boolean registry(RegistryModelInfo info) {

        if (this.registryModelInfo != null) {
            throw new RegistryException("服务本地缓存信息已存在,请勿重复注册!");
        }
        this.registryModelInfo = Collections.singletonList(info);
        RegistryProviderNecessaryInfo necessaryInfo = info.getNecessaryInfo();
        if (necessaryInfo == null) {
            throw new RegistryException("服务注册时必要信息不能为空");
        }
        return doRegistry();
    }

    @Override
    public Boolean allowToPublish() {
        assertRegistryModelNotEmpty();
        RegistryProviderNecessaryInfo necessaryInfo = singleRegistryModel().getNecessaryInfo();
        necessaryInfo.setEnable(true);
        return doChangeRegistryInfo();
    }

    @Override
    public Boolean notAllowToPublish() {
        assertRegistryModelNotEmpty();
        RegistryProviderNecessaryInfo necessaryInfo = singleRegistryModel().getNecessaryInfo();
        necessaryInfo.setEnable(false);
        return doChangeRegistryInfo();
    }

    @Override
    public void removeInstance() {
        assertRegistryModelNotEmpty();
        doRemoveRegistryInfo();
    }

    @Override
    public Boolean isPublish() {
        assertRegistryModelNotEmpty();
        return singleRegistryModel().getNecessaryInfo().getEnable();
    }

    @Override
    protected void otherDoInit() {
        /*provider初始化 1.注册*/
        registry(RegistryModeBuilder.initRegistryInfo(serviceClass));
    }

    /**
     * 往注册中心提交注册信息
     *
     * @return
     */
    protected abstract Boolean doRegistry();

    /**
     * 删除本服务在注册中心的信息
     */
    protected abstract void doRemoveRegistryInfo();

    /**
     * 修改本服务在注册中心的信息
     *
     * @return 是否修改成功
     */
    protected abstract Boolean doChangeRegistryInfo();

    /**
     * 校验是否存在或者已经加载对应生产者信息
     */
    private void assertRegistryModelNotEmpty() {
        Asserts.assertTrue(CollectionUtil.isNotEmpty(registryModelInfo), "服务未注册");
    }

    private RegistryModelInfo singleRegistryModel() {
        return this.registryModelInfo.get(0);
    }

}
