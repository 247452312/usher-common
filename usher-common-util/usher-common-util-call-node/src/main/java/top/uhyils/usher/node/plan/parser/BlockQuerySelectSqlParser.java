package top.uhyils.usher.node.plan.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource.JoinType;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlCharExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import top.uhyils.usher.annotation.NotNull;
import top.uhyils.usher.node.SqlTableSourceBinaryTreeInfo;
import top.uhyils.usher.node.plan.BinarySqlPlanImpl;
import top.uhyils.usher.node.plan.MysqlPlan;
import top.uhyils.usher.node.plan.PlanFactory;
import top.uhyils.usher.node.plan.query.MethodInvokePlan;
import top.uhyils.usher.node.sql.MySQLSelectItem;
import top.uhyils.usher.util.Asserts;


/**
 * 常规查询语句解析类
 *
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年11月20日 14时18分
 */
public class BlockQuerySelectSqlParser extends AbstractSelectSqlParser {


    public BlockQuerySelectSqlParser() {
    }

    @Override
    protected boolean doCanParse(SQLSelectStatement sql) {
        SQLSelectQuery query = sql.getSelect().getQuery();
        return query instanceof MySqlSelectQueryBlock;
    }

    @Override
    protected List<MysqlPlan> doParse(SQLSelectStatement sql, Map<String, String> headers) {
        return parseSelect(sql.getSelect(), headers);
    }

    private List<MySQLSelectItem> parseSelectList(List<MysqlPlan> planResults, List<SQLSelectItem> selectList, Map<String, String> headers) {
        List<MySQLSelectItem> result = new ArrayList<>();
        for (int i = 0; i < selectList.size(); i++) {
            SQLSelectItem t = selectList.get(i);
            // 解析语句块
            MySQLSelectItem sqlSelectItems = parseSelectListItem(planResults, i, headers, t);
            result.add(sqlSelectItems);
        }
        return result;
    }

    /**
     * 解析可作为单个字段的语句块
     *
     * @param result     之前的结果
     * @param index      语句块index
     * @param headers
     * @param selectItem 语句块
     *
     * @return
     */
    @Nullable
    private MySQLSelectItem parseSelectListItem(List<MysqlPlan> result, int index, Map<String, String> headers, SQLSelectItem selectItem) {
        SQLExpr expr = selectItem.getExpr();
        // 查询参数的,或者查询常规,直接返回
        if (expr instanceof SQLVariantRefExpr || (expr instanceof SQLPropertyExpr && ((SQLPropertyExpr) expr).getOwner() instanceof SQLVariantRefExpr)
            || expr instanceof SQLPropertyExpr || expr instanceof SQLIdentifierExpr || expr instanceof SQLAllColumnExpr) {
            return new MySQLSelectItem(expr, selectItem.getAlias(), selectItem);
        }
        if (expr instanceof SQLQueryExpr) {
            String sql = expr.toString();
            MysqlPlan newPlan = reExecute(sql, headers, plans -> {
                Asserts.assertTrue(plans != null && plans.size() == 1, "子查询不唯一");
                return plans.get(0);
            });
            result.add(newPlan);
            return new MySQLSelectItem(new SQLIdentifierExpr("&" + newPlan.getId()), selectItem.getAlias(), selectItem);
        }
        if (expr instanceof SQLMethodInvokeExpr) {
            SQLMethodInvokeExpr sqlMethodInvokeExpr = (SQLMethodInvokeExpr) expr;
            String methodName = sqlMethodInvokeExpr.getMethodName();
            List<SQLExpr> arguments = sqlMethodInvokeExpr.getArguments();
            List<SQLExpr> newArguments = parseMethodArgument(result, headers, arguments);
            MethodInvokePlan newPlan = PlanFactory.buildMethodInvokePlan(headers, index, methodName, newArguments, sqlMethodInvokeExpr);
            result.add(newPlan);
            return new MySQLSelectItem(new SQLIdentifierExpr("&" + newPlan.getId()), selectItem.getAlias(), selectItem, newPlan.getMethodEnum());
        }
        if (expr instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) expr;
            List<SQLExpr> sqlExprs = parseMethodArgument(result, headers, Arrays.asList(binaryOpExpr.getLeft(), binaryOpExpr.getRight()));
            BinarySqlPlanImpl newPlan = new BinarySqlPlanImpl(headers, sqlExprs.get(0), binaryOpExpr.getOperator(), sqlExprs.get(1));
            result.add(newPlan);
            return new MySQLSelectItem(new SQLIdentifierExpr("&" + newPlan.getId()), selectItem.getAlias(), selectItem);
        }
        if (expr instanceof SQLCharExpr) {
            return new MySQLSelectItem(expr, selectItem.getAlias(), selectItem);
        }
        Asserts.throwException("查询报错,子查询类型找不到:{},内容为:{}", expr.getClass().getName(), selectItem.toString());
        return null;
    }

    /**
     * 解析方法参数
     *
     * @param plans
     * @param arguments
     */
    private List<SQLExpr> parseMethodArgument(List<MysqlPlan> plans, Map<String, String> headers, List<SQLExpr> arguments) {
        List<SQLExpr> result = new ArrayList<>(arguments.size());
        for (int i = 0; i < arguments.size(); i++) {
            SQLExpr argument = arguments.get(i);
            if (argument instanceof SQLMethodInvokeExpr) {
                SQLMethodInvokeExpr sqlMethodInvokeExpr = (SQLMethodInvokeExpr) argument;
                String methodName = sqlMethodInvokeExpr.getMethodName();
                List<SQLExpr> argumentsItem = sqlMethodInvokeExpr.getArguments();
                List<SQLExpr> newArgumentsItem = parseMethodArgument(plans, headers, argumentsItem);
                MysqlPlan newPlan = PlanFactory.buildMethodInvokePlan(headers, i, methodName, newArgumentsItem, sqlMethodInvokeExpr);
                plans.add(newPlan);
                result.add(new MySqlCharExpr("&" + newPlan.getId()));
            } else if (argument instanceof SQLBinaryOpExpr) {
                SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) argument;
                SQLExpr left = binaryOpExpr.getLeft();
                SQLBinaryOperator operator = binaryOpExpr.getOperator();
                SQLExpr right = binaryOpExpr.getRight();
                List<SQLExpr> leftSqlExpr = parseMethodArgument(plans, headers, Arrays.asList(left));
                List<SQLExpr> rightSqlExpr = parseMethodArgument(plans, headers, Arrays.asList(right));
                MysqlPlan newPlan = PlanFactory.buildBinarySqlPlan(headers, leftSqlExpr.get(leftSqlExpr.size() - 1), operator, rightSqlExpr.get(rightSqlExpr.size() - 1));
                plans.add(newPlan);
                result.add(new MySqlCharExpr("&" + newPlan.getId()));
            } else {
                result.add(argument);
            }
        }
        return result;
    }

    /**
     * 制作执行计划 同时也会将生成的执行计划添加到参数plans中
     *
     * @param froms 目标表(多个)
     *
     * @return 此次制作执行计划新生成的执行计划
     */
    @NotNull
    private List<MysqlPlan> makeMainPlan(List<MysqlPlan> plans, SqlTableSourceBinaryTreeInfo froms, Map<String, String> headers) {
        List<MysqlPlan> resultPlan = new ArrayList<>();
        if (froms.isLevel()) {
            MysqlPlan mysqlPlan = PlanFactory.buildBlockQuerySelectSqlPlan(froms, headers, new HashMap<>());
            resultPlan.add(mysqlPlan);
            plans.add(mysqlPlan);
            return resultPlan;
        } else {
            JoinType joinType = froms.getJoinType();

            switch (joinType) {
                case INNER_JOIN:
                    return makeInnerJoin(plans, froms, headers);
                case LEFT_OUTER_JOIN:
                    return makeLeftJoin(plans, froms, headers);
                case RIGHT_OUTER_JOIN:
                    return makeRightJoin(plans, froms, headers);
                default:
                    Asserts.throwException("无指定连表方案");
                    return null;
            }
        }
    }

    @NotNull
    private List<MysqlPlan> makeRightJoin(List<MysqlPlan> plans, SqlTableSourceBinaryTreeInfo froms, Map<String, String> headers) {
        List<MysqlPlan> resultPlan = new ArrayList<>();
        List<MysqlPlan> rightPlan = makeMainPlan(plans, froms.getRightTree(), headers);
        resultPlan.addAll(rightPlan);
        plans.addAll(rightPlan);
        List<MysqlPlan> leftPlan = makeMainPlan(plans, froms.getLeftTree(), headers);
        resultPlan.addAll(leftPlan);
        plans.addAll(leftPlan);

        long leftPlanId = leftPlan.get(leftPlan.size() - 1).getId();
        long rightPlanId = rightPlan.get(rightPlan.size() - 1).getId();

        MysqlPlan sqlPlan = PlanFactory.buildRightJoinSqlPlan(headers, froms, leftPlanId, rightPlanId);
        resultPlan.add(sqlPlan);
        plans.add(sqlPlan);
        return resultPlan;
    }

    @NotNull
    private List<MysqlPlan> makeLeftJoin(List<MysqlPlan> plans, SqlTableSourceBinaryTreeInfo froms, Map<String, String> headers) {
        List<MysqlPlan> resultPlan = new ArrayList<>();
        List<MysqlPlan> leftPlan = makeMainPlan(plans, froms.getLeftTree(), headers);
        resultPlan.addAll(leftPlan);
        List<MysqlPlan> rightPlan = makeMainPlan(plans, froms.getRightTree(), headers);
        resultPlan.addAll(rightPlan);

        long leftPlanId = leftPlan.get(leftPlan.size() - 1).getId();
        long rightPlanId = rightPlan.get(rightPlan.size() - 1).getId();
        MysqlPlan sqlPlan = PlanFactory.buildLeftJoinSqlPlan(headers, froms, leftPlanId, rightPlanId);
        resultPlan.add(sqlPlan);
        plans.add(sqlPlan);
        return resultPlan;
    }

    @NotNull
    private List<MysqlPlan> makeInnerJoin(List<MysqlPlan> plans, SqlTableSourceBinaryTreeInfo froms, Map<String, String> headers) {
        List<MysqlPlan> resultPlan = new ArrayList<>();
        List<MysqlPlan> leftPlan = makeMainPlan(plans, froms.getLeftTree(), headers);
        resultPlan.addAll(leftPlan);
        plans.addAll(leftPlan);
        List<MysqlPlan> rightPlan = makeMainPlan(plans, froms.getRightTree(), headers);
        resultPlan.addAll(rightPlan);
        plans.addAll(rightPlan);
        long leftPlanId = leftPlan.get(leftPlan.size() - 1).getId();
        long rightPlanId = rightPlan.get(rightPlan.size() - 1).getId();

        MysqlPlan sqlPlan = PlanFactory.buildInnerJoinSqlPlan(headers, froms, leftPlanId, rightPlanId);
        resultPlan.add(sqlPlan);
        plans.add(sqlPlan);
        return resultPlan;
    }







    /**
     * 解析select语句
     *
     * @param select
     *
     * @return
     */
    private List<MysqlPlan> parseSelect(SQLSelect select, Map<String, String> headers) {
        // 1. 处理where中的子查询
        MySqlSelectQueryBlock query = (MySqlSelectQueryBlock) select.getQuery();
        ArrayList<MysqlPlan> plans = new ArrayList<>();
        List<SQLBinaryOpExpr> where = parseSQLExprWhere(plans, query.getWhere(), headers);

        // 2.处理from后需要查询的条件
        List<MysqlPlan> mainPlans = makeMainPlan(plans, transFrom(plans, query.getFrom(), where, headers), headers);

        // 3.selectList 查询字段的子查询
        MysqlPlan lastMainPlan = mainPlans.get(mainPlans.size() - 1);
        // 解析sql语句中字段
        List<MySQLSelectItem> selectList = parseSelectList(plans, query.getSelectList(), headers);
        // 添加结果字段映射节点
        plans.add(PlanFactory.buildResultMappingPlan(headers, lastMainPlan, selectList));

        return plans;
    }


}
