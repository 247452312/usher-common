package top.uhyils.usher.plan.impl;

import com.alibaba.druid.sql.ast.SQLExpr;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.helpers.MessageFormatter;
import top.uhyils.usher.NodeInvokeResult;
import top.uhyils.usher.content.CallNodeContent;
import top.uhyils.usher.content.CallerUserInfo;
import top.uhyils.usher.plan.MethodInvokePlan;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年08月26日 16时33分
 */
public class MethodInvokePlanImpl extends MethodInvokePlan {


    public MethodInvokePlanImpl(Map<String, String> headers, Integer index, String methodName, List<SQLExpr> arguments, String asName) {
        super(headers, index, methodName, arguments, asName);
    }

    @Override
    public NodeInvokeResult invoke(Map<String, String> headers) {
        CallerUserInfo mysqlTcpLink = CallNodeContent.CALLER_INFO.get();
        NodeInvokeResult nodeInvokeResult = new NodeInvokeResult(this);
        String fieldName = toFieldName();
        nodeInvokeResult.setFieldInfos(Collections.singletonList(methodEnum.makeFieldInfo(mysqlTcpLink.getDatabaseName(), CallNodeContent.DEFAULT_METHOD_CALL_TABLE, CallNodeContent.DEFAULT_METHOD_CALL_TABLE, this.index, fieldName)));
        nodeInvokeResult.setResult(methodEnum.makeResult(lastAllPlanResult, lastNodeInvokeResult, arguments, fieldName));
        return nodeInvokeResult;
    }

    private String toFieldName() {
        if (asName != null) {
            return asName;
        }
        String collect = arguments.stream().map(Object::toString).collect(Collectors.joining(","));
        return MessageFormatter.arrayFormat("{}({})", new Object[]{methodName, collect}).getMessage();
    }

}
