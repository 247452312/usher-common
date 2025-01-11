package top.uhyils.usher.mysql.plan.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLUseStatement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import top.uhyils.usher.plan.SqlPlan;
import top.uhyils.usher.plan.parser.SqlParser;
import top.uhyils.usher.util.SqlStringUtil;

/**
 * sql
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2023年08月29日 10时20分
 */
public class UseSqlParser implements SqlParser {


    @Override
    public boolean canParse(SQLStatement sql) {
        if (sql instanceof SQLUseStatement) {
            return true;
        }
        return false;
    }

    @Override
    public List<SqlPlan> parse(SQLStatement sql, Map<String, String> headers) {
        SQLUseStatement sqlUseStatement = (SQLUseStatement) sql;
        String simpleName = sqlUseStatement.getDatabase().getSimpleName();
        simpleName = SqlStringUtil.cleanQuotation(simpleName);
        //        return Arrays.asList(PlanFactory.buildUsePlan(simpleName, headers));
        return Collections.emptyList();
    }
}
