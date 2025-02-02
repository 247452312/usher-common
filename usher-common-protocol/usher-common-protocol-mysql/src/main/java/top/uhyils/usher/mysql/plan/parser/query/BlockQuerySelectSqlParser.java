package top.uhyils.usher.mysql.plan.parser.query;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLValuableExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource.JoinType;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlCharExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.uhyils.usher.annotation.NotNull;
import top.uhyils.usher.mysql.content.MysqlContent;
import top.uhyils.usher.mysql.plan.MysqlPlan;
import top.uhyils.usher.mysql.plan.PlanFactory;
import top.uhyils.usher.mysql.pojo.SqlTableSourceBinaryTreeInfo;
import top.uhyils.usher.mysql.pojo.entity.MysqlTcpLink;
import top.uhyils.usher.mysql.pojo.plan.AbstractResultMappingPlan;
import top.uhyils.usher.mysql.pojo.plan.MethodInvokePlan;
import top.uhyils.usher.mysql.pojo.plan.impl.BinarySqlPlanImpl;
import top.uhyils.usher.mysql.pojo.sql.MySQLSelectItem;
import top.uhyils.usher.mysql.pojo.sql.MySqlListExpr;
import top.uhyils.usher.util.Asserts;
import top.uhyils.usher.util.CollectionUtil;


/**
 * 常规查询语句解析类
 *
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年11月20日 14时18分
 */
@Component
public class BlockQuerySelectSqlParser extends AbstractSelectSqlParser {

    @Autowired
    private PlanFactory planFactory;

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
            MethodInvokePlan newPlan = planFactory.buildMethodInvokePlan(headers, index, methodName, newArguments, sqlMethodInvokeExpr);
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
                MysqlPlan newPlan = planFactory.buildMethodInvokePlan(headers, i, methodName, newArgumentsItem, sqlMethodInvokeExpr);
                plans.add(newPlan);
                result.add(new MySqlCharExpr("&" + newPlan.getId()));
            } else if (argument instanceof SQLBinaryOpExpr) {
                SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) argument;
                SQLExpr left = binaryOpExpr.getLeft();
                SQLBinaryOperator operator = binaryOpExpr.getOperator();
                SQLExpr right = binaryOpExpr.getRight();
                List<SQLExpr> leftSqlExpr = parseMethodArgument(plans, headers, Arrays.asList(left));
                List<SQLExpr> rightSqlExpr = parseMethodArgument(plans, headers, Arrays.asList(right));
                MysqlPlan newPlan = planFactory.buildBinarySqlPlan(headers, leftSqlExpr.get(leftSqlExpr.size() - 1), operator, rightSqlExpr.get(rightSqlExpr.size() - 1));
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
            MysqlPlan mysqlPlan = planFactory.buildBlockQuerySelectSqlPlan(froms, headers, new HashMap<>());
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

        MysqlPlan sqlPlan = planFactory.buildRightJoinSqlPlan(headers, froms, leftPlanId, rightPlanId);
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
        MysqlPlan sqlPlan = planFactory.buildLeftJoinSqlPlan(headers, froms, leftPlanId, rightPlanId);
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

        MysqlPlan sqlPlan = planFactory.buildInnerJoinSqlPlan(headers, froms, leftPlanId, rightPlanId);
        resultPlan.add(sqlPlan);
        plans.add(sqlPlan);
        return resultPlan;
    }

    /**
     * 转换where为正常逻辑
     *
     * @param where
     *
     * @return
     */
    private List<SQLBinaryOpExpr> parseSQLExprWhere(List<MysqlPlan> plans, SQLExpr where, Map<String, String> headers) {
        if (where == null) {
            return null;
        }
        List<SQLBinaryOpExpr> result = new ArrayList<>();
        if (where instanceof SQLBinaryOpExpr) {
            return parseSqlBinaryOpExprWhere(plans, (SQLBinaryOpExpr) where, result, headers);
        }
        if (where instanceof SQLInSubQueryExpr) {
            SQLInSubQueryExpr sqlInSubQueryExpr = (SQLInSubQueryExpr) where;
            SQLExpr expr = sqlInSubQueryExpr.getExpr();
            SQLSelect subQuery = sqlInSubQueryExpr.getSubQuery();
            List<MysqlPlan> mysqlPlans = parseSelect(subQuery, headers);
            Asserts.assertTrue(CollectionUtil.isNotEmpty(mysqlPlans), "解析plan为空:{}", subQuery);
            plans.addAll(mysqlPlans);
            MysqlPlan mysqlPlan = mysqlPlans.get(0);
            return Collections.singletonList(new SQLBinaryOpExpr(expr, SQLBinaryOperator.Equality, new MySqlCharExpr("&" + mysqlPlan.getId())));
        }
        if (where instanceof SQLInListExpr) {
            SQLInListExpr sqlInListExpr = (SQLInListExpr) where;
            SQLExpr expr = sqlInListExpr.getExpr();
            List<SQLExpr> targetList = sqlInListExpr.getTargetList();
            MySqlListExpr mySqlListExpr = new MySqlListExpr(targetList);
            return Collections.singletonList(new SQLBinaryOpExpr(expr, SQLBinaryOperator.Equality, mySqlListExpr));
        }
        Asserts.throwException("sql_where解析错误,没有找到解析类型:{}", where);
        return null;
    }

    private List<SQLBinaryOpExpr> parseSqlBinaryOpExprWhere(List<MysqlPlan> plans, SQLBinaryOpExpr whereSqlBinaryOpExpr, List<SQLBinaryOpExpr> sqlBinaryOpExprs, Map<String, String> headers) {
        SQLExpr left = whereSqlBinaryOpExpr.getLeft();
        SQLExpr right = whereSqlBinaryOpExpr.getRight();
        if (whereSqlBinaryOpExpr.getOperator().isRelational() || whereSqlBinaryOpExpr.getOperator() == SQLBinaryOperator.LessThanOrEqualOrGreaterThan) {
            if (right instanceof SQLValuableExpr) {
                sqlBinaryOpExprs.add(whereSqlBinaryOpExpr);
                return sqlBinaryOpExprs;
            }
            if (right instanceof SQLQueryExpr) {
                List<MysqlPlan> mysqlPlans = reExecute(right.toString(), headers, (Consumer<List<MysqlPlan>>) plans::addAll);
                MysqlPlan lastMysqlPlan = mysqlPlans.get(mysqlPlans.size() - 1);
                SQLBinaryOpExpr sqlBinaryOpExpr = new SQLBinaryOpExpr(left, whereSqlBinaryOpExpr.getOperator(), new MySqlCharExpr("&" + lastMysqlPlan.getId()));
                sqlBinaryOpExprs.add(sqlBinaryOpExpr);
                return sqlBinaryOpExprs;
            }
            Asserts.throwException("未知的where条件类型:{},条件内容:{}", right.getClass().getName(), right.toString());

        }
        List<SQLBinaryOpExpr> leftSqlBinaryOpExprs = parseSQLExprWhere(plans, left, headers);
        sqlBinaryOpExprs.addAll(leftSqlBinaryOpExprs);
        List<SQLBinaryOpExpr> rightSqlBinaryOpExprs = parseSQLExprWhere(plans, right, headers);
        sqlBinaryOpExprs.addAll(rightSqlBinaryOpExprs);
        return sqlBinaryOpExprs;
    }

    /**
     * 转换from为正常的逻辑
     *
     * @param plans
     * @param from
     */
    @NotNull
    private SqlTableSourceBinaryTreeInfo transFrom(List<MysqlPlan> plans, SQLTableSource from, List<SQLBinaryOpExpr> where, Map<String, String> headers) {
        if (from == null) {
            // 无from语句,默认字段为@@前缀的系统变量, 并且如果from为空,则默认查询dual表
            SQLExprTableSource dual = new SQLExprTableSource(new SQLPropertyExpr(MysqlContent.DUAL_DATABASES, "dual"), null);
            return pool.getOrCreateObject(dual, where);
        } else if (from instanceof SQLJoinTableSource) {
            SQLJoinTableSource sqlJoinTableSource = (SQLJoinTableSource) from;
            JoinType joinType = sqlJoinTableSource.getJoinType();
            SqlTableSourceBinaryTreeInfo lefts = transFrom(plans, sqlJoinTableSource.getLeft(), where, headers);
            SqlTableSourceBinaryTreeInfo rights = transFrom(plans, sqlJoinTableSource.getRight(), where, headers);

            SQLBinaryOpExpr condition = (SQLBinaryOpExpr) sqlJoinTableSource.getCondition();
            switch (joinType) {
                case JOIN:
                case COMMA:
                case INNER_JOIN:
                    return pool.getOrCreateObject(lefts, rights, condition, JoinType.INNER_JOIN);
                case LEFT_OUTER_JOIN:
                    return pool.getOrCreateObject(lefts, rights, condition, JoinType.LEFT_OUTER_JOIN);
                case RIGHT_OUTER_JOIN:
                    return pool.getOrCreateObject(rights, lefts, condition, JoinType.LEFT_OUTER_JOIN);
                default:
                    Asserts.throwException("sql连表条件不支持:{}", joinType.name_lcase);
                    return null;
            }

        } else if (!(from instanceof SQLExprTableSource)) {
            SQLExprTableSource sqlExprTableSource = reExecute(from.toString(), headers, parse -> {
                Asserts.assertTrue(CollectionUtil.isNotEmpty(parse), "解析plan为空:{}", from.toString());
                plans.addAll(parse);
                MysqlPlan lastPlan = parse.get(parse.size() - 1);
                long lastPlanId = lastPlan.getId();
                MysqlTcpLink mysqlTcpLink = MysqlContent.MYSQL_TCP_INFO.get();
                return new SQLExprTableSource(new SQLPropertyExpr(mysqlTcpLink.getDatabase(), "&" + lastPlanId), from.getAlias());
            });
            return pool.getOrCreateObject(sqlExprTableSource, where);
        } else {
            return pool.getOrCreateObject((SQLExprTableSource) from, where);
        }
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
        plans.add(makeResultMappingPlan(headers, lastMainPlan, selectList));

        return plans;
    }

    /**
     * 制作结果字段映射节点执行计划
     *
     * @param lastMainPlan
     * @param selectList
     */
    private AbstractResultMappingPlan makeResultMappingPlan(Map<String, String> headers, MysqlPlan lastMainPlan, List<MySQLSelectItem> selectList) {
        return planFactory.buildResultMappingPlan(headers, lastMainPlan, selectList);
    }


}
