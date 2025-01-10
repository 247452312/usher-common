package top.uhyils.usher.enums;

import java.util.Objects;

/**
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年11月04日 08时24分
 */
public enum QuerySqlTypeEnum {
    /**
     * 查询
     */
    QUERY("QUERY"),
    /**
     * 修改
     */
    UPDATE("UPDATE"),
    /**
     * 插入
     */
    INSERT("INSERT"),
    /**
     * 删除
     */
    DELETE("DELETE"),

    /**
     * 未知
     */
    NULL("NULL");

    private final String code;

    QuerySqlTypeEnum(String code) {
        this.code = code;
    }

    public static QuerySqlTypeEnum findByName(String querySqlType) {
        for (QuerySqlTypeEnum value : values()) {
            if (Objects.equals(value.code, querySqlType)) {
                return value;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }
}
