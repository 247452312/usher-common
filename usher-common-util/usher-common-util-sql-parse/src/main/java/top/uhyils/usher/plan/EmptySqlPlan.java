package top.uhyils.usher.plan;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import top.uhyils.usher.pojo.NodeInvokeResult;

/**
 * 空白执行计划,什么都不做
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年04月01日 08时58分
 */
public class EmptySqlPlan extends AbstractSqlSqlPlan {

    public EmptySqlPlan(Map<String, String> headers) {
        super(null, headers, new HashMap<>());
    }

    @Override
    public NodeInvokeResult invoke(Map<String, String> headers) {
        NodeInvokeResult nodeInvokeResult = new NodeInvokeResult(this);
        nodeInvokeResult.setFieldInfos(new ArrayList<>());
        nodeInvokeResult.setResult(new ArrayList<>());
        return nodeInvokeResult;
    }
}
