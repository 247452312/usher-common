package top.uhyils.usher.mysql.pojo.plan.impl;

import java.util.Map;
import top.uhyils.usher.content.CallNodeContent;
import top.uhyils.usher.content.CallerUserInfo;
import top.uhyils.usher.mysql.pojo.plan.UsePlan;
import top.uhyils.usher.pojo.NodeInvokeResult;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2023年08月29日 10时27分
 */
public class UseSqlPlanImpl extends UsePlan {

    public UseSqlPlanImpl(String database, Map<String, String> headers, Map<String, Object> params) {
        super(database, headers, params);
    }


    @Override
    public NodeInvokeResult invoke(Map<String, String> headers) {
        CallerUserInfo callerUserInfo = CallNodeContent.CALLER_INFO.get();
        callerUserInfo.setDatabaseName(database);
        return new NodeInvokeResult(this);
    }
}
