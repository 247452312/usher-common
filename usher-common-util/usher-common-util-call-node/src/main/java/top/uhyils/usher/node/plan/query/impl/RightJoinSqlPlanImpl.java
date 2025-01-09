package top.uhyils.usher.node.plan.query.impl;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.List;
import java.util.Map;
import top.uhyils.usher.node.NodeInvokeResult;
import top.uhyils.usher.node.SqlTableSourceBinaryTreeInfo;
import top.uhyils.usher.node.plan.query.RightJoinSqlPlan;
import top.uhyils.usher.util.MapUtil;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年08月26日 16时34分
 */
public class RightJoinSqlPlanImpl extends RightJoinSqlPlan {

    public RightJoinSqlPlanImpl(Map<String, String> headers, SqlTableSourceBinaryTreeInfo tree, Long leftPlanId, Long rightPlanId) {
        super(headers, tree, leftPlanId, rightPlanId);
    }

    @Override
    public NodeInvokeResult invoke(Map<String, String> headers) {
        NodeInvokeResult nodeInvokeResult = new NodeInvokeResult(this);
        nodeInvokeResult.setFieldInfos(allFieldInfo());
        /*此处两个不同行列数的table 需要融合在一起 on中的条件是融合前需要遵守的,也是合并表的依据 where 是合并后进行筛选*/
        // on里的条件
        List<List<SQLBinaryOpExpr>> lists = splitCondition();

        JSONArray leftResults = this.leftResult.getResult();
        JSONArray rightResults = this.rightResult.getResult();

        JSONArray result = new JSONArray();
        String leftAlias = leftTree.getTableSource().getAlias();
        String rightAlias = rightTree.getTableSource().getAlias();
        for (Object rightResult : rightResults) {
            JSONObject jsonRightResult = (JSONObject) rightResult;
            boolean haveLine = false;
            for (Object leftResult : leftResults) {
                JSONObject jsonLeftResult = (JSONObject) leftResult;
                // 如果这是一个可以合并的行
                if (checkMerge(jsonLeftResult, jsonRightResult, lists, leftAlias, rightAlias)) {
                    Map<String, Object> copy = MapUtil.copy(jsonRightResult);
                    copy.putAll(jsonLeftResult);
                    haveLine = true;
                    result.add(copy);
                }
            }
            // 如果没有一条符合的,就单独搞一个right添加进入
            if (!haveLine) {
                Map<String, Object> copy = MapUtil.copy(jsonRightResult);
                result.add(copy);
            }
        }
        nodeInvokeResult.setResult(result);
        return nodeInvokeResult;
    }
}
