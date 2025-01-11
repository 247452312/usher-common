package top.uhyils.usher.plan.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLValuableExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource.JoinType;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlCharExpr;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import top.uhyils.usher.annotation.NotNull;
import top.uhyils.usher.content.CallNodeContent;
import top.uhyils.usher.content.CallerUserInfo;
import top.uhyils.usher.plan.SqlPlan;
import top.uhyils.usher.pojo.SqlTableSourceBinaryTreeInfo;
import top.uhyils.usher.pojo.SqlTableSourceBinaryTreePool;
import top.uhyils.usher.sql.UsherSqlListExpr;
import top.uhyils.usher.util.Asserts;
import top.uhyils.usher.util.CollectionUtil;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月09日 16时18分
 */
public abstract class AbstractSqlParser implements SqlParser {

    /**
     * 实例池 table信息
     */
    protected final SqlTableSourceBinaryTreePool pool = SqlTableSourceBinaryTreePool.getInstance();

    /**
     * sql解析
     */
    private List<AbstractSelectSqlParser> selectInterpreters;

    /**
     * 转换where为正常逻辑
     *
     * @param where
     *
     * @return
     */
    protected List<SQLBinaryOpExpr> parseSQLExprWhere(List<SqlPlan> plans, SQLExpr where, Map<String, String> headers) {
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
            List<SqlPlan> sqlPlans = reExecute(subQuery.toString(), headers, (Consumer<List<SqlPlan>>) plans::addAll);
            Asserts.assertTrue(CollectionUtil.isNotEmpty(sqlPlans), "解析plan为空:{}", subQuery);
            plans.addAll(sqlPlans);
            SqlPlan sqlPlan = sqlPlans.get(0);
            return Collections.singletonList(new SQLBinaryOpExpr(expr, SQLBinaryOperator.Equality, new MySqlCharExpr("&" + sqlPlan.getId())));
        }
        if (where instanceof SQLInListExpr) {
            SQLInListExpr sqlInListExpr = (SQLInListExpr) where;
            SQLExpr expr = sqlInListExpr.getExpr();
            List<SQLExpr> targetList = sqlInListExpr.getTargetList();
            UsherSqlListExpr usherSqlListExpr = new UsherSqlListExpr(targetList);
            return Collections.singletonList(new SQLBinaryOpExpr(expr, SQLBinaryOperator.Equality, usherSqlListExpr));
        }
        Asserts.throwException("sql_where解析错误,没有找到解析类型:{}", where);
        return null;
    }

    /**
     * 重新解析一个sql
     *
     * @param fromSql
     * @param sqlExecuteFunction sql解析成一个执行计划之后需要做什么
     */
    protected <T> T reExecute(String fromSql, Map<String, String> headers, Function<List<SqlPlan>, T> sqlExecuteFunction) {
        // 检查解析器是否初始化
        checkInterpreters();
        SQLSelectStatement fromSqlStatement = (SQLSelectStatement) new MySqlStatementParser(fromSql).parseStatement();
        return reExecute(fromSqlStatement, headers, sqlExecuteFunction);
    }

    /**
     * 重新解析一个sql
     *
     * @param fromSqlStatement
     * @param sqlExecuteFunction sql解析成一个执行计划之后需要做什么
     */
    protected <T> T reExecute(SQLSelectStatement fromSqlStatement, Map<String, String> headers, Function<List<SqlPlan>, T> sqlExecuteFunction) {
        // 检查解析器是否初始化
        checkInterpreters();
        for (AbstractSelectSqlParser selectInterpreter : selectInterpreters) {
            if (selectInterpreter.canParse(fromSqlStatement)) {
                List<SqlPlan> parse = selectInterpreter.parse(fromSqlStatement, headers);
                return sqlExecuteFunction.apply(parse);
            }
        }
        return null;
    }


    /**
     * 重新解析一个sql
     *
     * @param fromSql
     * @param reExecute
     */
    protected List<SqlPlan> reExecute(String fromSql, Map<String, String> headers, Consumer<List<SqlPlan>> reExecute) {
        //检查解析器是否初始化
        checkInterpreters();
        SQLSelectStatement fromSqlStatement = (SQLSelectStatement) new MySqlStatementParser(fromSql).parseStatement();
        for (AbstractSelectSqlParser selectInterpreter : selectInterpreters) {
            if (selectInterpreter.canParse(fromSqlStatement)) {
                List<SqlPlan> parse = selectInterpreter.parse(fromSqlStatement, headers);
                reExecute.accept(parse);
                return parse;
            }
        }
        Asserts.throwException("错误,未找到对应的解析类,语句为:{}", fromSql);
        return Collections.emptyList();
    }

    /**
     * 转换from为正常的逻辑
     *
     * @param plans
     * @param from
     */
    @NotNull
    protected SqlTableSourceBinaryTreeInfo transFrom(List<SqlPlan> plans, SQLTableSource from, List<SQLBinaryOpExpr> where, Map<String, String> headers) {
        if (from == null) {
            // 无from语句,默认字段为@@前缀的系统变量, 并且如果from为空,则默认查询dual表
            SQLExprTableSource dual = new SQLExprTableSource(new SQLPropertyExpr(CallNodeContent.DUAL_DATABASES, "dual"), null);
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
                    Asserts.throwException("sql连表条件不支持:{}", joinType.nameLCase);
                    return null;
            }

        } else if (!(from instanceof SQLExprTableSource)) {
            SQLExprTableSource sqlExprTableSource = reExecute(from.toString(), headers, parse -> {
                Asserts.assertTrue(CollectionUtil.isNotEmpty(parse), "解析plan为空:{}", from.toString());
                plans.addAll(parse);
                SqlPlan lastPlan = parse.get(parse.size() - 1);
                long lastPlanId = lastPlan.getId();
                CallerUserInfo callerUserInfo = CallNodeContent.CALLER_INFO.get();
                return new SQLExprTableSource(new SQLPropertyExpr(callerUserInfo.getDatabaseName(), "&" + lastPlanId), from.getAlias());
            });
            return pool.getOrCreateObject(sqlExprTableSource, where);
        } else {
            return pool.getOrCreateObject((SQLExprTableSource) from, where);
        }
    }

    private List<SQLBinaryOpExpr> parseSqlBinaryOpExprWhere(List<SqlPlan> plans, SQLBinaryOpExpr whereSqlBinaryOpExpr, List<SQLBinaryOpExpr> sqlBinaryOpExprs, Map<String, String> headers) {
        SQLExpr left = whereSqlBinaryOpExpr.getLeft();
        SQLExpr right = whereSqlBinaryOpExpr.getRight();
        if (whereSqlBinaryOpExpr.getOperator().isRelational() || whereSqlBinaryOpExpr.getOperator() == SQLBinaryOperator.LessThanOrEqualOrGreaterThan) {
            if (right instanceof SQLValuableExpr) {
                sqlBinaryOpExprs.add(whereSqlBinaryOpExpr);
                return sqlBinaryOpExprs;
            }
            if (right instanceof SQLQueryExpr) {
                List<SqlPlan> sqlPlans = reExecute(right.toString(), headers, (Consumer<List<SqlPlan>>) plans::addAll);
                SqlPlan lastSqlPlan = sqlPlans.get(sqlPlans.size() - 1);
                SQLBinaryOpExpr sqlBinaryOpExpr = new SQLBinaryOpExpr(left, whereSqlBinaryOpExpr.getOperator(), new MySqlCharExpr("&" + lastSqlPlan.getId()));
                sqlBinaryOpExprs.add(sqlBinaryOpExpr);
                return sqlBinaryOpExprs;
            }
            if (right instanceof SQLVariantRefExpr) {
                String name = ((SQLVariantRefExpr) right).getName();

                SQLBinaryOpExpr sqlBinaryOpExpr = new SQLBinaryOpExpr(left, whereSqlBinaryOpExpr.getOperator(), new MySqlCharExpr("&-1." + name));
                sqlBinaryOpExprs.add(sqlBinaryOpExpr);
                return sqlBinaryOpExprs;
            }
            if (right instanceof SQLPropertyExpr) {
                Asserts.throwException("不支持的where条件中表达式右侧出现sql字段, 条件内容:{}", right.getParent().toString());
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
     * 检查解析器是否初始化
     */
    private void checkInterpreters() {
        if (selectInterpreters == null) {
            selectInterpreters = Arrays.asList(new UnionSelectSqlParser(), new BlockQuerySelectSqlParser());
        }
    }

}
