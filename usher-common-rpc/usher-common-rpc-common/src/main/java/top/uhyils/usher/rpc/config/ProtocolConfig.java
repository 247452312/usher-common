package top.uhyils.usher.rpc.config;

import java.io.Serializable;

/**
 * 协议配置
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2021年01月16日 10时33分
 */
public class ProtocolConfig implements Serializable {

    private static final long serialVersionUID = 3664945833312687591L;


    /**
     * 在调用自身rpc时自动使用本地的bean
     */
    private Boolean autoUseSelf = Boolean.TRUE;


    public Boolean getAutoUseSelf() {
        return autoUseSelf;
    }

    public void setAutoUseSelf(Boolean autoUseSelf) {
        this.autoUseSelf = autoUseSelf;
    }
}
