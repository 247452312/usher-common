package top.uhyils.usher.util;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import top.uhyils.usher.enums.ParseEnum;
import top.uhyils.usher.plan.SqlPlan;
import top.uhyils.usher.plan.parser.SqlParser;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2023年08月30日 17时41分
 */
public final class PlanUtil {

    private PlanUtil() {
        throw new RuntimeException("PlanUtil不能实例化");
    }


    /**
     * 解析sql语句
     *
     * @param sql sql语句
     *
     * @return
     */
    public static List<SqlPlan> analysisSqlToPlan(String sql) {
        return analysisSqlToPlan(sql, new HashMap<>());
    }

    /**
     * 解析sql语句
     *
     * @param sql sql语句
     *
     * @return
     */
    public static List<SqlPlan> analysisSqlToPlan(String sql, Map<String, String> headers) {
        SQLStatement sqlStatement = new MySqlStatementParser(sql).parseStatement();
        List<SqlParser> beans = ParseEnum.allParser(null);
        for (SqlParser bean : beans) {
            if (bean.canParse(sqlStatement)) {
                return bean.parse(sqlStatement, headers);
            }
        }
        Asserts.throwException("解析执行计划失败,sql类型:{}, sql:{}", sqlStatement.getClass().getName(), sql);
        return null;
    }

}
