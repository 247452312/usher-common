package top.uhyils.usher.mysql.pojo.sys;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import top.uhyils.usher.context.LoginInfoHelper;
import top.uhyils.usher.enums.FieldTypeEnum;
import top.uhyils.usher.mysql.handler.MysqlServiceHandler;
import top.uhyils.usher.mysql.pojo.DTO.GlobalVariablesInfo;
import top.uhyils.usher.mysql.util.MysqlUtil;
import top.uhyils.usher.pojo.DTO.UserDTO;
import top.uhyils.usher.pojo.FieldInfo;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.pojo.SqlGlobalVariables;
import top.uhyils.usher.util.Asserts;
import top.uhyils.usher.util.CollectionUtil;
import top.uhyils.usher.util.StringUtil;

/**
 * 系统变量
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年09月22日 09时30分
 */
public class PGlobalVariables extends AbstractSysTable {
    //
    //    /**
    //     * mysql全局系统参数
    //     */
    //    private MysqlGlobalVariables mysqlGlobalVariables;

    public PGlobalVariables(MysqlServiceHandler handler, Map<String, Object> params) {
        super(handler, params);
        this.params = params.entrySet().stream().collect(Collectors.toMap(t -> t.getKey().toLowerCase(), Entry::getValue));
    }


    @Override
    public NodeInvokeResult doGetResultNoParams() {

        Optional<UserDTO> userOptional = LoginInfoHelper.get();
        if (!userOptional.isPresent()) {
            throw Asserts.makeException("未登录");
        }
        String variableName = (String) params.get("variable_name");
        SqlGlobalVariables variables = handler.findMysqlGlobalVariables();
        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(variables));
        List<Map<String, Object>> newResults = new ArrayList<>();
        jsonObject.entrySet().stream().filter(t -> {
            String key = t.getKey();
            return MysqlUtil.likeMatching(key, Collections.singletonList(variableName));
        }).forEach(t -> {
            GlobalVariablesInfo globalVariablesInfo = new GlobalVariablesInfo();
            globalVariablesInfo.setVariableName(t.getKey());
            globalVariablesInfo.setVariableValue(t.getValue());
            newResults.add(JSONObject.parseObject(JSONObject.toJSONString(globalVariablesInfo)));
        });

        NodeInvokeResult nodeInvokeResult = new NodeInvokeResult(null);
        if (CollectionUtil.isNotEmpty(newResults)) {
            List<Map<String, Object>> tempResults = new ArrayList<>();
            Map<String, Object> first = newResults.get(0);
            Map<String, String> fieldNameMap = first.keySet().stream().collect(Collectors.toMap(t -> t, t -> StringUtil.toUnderline(t).toUpperCase()));
            for (int i = 0; i < newResults.size(); i++) {
                Map<String, Object> newResult = newResults.get(i);
                Map<String, Object> tempNewResultMap = new HashMap<>(newResult.size());
                for (Entry<String, Object> newResultItem : newResult.entrySet()) {
                    String key = newResultItem.getKey();
                    Object value = newResultItem.getValue();
                    tempNewResultMap.put(fieldNameMap.get(key), value);
                }
                tempResults.add(tempNewResultMap);
            }
            newResults.clear();
            newResults.addAll(tempResults);
        }
        nodeInvokeResult.setResult(newResults);
        List<FieldInfo> fieldInfos = new ArrayList<>();
        fieldInfos.add(new FieldInfo("performance_schema", "global_variables", "global_variables", "VARIABLE_NAME", "VARIABLE_NAME", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("performance_schema", "global_variables", "global_variables", "VARIABLE_VALUE", "VARIABLE_VALUE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        nodeInvokeResult.setFieldInfos(fieldInfos);
        return nodeInvokeResult;
    }
}
