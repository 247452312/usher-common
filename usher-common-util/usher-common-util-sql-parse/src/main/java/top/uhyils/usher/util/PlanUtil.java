package top.uhyils.usher.util;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import top.uhyils.usher.content.CallNodeContent;
import top.uhyils.usher.content.CallerUserInfo;
import top.uhyils.usher.content.ParserContent;
import top.uhyils.usher.enums.FieldTypeEnum;
import top.uhyils.usher.enums.ParseEnum;
import top.uhyils.usher.plan.SqlPlan;
import top.uhyils.usher.plan.parser.SqlParser;
import top.uhyils.usher.pojo.FieldInfo;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.pojo.SqlInvokeCommand;

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
        List<SqlParser> beans = ParseEnum.allParser(ParserContent.otherParsers());
        for (SqlParser bean : beans) {
            if (bean.canParse(sqlStatement)) {
                return bean.parse(sqlStatement, headers);
            }
        }
        Asserts.throwException("解析执行计划失败,sql类型:{}, sql:{}", sqlStatement.getClass().getName(), sql);
        return null;
    }


    /**
     * 执行执行计划
     *
     * @return 每个执行计划的结果 key->执行计划id value->执行计划执行结果
     */
    public static NodeInvokeResult execute(String sql, Map<String, String> headers, Function<SqlInvokeCommand, NodeInvokeResult> handler) {
        return execute(sql, headers, new JSONObject(), handler);
    }

    /**
     * 执行执行计划
     *
     * @return 每个执行计划的结果 key->执行计划id value->执行计划执行结果
     */
    public static NodeInvokeResult execute(String sql, Function<SqlInvokeCommand, NodeInvokeResult> handler) {
        return execute(sql, new HashMap<>(16), new JSONObject(), handler);
    }

    /**
     * 执行 执行计划
     *
     * @param params 入参 正常格式 key为字段名称 value为对应字段值
     *
     * @return 每个执行计划的结果
     */
    public static NodeInvokeResult execute(String sql, Map<String, String> headers, JSONObject params, Function<SqlInvokeCommand, NodeInvokeResult> handler) {
        // 解析sql为执行计划
        List<SqlPlan> plans = PlanUtil.analysisSqlToPlan(sql);
        return execute(plans, headers, params, handler);
    }


    /**
     * 执行 执行计划
     *
     * @param params 入参 正常格式 key为字段名称 value为对应字段值
     *
     * @return 每个执行计划的结果
     */
    public static NodeInvokeResult execute(List<SqlPlan> plans, Map<String, String> headers, JSONObject params, Function<SqlInvokeCommand, NodeInvokeResult> handler) {

        // 初始化参数
        Map<Long, NodeInvokeResult> planParamMap = makeFirstParam(params);

        NodeInvokeResult lastResult = null;
        // 补全并执行
        for (SqlPlan sqlPlan : plans) {
            sqlPlan.complete(planParamMap, handler);
            NodeInvokeResult invoke = sqlPlan.invoke(headers);
            lastResult = invoke;
            planParamMap.put(sqlPlan.getId(), invoke);
        }
        return lastResult;
    }

    /**
     * 制作伪装为id为-1的执行计划执行结果
     *
     * @param params
     *
     * @return
     */
    @NotNull
    private static Map<Long, NodeInvokeResult> makeFirstParam(JSONObject params) {
        Map<Long, NodeInvokeResult> planParamMap = new HashMap<>();

        JSONArray jsonArray = new JSONArray();
        if (params != null) {
            jsonArray.add(params);
        }
        planParamMap.put(-1L, paramsToResult(jsonArray));

        return planParamMap;
    }

    /**
     * 由于执行计划的规则为将此执行计划之前的所有执行计划的结果作为入参 所以此处的作用为:
     * <p>
     * 将入参 伪装为 id为-1的执行计划的执行结果来作为执行计划链条的起点入参
     *
     * @return
     */
    private static NodeInvokeResult paramsToResult(JSONArray params) {
        if (params == null || params.isEmpty()) {
            return new NodeInvokeResult(null);
        }
        NodeInvokeResult nodeInvokeResult = new NodeInvokeResult(null);

        LinkedList<FieldInfo> fieldInfos = new LinkedList<>();
        List<String> fields = params.stream().flatMap(t -> ((JSONObject) t).keySet().stream()).distinct().collect(Collectors.toList());
        JSONObject firstParam = (JSONObject) params.get(0);
        CallerUserInfo callerUserInfo = CallNodeContent.CALLER_INFO.get();
        first:
        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            if (firstParam.containsKey(field)) {
                FieldInfo fieldInfo = FieldTypeEnum.makeFieldInfo(callerUserInfo.getDatabaseName(), CallNodeContent.DEFAULT_PARAM_TABLE, CallNodeContent.DEFAULT_PARAM_TABLE, firstParam.get(field), i, field);
                fieldInfos.add(fieldInfo);
            } else {
                for (Object param : params) {
                    JSONObject paramJson = (JSONObject) param;
                    if (paramJson.containsKey(field)) {
                        FieldInfo fieldInfo = FieldTypeEnum.makeFieldInfo(callerUserInfo.getDatabaseName(), CallNodeContent.DEFAULT_PARAM_TABLE, CallNodeContent.DEFAULT_PARAM_TABLE, firstParam.get(field), i, field);
                        fieldInfos.add(fieldInfo);
                        continue first;
                    }
                }
                Asserts.throwException("未找到指定类的类型:{}", field);
            }
        }
        nodeInvokeResult.setFieldInfos(fieldInfos);
        nodeInvokeResult.setResult(params);
        return nodeInvokeResult;
    }

}
