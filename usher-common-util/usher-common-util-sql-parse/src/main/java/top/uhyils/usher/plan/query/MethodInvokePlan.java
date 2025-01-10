package top.uhyils.usher.plan.query;

import com.alibaba.druid.sql.ast.SQLExpr;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import top.uhyils.usher.enums.SqlMethodEnum;
import top.uhyils.usher.plan.AbstractSqlSqlPlan;

/**
 * 执行方法的执行计划
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年08月26日 15时57分
 */
public abstract class MethodInvokePlan extends AbstractSqlSqlPlan {

    /**
     * 此方法在语句中的位置
     */
    protected final Integer index;

    /**
     * 方法名称
     */
    protected final String methodName;

    /**
     * 方法入参
     */
    protected final List<SQLExpr> arguments;

    /**
     * 别名
     */
    protected final String asName;

    /**
     * 方法
     */
    protected SqlMethodEnum methodEnum;


    protected MethodInvokePlan(Map<String, String> headers, Integer index, String methodName, List<SQLExpr> arguments, String asName) {
        super(null, headers, new HashMap<>());
        this.index = index;
        this.methodName = methodName;
        this.arguments = arguments;
        this.asName = asName;
        this.methodEnum = SqlMethodEnum.parse(methodName, arguments.size());
    }

    public SqlMethodEnum getMethodEnum() {
        return methodEnum;
    }
}
