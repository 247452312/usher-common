package top.uhyils.usher.node.plan.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import top.uhyils.usher.node.SqlTableSourceBinaryTreeInfo;
import top.uhyils.usher.node.plan.MysqlPlan;
import top.uhyils.usher.node.plan.PlanFactory;

/**
 * 常规修改语句解析类
 *
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2025年1月9日 15时50分
 */
public class UpdateSqlParser extends AbstractSqlParser {


    public UpdateSqlParser() {
    }


    @Override
    public boolean canParse(SQLStatement sql) {
        return sql instanceof MySqlUpdateStatement;
    }

    @Override
    public List<MysqlPlan> parse(SQLStatement sql, Map<String, String> headers) {
        MySqlUpdateStatement updateSql = (MySqlUpdateStatement) sql;
        SQLExpr whereExpr = updateSql.getWhere();
        List<SQLUpdateSetItem> items = updateSql.getItems();
        Map<String, String> itemMap = new HashMap<>(items.size());
        for (SQLUpdateSetItem item : items) {
            SQLExpr column = item.getColumn();
            SQLExpr value = item.getValue();
            itemMap.put(column.toString(), value.toString());
        }
        List<MysqlPlan> plans = new ArrayList<>();
        List<SQLBinaryOpExpr> where = parseSQLExprWhere(plans, whereExpr, headers);
        SqlTableSourceBinaryTreeInfo sqlTableSourceBinaryTreeInfo = transFrom(plans, updateSql.getTableSource(), where, headers);
        makeMainPlan(plans, sqlTableSourceBinaryTreeInfo, itemMap, headers);

        return plans;
    }

    private List<MysqlPlan> makeMainPlan(List<MysqlPlan> plans, SqlTableSourceBinaryTreeInfo froms, Map<String, String> itemMap, Map<String, String> headers) {
        List<MysqlPlan> result = new ArrayList<>();
        MysqlPlan mysqlPlan = PlanFactory.buildUpdateSql(froms, itemMap, headers, new HashMap<>());
        plans.add(mysqlPlan);
        result.add(mysqlPlan);
        return result;
    }
}
