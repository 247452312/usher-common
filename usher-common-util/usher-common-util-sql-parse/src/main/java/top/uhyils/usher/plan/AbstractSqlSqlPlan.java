package top.uhyils.usher.plan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import top.uhyils.usher.enums.SqlPlanTypeEnum;
import top.uhyils.usher.pojo.FieldInfo;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.pojo.PlaceholderInfo;
import top.uhyils.usher.pojo.SqlInvokeCommand;
import top.uhyils.usher.util.Asserts;
import top.uhyils.usher.util.CollectionUtil;
import top.uhyils.usher.util.IdUtil;
import top.uhyils.usher.util.MapUtil;

/**
 * sql执行计划的抽象模板
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年03月29日 08时49分
 */
public abstract class AbstractSqlSqlPlan implements SqlSqlPlan {

    /**
     * 执行计划原始sql
     */
    protected final String sql;

    /**
     * 执行计划参数 key->参数名称 value->占位符(替换之后为实际的参数值)
     */
    protected final Map<String, Object> params;

    /**
     * 此plan唯一标示
     */
    private final long id;

    /**
     * 注入的handler
     */
    protected Function<SqlInvokeCommand, NodeInvokeResult> handler;

    /**
     * 请求头
     */
    protected Map<String, String> headers;

    /**
     * 过去执行的所有结果
     */
    protected Map<Long, NodeInvokeResult> lastAllPlanResult;

    /**
     * 最后一次执行的结果
     */
    protected NodeInvokeResult lastNodeInvokeResult;


    /**
     * 配置
     */

    protected AbstractSqlSqlPlan(String sql, Map<String, String> headers, Map<String, Object> params) {
        this.sql = sql;
        this.params = params;
        this.headers = headers;
        this.id = IdUtil.newId();
    }

    protected AbstractSqlSqlPlan(Long id, String sql, Map<String, String> headers, Map<String, Object> params) {
        this.sql = sql;
        this.params = params;
        this.headers = headers;
        this.id = id;
    }


    @Override
    public void complete(Map<Long, NodeInvokeResult> planArgs, Function<SqlInvokeCommand, NodeInvokeResult> handler) {
        // 填充占位符
        completePlaceholder(planArgs);
        planArgs.keySet().stream().max(Long::compareTo).ifPresent(aLong -> this.lastNodeInvokeResult = planArgs.get(aLong));
        this.lastAllPlanResult = planArgs;
        this.handler = handler;
    }

    @Override
    public SqlPlanTypeEnum type() {
        return SqlPlanTypeEnum.EXECUTE_SQL;
    }

    @Override
    public long getId() {
        return id;
    }

    public Map<String, Object> toParams() {
        return params;
    }

    public Map<String, String> toHeaders() {
        return headers;
    }

    /**
     * 填充占位符
     *
     * @param planArgs
     */
    protected void completePlaceholder(Map<Long, NodeInvokeResult> planArgs) {
        params.forEach((k, v) -> {
            // 如果是占位符
            if (v instanceof PlaceholderInfo) {
                PlaceholderInfo placeholder = (PlaceholderInfo) v;
                NodeInvokeResult nodeInvokeResult = planArgs.get(placeholder.getId());
                Asserts.assertTrue(nodeInvokeResult != null, "占位符对应的参数不存在");
                JSONArray maps = nodeInvokeResult.getResult();

                String name = placeholder.getName();
                List<Object> collect = maps.stream().map(t -> ((JSONObject) t).get(name)).filter(Objects::nonNull).collect(Collectors.toList());
                if (collect.size() == 1) {
                    Object o = collect.get(0);
                    params.put(k, o);
                } else {
                    params.put(k, collect);
                }
            }
        });
    }

    /**
     * 展平结果
     *
     * @param nodeInvokeResult
     *
     * @return
     */
    protected NodeInvokeResult tileResultJson(NodeInvokeResult nodeInvokeResult) {
        List<FieldInfo> fieldInfos = nodeInvokeResult.getFieldInfos();
        JSONArray result = nodeInvokeResult.getResult();
        if (CollectionUtil.isEmpty(fieldInfos) || CollectionUtil.isEmpty(result)) {
            return nodeInvokeResult;
        }
        boolean change = false;
        Map<String, FieldInfo> fieldInfoMap = fieldInfos.stream().collect(Collectors.toMap(FieldInfo::getFieldName, t -> t));
        JSONArray newResult = new JSONArray();
        // 遍历行
        for (Object resultItem : result) {
            JSONObject resultItemJson = (JSONObject) resultItem;

            JSONObject newLine = new JSONObject();
            List<Map<String, Object>> newResultTemp = new ArrayList<>();
            // 初始只有一行
            newResultTemp.add(newLine);

            // 遍历列
            for (Entry<String, Object> resultCell : resultItemJson.entrySet()) {
                Object value = resultCell.getValue();
                if (value instanceof JSON) {
                    change = true;
                }
                // 一行展开成多行
                if (value instanceof JSONArray) {
                    JSONArray jsonArrayValue = (JSONArray) value;
                    List<Map<String, Object>> newResultTempTemp = new ArrayList<>();
                    for (Object o : jsonArrayValue) {
                        // 每一行都要进行自我复制成多行
                        for (Map<String, Object> item : newResultTemp) {
                            Map<String, Object> copy = MapUtil.copy(item);
                            copy.put(resultCell.getKey(), o);
                            newResultTempTemp.add(copy);
                        }
                    }
                    newResultTemp = newResultTempTemp;
                } else if (value instanceof JSONObject) {
                    List<FieldInfo> fieldInfoTemp = new ArrayList<>(fieldInfos);
                    FieldInfo oldField = fieldInfoMap.remove(resultCell.getKey());
                    fieldInfoTemp.remove(oldField);
                    JSONObject jsonObject = (JSONObject) value;
                    for (Entry<String, Object> objectEntry : jsonObject.entrySet()) {
                        String newFieldName = resultCell.getKey() + "." + objectEntry.getKey();
                        if (!fieldInfoMap.containsKey(newFieldName)) {
                            FieldInfo newFieldInfo = oldField.copyWithNewFieldName(newFieldName);
                            fieldInfoTemp.add(newFieldInfo);
                            fieldInfoMap.put(newFieldName, newFieldInfo);
                        }
                        for (Map<String, Object> stringObjectMap : newResultTemp) {
                            stringObjectMap.remove(resultCell.getKey());
                            stringObjectMap.put(newFieldName, objectEntry.getValue());
                        }
                    }
                    fieldInfos = fieldInfoTemp;
                } else {
                    for (Map<String, Object> item : newResultTemp) {
                        item.put(resultCell.getKey(), resultCell.getValue());
                    }
                }
            }
            newResult.addAll(newResultTemp);
        }
        if (Boolean.FALSE.equals(change)) {
            return nodeInvokeResult;
        }
        NodeInvokeResult nodeInvokeResult1 = new NodeInvokeResult(this);
        nodeInvokeResult1.setFieldInfos(fieldInfos);
        nodeInvokeResult1.setResult(newResult);

        return tileResultJson(nodeInvokeResult1);
    }
}
