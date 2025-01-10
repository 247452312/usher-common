package top.uhyils.usher.plan.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import java.util.List;
import java.util.Map;
import top.uhyils.usher.plan.SqlPlan;

/**
 * 查询解释器
 *
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年11月20日 11时18分
 */
public abstract class AbstractSelectSqlParser extends AbstractSqlParser {


    @Override
    public boolean canParse(SQLStatement sql) {
        if (!(sql instanceof SQLSelectStatement)) {
            return false;
        }
        return doCanParse((SQLSelectStatement) sql);
    }

    @Override
    public List<SqlPlan> parse(SQLStatement sql, Map<String, String> headers) {
        return doParse((SQLSelectStatement) sql, headers);
    }


    /**
     * 是否是指定语句
     *
     * @param sql
     *
     * @return
     */
    protected abstract boolean doCanParse(SQLSelectStatement sql);

    /**
     * 解析
     *
     * @param sql
     *
     * @return
     */
    protected abstract List<SqlPlan> doParse(SQLSelectStatement sql, Map<String, String> headers);


}
