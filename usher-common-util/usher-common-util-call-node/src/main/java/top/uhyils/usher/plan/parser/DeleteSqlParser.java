package top.uhyils.usher.plan.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import java.util.List;
import java.util.Map;
import top.uhyils.usher.plan.MysqlPlan;

/**
 * 常规查询语句解析类
 *
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2025年1月9日 15时50分
 */
public class DeleteSqlParser implements SqlParser {


    public DeleteSqlParser() {
    }


    @Override
    public boolean canParse(SQLStatement sql) {
        return sql instanceof MySqlUpdateStatement;
    }

    @Override
    public List<MysqlPlan> parse(SQLStatement sql, Map<String, String> headers) {
        return null;
    }
}
