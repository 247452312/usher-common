package top.uhyils.usher.plan.impl;


import com.alibaba.fastjson.JSONArray;
import java.util.ArrayList;
import java.util.Map;
import top.uhyils.usher.NodeInvokeResult;
import top.uhyils.usher.handler.MysqlServiceHandler;
import top.uhyils.usher.plan.AbstractMysqlSqlPlan;

/**
 * 空白执行计划,什么都不做
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年04月01日 08时58分
 */
public class EmptyMysqlPlan extends AbstractMysqlSqlPlan {

    public EmptyMysqlPlan(Map<String, String> headers) {
        super(null, headers, null);
    }

    @Override
    public void complete(Map<Long, NodeInvokeResult> planArgs, MysqlServiceHandler handler) {
    }

    @Override
    public NodeInvokeResult invoke(Map<String, String> headers) {
        NodeInvokeResult nodeInvokeResult = new NodeInvokeResult(this);
        nodeInvokeResult.setFieldInfos(new ArrayList<>());
        nodeInvokeResult.setResult(new JSONArray());
        return nodeInvokeResult;
    }
}
