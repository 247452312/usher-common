package top.uhyils.usher.mysql.pojo.cqe.impl;

import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import top.uhyils.usher.exception.AssertException;
import top.uhyils.usher.handler.NodeHandler;
import top.uhyils.usher.mysql.enums.MysqlCommandTypeEnum;
import top.uhyils.usher.mysql.enums.SqlTypeEnum;
import top.uhyils.usher.mysql.handler.MysqlServiceHandler;
import top.uhyils.usher.mysql.pojo.response.MysqlResponse;
import top.uhyils.usher.mysql.pojo.response.impl.ErrResponse;
import top.uhyils.usher.mysql.pojo.response.impl.OkResponse;
import top.uhyils.usher.mysql.pojo.response.impl.ResultSetResponse;
import top.uhyils.usher.mysql.util.MysqlUtil;
import top.uhyils.usher.mysql.util.Proto;
import top.uhyils.usher.node.call.CallNode;
import top.uhyils.usher.plan.SqlPlan;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.util.CollectionUtil;
import top.uhyils.usher.util.LogUtil;
import top.uhyils.usher.util.PlanUtil;
import top.uhyils.usher.util.StringUtil;


/**
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年11月03日 18时42分
 */
public class ComQueryCommand extends MysqlSqlCommand {

    private final NodeHandler handler;

    private final MysqlServiceHandler mysqlServiceHandler;

    private String completeSql;

    public ComQueryCommand(byte[] mysqlBytes, String sql, NodeHandler handler, MysqlServiceHandler mysqlServiceHandler) {
        this(mysqlBytes, handler, mysqlServiceHandler);
        this.completeSql = sql;
    }

    public ComQueryCommand(byte[] mysqlBytes, NodeHandler handler, MysqlServiceHandler mysqlServiceHandler) {
        super(mysqlBytes);
        this.handler = handler;
        this.mysqlServiceHandler = mysqlServiceHandler;
    }

    @Override
    public List<MysqlResponse> invoke() throws Exception {
        if (StringUtil.isEmpty(completeSql)) {
            return Collections.singletonList(ErrResponse.build("sql语句不能为空"));
        }
        LogUtil.info("mysql协议sql执行了:" + completeSql);
        String[] split = completeSql.split(";");
        List<MysqlResponse> result = new ArrayList<>();
        for (String sql : split) {
            invokeSql(sql, result);
        }

        LogUtil.info("mysql协议sql回应:" + result.stream().map(MysqlResponse::toResponseStr).collect(Collectors.joining()));
        return result;
    }

    @Override
    public MysqlCommandTypeEnum type() {
        return MysqlCommandTypeEnum.COM_QUERY;
    }

    @Override
    protected void load() {
        Proto proto = new Proto(mysqlBytes, 5);
        this.completeSql = proto.get_null_str();
    }

    /**
     * 解析并执行sql
     *
     * @param sql
     * @param result
     */
    private void invokeSql(String sql, List<MysqlResponse> result) {

        // 解析sql为执行计划
        List<SqlPlan> sqlPlans = PlanUtil.analysisSqlToPlan(sql);
        // 执行计划为空, 返回执行成功,无信息
        if (CollectionUtil.isEmpty(sqlPlans)) {
            result.add(new OkResponse(SqlTypeEnum.NULL));
            return;
        }

        NodeInvokeResult execute;
        try {
            execute = PlanUtil.execute(sqlPlans, new HashMap<>(), new JSONObject(), command -> {
                CallNode callNode = MysqlUtil.parseCallNode(command, mysqlServiceHandler, handler);
                return callNode.call(command.getHeader(), command.getParams());
            });
        } catch (AssertException e) {
            LogUtil.error(this, e, "sql:" + sql + "\n");
            throw e;
        }
        // 如果没有结果, 说明不是一个常规的查询语句,返回ok即可,如果报错,则在外部已经进行了try,catch
        if (CollectionUtil.isEmpty(execute.getFieldInfos())) {
            result.add(new OkResponse(SqlTypeEnum.NULL));
            return;
        }
        result.add(new ResultSetResponse(execute.getFieldInfos(), execute.getResult()));


    }
}
