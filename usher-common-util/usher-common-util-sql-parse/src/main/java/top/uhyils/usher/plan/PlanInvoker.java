package top.uhyils.usher.plan;

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
import top.uhyils.usher.enums.FieldTypeEnum;
import top.uhyils.usher.pojo.FieldInfo;
import top.uhyils.usher.pojo.MysqlInvokeCommand;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.util.Asserts;
import top.uhyils.usher.util.PlanUtil;

/**
 * 执行计划者,本身带有工具意义,并不是一个完整的领域
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年03月29日 08时38分
 */
public class PlanInvoker {


    private PlanInvoker() {
    }

    /**
     * 执行执行计划
     *
     * @return 每个执行计划的结果 key->执行计划id value->执行计划执行结果
     */
    public static NodeInvokeResult execute(String sql, Map<String, String> headers, Function<MysqlInvokeCommand, NodeInvokeResult> handler) {
        return execute(sql, headers, new JSONObject(), handler);
    }

    /**
     * 执行执行计划
     *
     * @return 每个执行计划的结果 key->执行计划id value->执行计划执行结果
     */
    public static NodeInvokeResult execute(String sql, Function<MysqlInvokeCommand, NodeInvokeResult> handler) {
        return execute(sql, new HashMap<>(16), new JSONObject(), handler);
    }

    /**
     * 执行 执行计划
     *
     * @param params 入参 正常格式 key为字段名称 value为对应字段值
     *
     * @return 每个执行计划的结果
     */
    public static NodeInvokeResult execute(String sql, Map<String, String> headers, JSONObject params, Function<MysqlInvokeCommand, NodeInvokeResult> handler) {

        // 解析sql为执行计划
        List<MysqlPlan> plans = PlanUtil.analysisSqlToPlan(sql);
        // 初始化参数
        Map<Long, NodeInvokeResult> planParamMap = makeFirstParam(params);

        NodeInvokeResult lastResult = null;
        // 补全并执行
        for (MysqlPlan mysqlPlan : plans) {
            mysqlPlan.complete(planParamMap, handler);
            NodeInvokeResult invoke = mysqlPlan.invoke(headers);
            lastResult = invoke;
            planParamMap.put(mysqlPlan.getId(), invoke);
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
