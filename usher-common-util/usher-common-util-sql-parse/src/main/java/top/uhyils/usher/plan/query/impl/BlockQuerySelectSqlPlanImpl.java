package top.uhyils.usher.plan.query.impl;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlCharExpr;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import top.uhyils.usher.content.CallNodeContent;
import top.uhyils.usher.enums.QuerySqlTypeEnum;
import top.uhyils.usher.plan.query.BlockQuerySelectSqlPlan;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.pojo.SqlInvokeCommand;
import top.uhyils.usher.pojo.SqlInvokeCommandBuilder;
import top.uhyils.usher.pojo.SqlTableSourceBinaryTreeInfo;
import top.uhyils.usher.sql.ExprParseResultInfo;
import top.uhyils.usher.util.Asserts;
import top.uhyils.usher.util.StringUtil;
import top.uhyils.usher.util.UsherSqlUtil;

/**
 * 简单sql执行计划
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年08月26日 16时31分
 */
public class BlockQuerySelectSqlPlanImpl extends BlockQuerySelectSqlPlan {


    public BlockQuerySelectSqlPlanImpl(SqlTableSourceBinaryTreeInfo froms, Map<String, String> headers, Map<String, Object> params) {
        super(froms, headers, params);
    }


    @Override
    public NodeInvokeResult invoke(Map<String, String> headers) {
        SqlInvokeCommandBuilder sqlInvokeCommandBuilder = new SqlInvokeCommandBuilder();
        sqlInvokeCommandBuilder.type(QuerySqlTypeEnum.QUERY);
        sqlInvokeCommandBuilder.addArgs(params);
        sqlInvokeCommandBuilder.addHeader(headers);
        SQLExprTableSource tableSource = froms.getTableSource();
        sqlInvokeCommandBuilder.addAlias(tableSource.getAlias());
        SQLPropertyExpr expr = (SQLPropertyExpr) tableSource.getExpr();
        String owner = expr.getOwnernName();
        String tableName = expr.getName();
        if (tableName.startsWith("&")) {
            Long resultIndex = Long.parseLong(tableName.substring(1));
            return lastAllPlanResult.get(resultIndex);
        }
        List<SQLBinaryOpExpr> where = froms.getWhere();
        Map<String, Object> whereParams = new HashMap<>();
        boolean haveResult = true;
        if (where != null) {
            for (SQLBinaryOpExpr sqlBinaryOpExpr : where) {
                SQLExpr left = sqlBinaryOpExpr.getLeft();
                SQLExpr right = sqlBinaryOpExpr.getRight();

                // where两边都不是属性的时候直接忽略
                if (!(left instanceof SQLIdentifierExpr) && !(left instanceof SQLPropertyExpr) && !(right instanceof SQLIdentifierExpr)) {
                    // 如果两边不一致,则无结果
                    if (!Objects.equals(left.toString(), right.toString())) {
                        haveResult = false;
                    }
                    continue;
                }
                String leftStr = null;
                // 这里解析符号左边的参数
                if (left.toString().startsWith("&")) {
                    ExprParseResultInfo<Object> leftResponseInfo = UsherSqlUtil.parse(left, lastAllPlanResult, lastNodeInvokeResult);
                    leftStr = leftResponseInfo.get().toString();
                } else if (left instanceof SQLPropertyExpr) {
                    String paramOwner = ((SQLPropertyExpr) left).getOwnerName();
                    if (Objects.equals(paramOwner, tableSource.getAlias())) {
                        leftStr = ((SQLPropertyExpr) left).getName();
                    } else {
                        // 这个条件不是这个类的.直接跳过
                        continue;
                    }
                } else {
                    leftStr = left.toString();
                }
                List<Object> rightObjs = new ArrayList<>();
                // 这里解析符号右边的参数
                if (right instanceof MySqlCharExpr && ((MySqlCharExpr) right).getText().startsWith("&")) {
                    ExprParseResultInfo<Object> rightResponseInfo = UsherSqlUtil.parse(right, lastAllPlanResult, lastNodeInvokeResult);
                    rightObjs.addAll(rightResponseInfo.getListResult());
                    whereParams.put(leftStr, rightObjs);
                } else {
                    whereParams.put(leftStr, StringUtil.trimTarget(right.toString(), "'"));
                }
            }
        }
        sqlInvokeCommandBuilder.addArgs(whereParams);
        String database = CallNodeContent.CALLER_INFO.get().getDatabaseName();
        if (owner != null) {
            sqlInvokeCommandBuilder.fillDatabase(owner);
        } else if (StringUtil.isNotEmpty(database)) {
            sqlInvokeCommandBuilder.fillDatabase(database);
        } else {
            Asserts.throwException("No database selected");
        }
        sqlInvokeCommandBuilder.fillTable(tableName);
        SqlInvokeCommand build = sqlInvokeCommandBuilder.build();
        NodeInvokeResult nodeInvokeResult = handler.apply(build);
        nodeInvokeResult.setSourcePlan(this);
        if (!haveResult) {
            nodeInvokeResult.setResult(new ArrayList<>());
        }

        // 平铺/展开结果中的json
        nodeInvokeResult = tileResultJson(nodeInvokeResult);
        return nodeInvokeResult;
    }


}
