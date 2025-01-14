package top.uhyils.usher.mysql.pojo.sys;

import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import top.uhyils.usher.context.LoginInfoHelper;
import top.uhyils.usher.enums.FieldTypeEnum;
import top.uhyils.usher.mysql.enums.TableTypeEnum;
import top.uhyils.usher.mysql.handler.MysqlServiceHandler;
import top.uhyils.usher.mysql.pojo.DTO.TableDTO;
import top.uhyils.usher.mysql.pojo.DTO.TableInfo;
import top.uhyils.usher.pojo.DTO.UserDTO;
import top.uhyils.usher.pojo.FieldInfo;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.util.Asserts;
import top.uhyils.usher.util.CollectionUtil;
import top.uhyils.usher.util.StringUtil;

/**
 * information_schema.TABLES 表
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年09月08日 09时13分
 */
public class ITables extends AbstractSysTable {

    public ITables(MysqlServiceHandler handler, Map<String, Object> params) {
        super(handler, params);
        this.params = params.entrySet().stream().collect(Collectors.toMap(t -> t.getKey().toLowerCase(), Entry::getValue));
    }

    @Override
    public NodeInvokeResult doGetResultNoParams() {
        String schemaNames = (String) params.get("table_schema");
        Optional<UserDTO> userOptional = LoginInfoHelper.get();
        if (!userOptional.isPresent()) {
            throw Asserts.makeException("未登录");
        }
        List<TableDTO> callNodeDTOS = handler.findTableByCompanyAndDatabase(userOptional.get().getId(), Collections.singletonList(schemaNames));

        List<Map<String, Object>> newResults = new ArrayList<>();
        Set<String> dbSet = new HashSet<>();
        callNodeDTOS.stream().filter(t -> {
            if (dbSet.contains(t.getDatabase())) {
                return false;
            } else {
                dbSet.add(t.getDatabase());
                return true;
            }
        }).forEach(t -> {
            TableInfo tableInfo = new TableInfo();
            tableInfo.setTableSchema(t.getDatabase());
            tableInfo.setTableName(t.getTable());
            tableInfo.setTableType(TableTypeEnum.BASE_TABLE);
            newResults.add(JSONObject.parseObject(JSONObject.toJSONString(tableInfo)));
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
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "TABLE_CATALOG", "TABLE_CATALOG", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "TABLE_SCHEMA", "TABLE_SCHEMA", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "TABLE_NAME", "TABLE_NAME", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "CREATE_OPTIONS", "CREATE_OPTIONS", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "TABLE_COMMENT", "TABLE_COMMENT", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "TABLE_TYPE", "TABLE_TYPE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "ENGINE", "ENGINE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "VERSION", "VERSION", 0, 1, FieldTypeEnum.FIELD_TYPE_INT24, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "ROW_FORMAT", "ROW_FORMAT", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "TABLE_ROWS", "TABLE_ROWS", 0, 1, FieldTypeEnum.FIELD_TYPE_INT24, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "AVG_ROW_LENGTH", "AVG_ROW_LENGTH", 0, 1, FieldTypeEnum.FIELD_TYPE_INT24, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "DATA_LENGTH", "DATA_LENGTH", 0, 1, FieldTypeEnum.FIELD_TYPE_INT24, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "MAX_DATA_LENGTH", "MAX_DATA_LENGTH", 0, 1, FieldTypeEnum.FIELD_TYPE_INT24, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "INDEX_LENGTH", "INDEX_LENGTH", 0, 1, FieldTypeEnum.FIELD_TYPE_INT24, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "DATA_FREE", "DATA_FREE", 0, 1, FieldTypeEnum.FIELD_TYPE_INT24, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "AUTO_INCREMENT", "AUTO_INCREMENT", 0, 1, FieldTypeEnum.FIELD_TYPE_INT24, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "CREATE_TIME", "CREATE_TIME", 0, 1, FieldTypeEnum.FIELD_TYPE_TIMESTAMP, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "UPDATE_TIME", "UPDATE_TIME", 0, 1, FieldTypeEnum.FIELD_TYPE_TIMESTAMP, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "CHECK_TIME", "CHECK_TIME", 0, 1, FieldTypeEnum.FIELD_TYPE_TIMESTAMP, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "TABLE_COLLATION", "TABLE_COLLATION", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "tables", "tables", "CHECKSUM", "CHECKSUM", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));

        nodeInvokeResult.setFieldInfos(fieldInfos);
        return nodeInvokeResult;
    }
}
