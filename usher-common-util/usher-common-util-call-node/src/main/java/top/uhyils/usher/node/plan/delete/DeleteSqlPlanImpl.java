package top.uhyils.usher.node.plan.delete;

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
import top.uhyils.usher.node.InvokeCommandBuilder;
import top.uhyils.usher.node.MysqlInvokeCommand;
import top.uhyils.usher.node.NodeInvokeResult;
import top.uhyils.usher.node.SqlTableSourceBinaryTreeInfo;
import top.uhyils.usher.node.call.CallNode;
import top.uhyils.usher.node.content.CallNodeContent;
import top.uhyils.usher.node.enums.NodeSqlTypeEnum;
import top.uhyils.usher.node.plan.AbstractMysqlSqlPlan;
import top.uhyils.usher.node.sql.ExprParseResultInfo;
import top.uhyils.usher.node.util.MysqlUtil;
import top.uhyils.usher.util.Asserts;
import top.uhyils.usher.util.StringUtil;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月09日 16时36分
 */
public class DeleteSqlPlanImpl extends AbstractMysqlSqlPlan {

    private final SqlTableSourceBinaryTreeInfo froms;

    public DeleteSqlPlanImpl(SqlTableSourceBinaryTreeInfo froms, Map<String, String> headers, Map<String, Object> params) {
        super("delete " + froms.getTableSource().getName(), headers, params);
        this.froms = froms;
    }

    @Override
    public NodeInvokeResult invoke(Map<String, String> headers) {
        InvokeCommandBuilder invokeCommandBuilder = new InvokeCommandBuilder();
        invokeCommandBuilder.type(NodeSqlTypeEnum.DELETE);
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
        CallNode callNode = handler.makeNode(build);
        NodeInvokeResult nodeInvokeResult = callNode.call(build.getHeader(), build.getParams());
        nodeInvokeResult.setSourcePlan(this);

        // 平铺/展开结果中的json
        nodeInvokeResult = tileResultJson(nodeInvokeResult);
        return nodeInvokeResult;
    }
}
