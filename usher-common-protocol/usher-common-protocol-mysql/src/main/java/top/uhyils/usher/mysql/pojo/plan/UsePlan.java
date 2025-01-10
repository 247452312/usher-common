package top.uhyils.usher.mysql.pojo.plan;

import java.util.Map;
import top.uhyils.usher.plan.AbstractSqlSqlPlan;

/**
 * use命令
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2023年08月29日 10时25分
 */
public abstract class UsePlan extends AbstractSqlSqlPlan {

    protected String database;

    protected UsePlan(String database, Map<String, String> headers, Map<String, Object> params) {
        super("use " + database, headers, params);
        this.database = database;
    }

}
