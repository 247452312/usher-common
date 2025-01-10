package top.uhyils.usher.mysql.pojo.sys;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import top.uhyils.usher.content.CallNodeContent;
import top.uhyils.usher.context.LoginInfoHelper;
import top.uhyils.usher.enums.FieldTypeEnum;
import top.uhyils.usher.mysql.handler.MysqlServiceHandler;
import top.uhyils.usher.mysql.pojo.DTO.TableDTO;
import top.uhyils.usher.node.DatabaseInfo;
import top.uhyils.usher.pojo.DTO.UserDTO;
import top.uhyils.usher.pojo.FieldInfo;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.util.Asserts;
import top.uhyils.usher.util.CollectionUtil;
import top.uhyils.usher.util.StringUtil;

/**
 * information_schema库 SCHEMATA表
 * 提供了关于数据库中的库的信息。详细表述了某个库的名称，默认编码，排序规则。
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年09月01日 14时22分
 */
public class ISchemata extends AbstractSysTable {


    public ISchemata(MysqlServiceHandler handler, Map<String, Object> params) {
        super(handler, params);
        this.params = params.entrySet().stream().collect(Collectors.toMap(t -> t.getKey().toLowerCase(), Entry::getValue));
    }

    @Override
    public NodeInvokeResult doGetResultNoParams() {
        String schemaName = (String) params.get("table_schema");
        Optional<UserDTO> userOptional = LoginInfoHelper.get();
        if (!userOptional.isPresent()) {
            throw Asserts.makeException("未登录");
        }
        List<TableDTO> callNodeDTOS = handler.findTableByCompanyAndDatabase(userOptional.get().getId(), schemaName);

        JSONArray newResults = new JSONArray();

        Set<String> dbSet = new HashSet<>();
        callNodeDTOS.stream().filter(t -> {
            String database = t.getDatabase();
            if (dbSet.contains(database)) {
                return false;
            } else {
                dbSet.add(database);
                return true;
            }
        }).forEach(t -> {
            DatabaseInfo databaseInfo = new DatabaseInfo();
            databaseInfo.setCatalogName(CallNodeContent.CATALOG_NAME);
            databaseInfo.setSchemaName(t.getDatabase());
            databaseInfo.setDefaultCharacterSetName(CallNodeContent.DEFAULT_CHARACTER_SET_NAME);
            databaseInfo.setDefaultCollationName(CallNodeContent.DEFAULT_COLLATION_NAME);
            databaseInfo.setSqlPath(null);
            databaseInfo.setDefaultEncryption("NO");
            newResults.add(JSONObject.parseObject(JSONObject.toJSONString(databaseInfo)));
        });

        NodeInvokeResult nodeInvokeResult = new NodeInvokeResult(null);
        if (CollectionUtil.isNotEmpty(newResults)) {
            List<Map<String, Object>> tempResults = new ArrayList<>();
            JSONObject first = newResults.getJSONObject(0);
            Map<String, String> fieldNameMap = first.keySet().stream().collect(Collectors.toMap(t -> t, t -> StringUtil.toUnderline(t).toUpperCase()));
            for (int i = 0; i < newResults.size(); i++) {
                JSONObject newResult = newResults.getJSONObject(i);
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
        fieldInfos.add(new FieldInfo("information_schema", "schemata", "schemata", "CATALOG_NAME", "CATALOG_NAME", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "schemata", "schemata", "SCHEMA_NAME", "SCHEMA_NAME", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "schemata", "schemata", "DEFAULT_CHARACTER_SET_NAME", "DEFAULT_CHARACTER_SET_NAME", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "schemata", "schemata", "DEFAULT_COLLATION_NAME", "DEFAULT_COLLATION_NAME", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "schemata", "schemata", "SQL_PATH", "SQL_PATH", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "schemata", "schemata", "DEFAULT_ENCRYPTION", "DEFAULT_ENCRYPTION", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        nodeInvokeResult.setFieldInfos(fieldInfos);
        return nodeInvokeResult;
    }

    /**
     * 是否包含
     *
     * @param schemaNameStrList
     * @param schemaName
     *
     * @return
     */
    private boolean containsLike(List<String> schemaNameStrList, String schemaName) {
        boolean result = false;
        for (String s : schemaNameStrList) {
            if (schemaName.startsWith(s)) {
                result = true;
                break;
            }
        }
        return result;
    }
}
