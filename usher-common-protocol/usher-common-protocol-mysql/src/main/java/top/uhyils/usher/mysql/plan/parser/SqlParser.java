package top.uhyils.usher.mysql.plan.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import java.util.List;
import java.util.Map;
import top.uhyils.usher.mysql.plan.MysqlPlan;

/**
 * sql解析器
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年03月29日 08时41分
 */
public interface SqlParser {


    /**
     * 是否可以解释,既是否是此解释器
     *
     * @param sql
     *
     * @return
     */
    boolean canParse(SQLStatement sql);


    /**
     * 将sql解释为执行计划
     *
     * @param sql
     *
     * @return
     */
    List<MysqlPlan> parse(SQLStatement sql, Map<String, String> headers);

}
