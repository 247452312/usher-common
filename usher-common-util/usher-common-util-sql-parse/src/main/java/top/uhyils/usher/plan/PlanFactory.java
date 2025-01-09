package top.uhyils.usher.plan;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import java.util.List;
import java.util.Map;
import top.uhyils.usher.plan.delete.DeleteSqlPlanImpl;
import top.uhyils.usher.plan.query.AbstractResultMappingPlan;
import top.uhyils.usher.plan.query.BlockQuerySelectSqlPlan;
import top.uhyils.usher.plan.query.InnerJoinSqlPlan;
import top.uhyils.usher.plan.query.LeftJoinSqlPlan;
import top.uhyils.usher.plan.query.MethodInvokePlan;
import top.uhyils.usher.plan.query.RightJoinSqlPlan;
import top.uhyils.usher.plan.query.impl.BlockQuerySelectSqlPlanImpl;
import top.uhyils.usher.plan.query.impl.InnerJoinSqlPlanImpl;
import top.uhyils.usher.plan.query.impl.LeftJoinSqlPlanImpl;
import top.uhyils.usher.plan.query.impl.MethodInvokePlanImpl;
import top.uhyils.usher.plan.query.impl.ResultMappingPlanImpl;
import top.uhyils.usher.plan.query.impl.RightJoinSqlPlanImpl;
import top.uhyils.usher.plan.query.impl.UnionSqlPlanImpl;
import top.uhyils.usher.plan.update.UpdateSqlPlanImpl;
import top.uhyils.usher.pojo.SqlTableSourceBinaryTreeInfo;
import top.uhyils.usher.sql.MySQLSelectItem;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年08月26日 16时13分
 */
public class PlanFactory {

    public static BlockQuerySelectSqlPlan buildBlockQuerySelectSqlPlan(SqlTableSourceBinaryTreeInfo froms, Map<String, String> headers, Map<String, Object> params) {
        return new BlockQuerySelectSqlPlanImpl(froms, headers, params);
    }


    public static InnerJoinSqlPlan buildInnerJoinSqlPlan(Map<String, String> headers, SqlTableSourceBinaryTreeInfo tree, Long leftPlanId, Long rightPlanId) {
        return new InnerJoinSqlPlanImpl(headers, tree, leftPlanId, rightPlanId);
    }


    public static LeftJoinSqlPlan buildLeftJoinSqlPlan(Map<String, String> headers, SqlTableSourceBinaryTreeInfo tree, Long leftPlanId, Long rightPlanId) {
        return new LeftJoinSqlPlanImpl(headers, tree, leftPlanId, rightPlanId);
    }


    public static MethodInvokePlan buildMethodInvokePlan(Map<String, String> headers, Integer resultIndex, String methodName, List<SQLExpr> arguments, SQLMethodInvokeExpr invokeExpr) {
        SQLObject parent = invokeExpr.getParent();
        String asName;
        if (parent instanceof SQLSelectItem) {
            asName = ((SQLSelectItem) parent).getAlias();
        } else {
            asName = invokeExpr.getOwner() != null ? invokeExpr.getOwner().toString() : null;
        }
        return new MethodInvokePlanImpl(headers, resultIndex, methodName, arguments, asName);
    }


    public static AbstractResultMappingPlan buildResultMappingPlan(Map<String, String> headers, MysqlPlan lastMainPlan, List<MySQLSelectItem> selectList) {
        return new ResultMappingPlanImpl(headers, lastMainPlan, selectList);
    }


    public static RightJoinSqlPlan buildRightJoinSqlPlan(Map<String, String> headers, SqlTableSourceBinaryTreeInfo tree, Long leftPlanId, Long rightPlanId) {
        return new RightJoinSqlPlanImpl(headers, tree, leftPlanId, rightPlanId);
    }


    public static MysqlPlan buildUnionSelectSqlPlan(Map<String, String> headers, List<Long> planIds) {
        return new UnionSqlPlanImpl(headers, planIds);
    }


    public static MysqlPlan buildBinarySqlPlan(Map<String, String> headers, SQLExpr leftExpr, SQLBinaryOperator operator, SQLExpr rightExpr) {
        return new BinarySqlPlanImpl(headers, leftExpr, operator, rightExpr);
    }


    public static MysqlPlan buildDeleteSqlPlan(SqlTableSourceBinaryTreeInfo froms, Map<String, String> headers, Map<String, Object> params) {
        return new DeleteSqlPlanImpl(froms, headers, params);
    }

    public static MysqlPlan buildUpdateSql(SqlTableSourceBinaryTreeInfo froms, Map<String, String> itemMap, Map<String, String> headers, Map<String, Object> params) {
        return new UpdateSqlPlanImpl(froms, itemMap, headers, params);
    }
}
