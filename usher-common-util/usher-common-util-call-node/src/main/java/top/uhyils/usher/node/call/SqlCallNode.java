package top.uhyils.usher.node.call;

import com.alibaba.fastjson.JSONObject;
import java.util.Map;
import top.uhyils.usher.enums.QuerySqlTypeEnum;
import top.uhyils.usher.handler.NodeHandler;
import top.uhyils.usher.plan.PlanInvoker;
import top.uhyils.usher.pojo.CallInfo;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.pojo.SqlInvokeCommand;
import top.uhyils.usher.pojo.TableInfo;
import top.uhyils.usher.util.Asserts;

/**
 * 执行中间节点, 在宏观上是一个图,每个节点中都保存着一条sql语句用来递归执行后面的节点 具体哪个节点是根据解析的执行计划来确定
 * mysql可以根据请求的sql语句组成一个临时的节点
 * rpc可以根据请求的节点拼装为一个sql语句 再跑mysql的逻辑
 * mq可以根据topic 和tag 来确定一个节点 拼装撑sql后执行
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月08日 16时34分
 */
public class SqlCallNode extends AbstractNode {


    private static final String SQL_KEY = "sql";

    private final String sql;

    private final NodeHandler handler;

    public SqlCallNode(SqlInvokeCommand mysqlInvokeCommand, TableInfo tableInfo, NodeHandler handler) {
        super(mysqlInvokeCommand, tableInfo);
        CallInfo callInfo = tableInfo.getCallInfo();
        QuerySqlTypeEnum type = mysqlInvokeCommand.getType();
        Asserts.assertTrue(callInfo.getSupportSqlTypes().contains(type), "http根节点未设置指定调用方式:{}", type.getCode());
        JSONObject jsonObject = callInfo.getParams().get(type);
        this.sql = jsonObject.getString(SQL_KEY);
        this.handler = handler;
    }

    public SqlCallNode(String sql, NodeHandler handler) {
        super(null, null);
        this.sql = sql;
        this.handler = handler;
    }

    @Override
    public NodeInvokeResult call(Map<String, String> header, JSONObject params) {
        return PlanInvoker.execute(sql, header, params, mysqlInvokeCommand -> {
            CallNode callNode = handler.makeNode(mysqlInvokeCommand);
            return callNode.call(mysqlInvokeCommand.getHeader(), mysqlInvokeCommand.getParams());
        });
    }

    /**
     * 节点对应的sql
     *
     * @return
     */
    protected String sql() {
        return sql;
    }

}
