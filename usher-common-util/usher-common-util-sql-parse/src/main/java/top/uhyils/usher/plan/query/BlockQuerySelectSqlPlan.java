package top.uhyils.usher.plan.query;

import java.util.Map;
import top.uhyils.usher.plan.AbstractSqlSqlPlan;
import top.uhyils.usher.pojo.SqlTableSourceBinaryTreeInfo;

/**
 * 简单sql执行计划
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年08月26日 16时00分
 */
public abstract class BlockQuerySelectSqlPlan extends AbstractSqlSqlPlan {


    /**
     * table详情
     */
    protected SqlTableSourceBinaryTreeInfo froms;

    protected BlockQuerySelectSqlPlan(SqlTableSourceBinaryTreeInfo froms, Map<String, String> headers, Map<String, Object> params) {
        super("select * from " + froms.getTableSource().getName(), headers, params);
        this.froms = froms;
    }

    public BlockQuerySelectSqlPlan(Long id, String sql, Map<String, String> headers, Map<String, Object> params) {
        super(id, sql, headers, params);
    }


    public SqlTableSourceBinaryTreeInfo toTable() {
        return froms;
    }

}
