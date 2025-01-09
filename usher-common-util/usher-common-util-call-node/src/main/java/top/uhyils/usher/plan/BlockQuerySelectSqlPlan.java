package top.uhyils.usher.plan;

import java.util.Map;
import top.uhyils.usher.NodeInvokeResult;
import top.uhyils.usher.SqlTableSourceBinaryTreeInfo;
import top.uhyils.usher.handler.MysqlServiceHandler;

/**
 * 简单sql执行计划
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年08月26日 16时00分
 */
public abstract class BlockQuerySelectSqlPlan extends AbstractMysqlSqlPlan {

    protected MysqlServiceHandler handler;

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

    @Override
    public void complete(Map<Long, NodeInvokeResult> planArgs, MysqlServiceHandler handler) {
        super.complete(planArgs, handler);
        this.handler = handler;
    }

    public SqlTableSourceBinaryTreeInfo toTable() {
        return froms;
    }

}
