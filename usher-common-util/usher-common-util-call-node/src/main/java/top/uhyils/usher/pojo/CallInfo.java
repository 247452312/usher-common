package top.uhyils.usher.pojo;

import com.alibaba.fastjson.JSONObject;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import top.uhyils.usher.enums.QuerySqlTypeEnum;

/**
 * 叶子节点真正执行时需要的信息
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月10日 09时39分
 */
public class CallInfo implements Serializable {

    /**
     * 支持的类型
     */
    private List<QuerySqlTypeEnum> supportSqlTypes;

    /**
     * 对应的类型支持的参数
     */
    private Map<QuerySqlTypeEnum, JSONObject> params;

    public List<QuerySqlTypeEnum> getSupportSqlTypes() {
        return supportSqlTypes;
    }

    public void setSupportSqlTypes(List<QuerySqlTypeEnum> supportSqlTypes) {
        this.supportSqlTypes = supportSqlTypes;
    }

    public Map<QuerySqlTypeEnum, JSONObject> getParams() {
        return params;
    }

    public void setParams(Map<QuerySqlTypeEnum, JSONObject> params) {
        this.params = params;
    }
}
