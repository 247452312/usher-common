package top.uhyils.usher.plan.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import top.uhyils.usher.plan.SqlPlan;

/**
 * 常规insert语句解析类
 *
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2025年1月9日 15时50分
 */
public class InsertSqlParser extends AbstractSqlParser {


    public InsertSqlParser() {
    }


    @Override
    public boolean canParse(SQLStatement sql) {
        return sql instanceof MySqlInsertStatement;
    }

    @Override
    public List<SqlPlan> parse(SQLStatement sql, Map<String, String> headers) {
        return new ArrayList<>();
    }
}
