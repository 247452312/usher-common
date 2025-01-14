package top.uhyils.usher.plan.query.impl;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import top.uhyils.usher.plan.query.InnerJoinSqlPlan;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.pojo.SqlTableSourceBinaryTreeInfo;
import top.uhyils.usher.util.CollectionUtil;
import top.uhyils.usher.util.MapUtil;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年08月26日 16时32分
 */
public class InnerJoinSqlPlanImpl extends InnerJoinSqlPlan {

    public InnerJoinSqlPlanImpl(Map<String, String> headers, SqlTableSourceBinaryTreeInfo tree, Long leftPlanId, Long rightPlanId) {
        super(headers, tree, leftPlanId, rightPlanId);
    }

    @Override
    public NodeInvokeResult invoke(Map<String, String> headers) {
        NodeInvokeResult nodeInvokeResult = new NodeInvokeResult(this);
        nodeInvokeResult.setFieldInfos(allFieldInfo());
        /*此处两个不同行列数的table 需要融合在一起 on中的条件是融合前需要遵守的,也是合并表的依据 where 是合并后进行筛选*/
        // on里的条件
        List<List<SQLBinaryOpExpr>> lists = splitCondition();

        List<Map<String, Object>> leftResults = this.leftResult.getResult();
        List<Map<String, Object>> rightResults = this.rightResult.getResult();

        List<Map<String, Object>> result = new ArrayList<>();
        String leftAlias = leftTree.getTableSource().getAlias();
        String rightAlias = rightTree.getTableSource().getAlias();

        List<Map<String, Object>> leftCopy = CollectionUtil.copyList(leftResults);
        List<Map<String, Object>> rightCopy = CollectionUtil.copyList(rightResults);

        for (Object rightResult : rightResults) {
            JSONObject jsonRightResult = (JSONObject) rightResult;
            for (Object leftResult : leftResults) {
                JSONObject jsonLeftResult = (JSONObject) leftResult;
                // 如果这是一个可以合并的行
                if (checkMerge(jsonLeftResult, jsonRightResult, lists, leftAlias, rightAlias)) {
                    Map<String, Object> copy = MapUtil.copy(jsonRightResult);
                    copy.putAll(jsonLeftResult);
                    result.add(copy);
                    leftCopy.remove(leftResult);
                    rightCopy.remove(rightResult);
                }
            }
        }
        //这里加left
        for (Object leftResult : leftCopy) {
            JSONObject jsonLeftResult = (JSONObject) leftResult;
            Map<String, Object> copy = MapUtil.copy(jsonLeftResult);
            result.add(copy);
        }
        //这里加 right
        for (Object rightResult : rightCopy) {
            JSONObject jsonRightResult = (JSONObject) rightResult;
            Map<String, Object> copy = MapUtil.copy(jsonRightResult);
            result.add(copy);
        }
        nodeInvokeResult.setResult(result);
        return nodeInvokeResult;
    }
}
