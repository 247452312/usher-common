package top.uhyils.usher.plan.update;

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
import java.util.stream.Collectors;
import top.uhyils.usher.content.CallNodeContent;
import top.uhyils.usher.enums.QuerySqlTypeEnum;
import top.uhyils.usher.plan.AbstractMysqlSqlPlan;
import top.uhyils.usher.pojo.InvokeCommandBuilder;
import top.uhyils.usher.pojo.MysqlInvokeCommand;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.pojo.SqlTableSourceBinaryTreeInfo;
import top.uhyils.usher.sql.ExprParseResultInfo;
import top.uhyils.usher.util.Asserts;
import top.uhyils.usher.util.MysqlUtil;
import top.uhyils.usher.util.StringUtil;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月09日 16时36分
 */
public class UpdateSqlPlanImpl extends AbstractMysqlSqlPlan {

    private final SqlTableSourceBinaryTreeInfo froms;

    private final Map<String, String> itemMap;

    public UpdateSqlPlanImpl(SqlTableSourceBinaryTreeInfo froms, Map<String, String> itemMap, Map<String, String> headers, Map<String, Object> params) {
        super("update from " + froms.getTableSource().getName() + " set " + itemMap.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining(",")), headers, params);
        this.froms = froms;
        this.itemMap = itemMap;
    }

    @Override
    public NodeInvokeResult invoke(Map<String, String> headers) {
        InvokeCommandBuilder invokeCommandBuilder = new InvokeCommandBuilder();
        invokeCommandBuilder.type(QuerySqlTypeEnum.UPDATE);
        invokeCommandBuilder.updateItem(itemMap);
        invokeCommandBuilder.addArgs(params);
        invokeCommandBuilder.addHeader(headers);
        SQLExprTableSource tableSource = froms.getTableSource();
        invokeCommandBuilder.addAlias(tableSource.getAlias());
        SQLPropertyExpr expr = (SQLPropertyExpr) tableSource.getExpr();
        String owner = expr.getOwnernName();
        String tableName = expr.getName();
        Asserts.assertTrue(!tableName.startsWith("&"), "错误,delete语句的table不能是子查询或引用类型");
        List<SQLBinaryOpExpr> where = froms.getWhere();
        Map<String, Object> whereParams = new HashMap<>();
        if (where != null) {
            for (SQLBinaryOpExpr sqlBinaryOpExpr : where) {
                SQLExpr left = sqlBinaryOpExpr.getLeft();
                SQLExpr right = sqlBinaryOpExpr.getRight();

                // where两边都不是属性的时候直接忽略
                if (!(left instanceof SQLIdentifierExpr) && !(right instanceof SQLIdentifierExpr)) {
                    continue;
                }
                String leftStr = null;
                // 这里解析符号左边的参数
                if (left.toString().startsWith("&")) {
                    ExprParseResultInfo<Object> leftResponseInfo = MysqlUtil.parse(left, lastAllPlanResult, lastNodeInvokeResult);
                    leftStr = leftResponseInfo.get().toString();
                } else {
                    leftStr = left.toString();
                }
                List<Object> rightObjs = new ArrayList<>();
                // 这里解析符号右边的参数
                if (right instanceof MySqlCharExpr && ((MySqlCharExpr) right).getText().startsWith("&")) {
                    ExprParseResultInfo<Object> rightResponseInfo = MysqlUtil.parse(right, lastAllPlanResult, lastNodeInvokeResult);
                    rightObjs.addAll(rightResponseInfo.getListResult());
                } else {
                    rightObjs.add(right.toString());
                }
                whereParams.put(leftStr, rightObjs);
            }
        }
        invokeCommandBuilder.addArgs(whereParams);
        String database = CallNodeContent.CALLER_INFO.get().getDatabaseName();
        if (owner != null) {
            invokeCommandBuilder.fillDatabase(owner);
        } else if (StringUtil.isNotEmpty(database)) {
            invokeCommandBuilder.fillDatabase(database);
        } else {
            Asserts.throwException("No database selected");
        }
        invokeCommandBuilder.fillTable(tableName);
        MysqlInvokeCommand build = invokeCommandBuilder.build();
        NodeInvokeResult nodeInvokeResult = handler.apply(build);
        nodeInvokeResult.setSourcePlan(this);

        // 平铺/展开结果中的json
        nodeInvokeResult = tileResultJson(nodeInvokeResult);
        return nodeInvokeResult;
    }
}
