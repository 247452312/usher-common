package top.uhyils.usher.plan.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import top.uhyils.usher.plan.MysqlPlan;
import top.uhyils.usher.plan.PlanFactory;
import top.uhyils.usher.pojo.SqlTableSourceBinaryTreeInfo;

/**
 * 常规修改语句解析类
 *
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2025年1月9日 15时50分
 */
public class DeleteSqlParser extends AbstractSqlParser {


    public DeleteSqlParser() {
    }


    @Override
    public boolean canParse(SQLStatement sql) {
        return sql instanceof MySqlDeleteStatement;
    }

    @Override
    public List<MysqlPlan> parse(SQLStatement sql, Map<String, String> headers) {
        SQLTableSource tableSource = ((MySqlDeleteStatement) sql).getTableSource();
        SQLExpr whereExpr = ((MySqlDeleteStatement) sql).getWhere();
        List<MysqlPlan> plans = new ArrayList<>();
        List<SQLBinaryOpExpr> where = parseSQLExprWhere(plans, whereExpr, headers);
        SqlTableSourceBinaryTreeInfo sqlTableSourceBinaryTreeInfo = transFrom(plans, tableSource, where, headers);
        makeMainPlan(plans, sqlTableSourceBinaryTreeInfo, headers);
        return plans;

        //            Asserts.throwException("删除语句解析错误,未知的from语句类型:{},sql:{}", tableSource.getClass().getName(), tableSource.toString());
    }

    private List<MysqlPlan> makeMainPlan(List<MysqlPlan> plans, SqlTableSourceBinaryTreeInfo froms, Map<String, String> headers) {
        List<MysqlPlan> result = new ArrayList<>();
        MysqlPlan mysqlPlan = PlanFactory.buildDeleteSqlPlan(froms, headers, new HashMap<>());
        plans.add(mysqlPlan);
        result.add(mysqlPlan);
        return result;
    }
}
