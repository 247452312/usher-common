package top.uhyils.usher.util;

import com.alibaba.fastjson.JSON;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import top.uhyils.usher.enums.FieldTypeEnum;
import top.uhyils.usher.pojo.FieldInfo;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月10日 10时05分
 */
public class FieldUtil {

    public static List<FieldInfo> makeFieldInfo(String dbName, String tableName, String tableRealName, int startIndex, List<Map<String, Object>> values) {
        if (values.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, Object> jsonObject = values.get(0);
        List<FieldInfo> fieldInfos = new ArrayList<>();
        for (Entry<String, Object> entry : jsonObject.entrySet()) {
            Object value = entry.getValue();
            FieldTypeEnum type = null;
            if (value instanceof String) {
                type = FieldTypeEnum.FIELD_TYPE_VARCHAR;
            } else if (value instanceof Integer) {
                type = FieldTypeEnum.FIELD_TYPE_INT24;
            } else if (value instanceof Long) {
                type = FieldTypeEnum.FIELD_TYPE_LONGLONG;
            } else if (value instanceof Boolean) {
                type = FieldTypeEnum.FIELD_TYPE_TINY;
            } else if (value instanceof Double) {
                type = FieldTypeEnum.FIELD_TYPE_DOUBLE;
            } else if (value instanceof JSON) {
                type = FieldTypeEnum.FIELD_TYPE_VARCHAR;
            } else if (value == null) {
                type = FieldTypeEnum.FIELD_TYPE_NULL;
            } else {
                Asserts.throwException("未找到的字段类型:{}", value.getClass().getName());
            }
            fieldInfos.add(new FieldInfo(dbName, tableName, tableRealName, entry.getKey(), entry.getKey(), 0, startIndex++, type, (short) 0, (byte) 0));
        }
        return fieldInfos;
    }

}
