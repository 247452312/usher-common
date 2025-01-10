package top.uhyils.usher.enums;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import top.uhyils.usher.pojo.FieldInfo;
import top.uhyils.usher.util.Asserts;

/**
 * sql列类型
 *
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年11月04日 14时27分
 */
public enum FieldTypeEnum {
    /**
     * sql列类型
     */
    FIELD_TYPE_DECIMAL((byte) 0x00, BigDecimal.class),
    FIELD_TYPE_TINY((byte) 0x01, Integer.class),
    FIELD_TYPE_SHORT((byte) 0x02, Integer.class),
    FIELD_TYPE_LONG((byte) 0x03, Long.class),
    FIELD_TYPE_FLOAT((byte) 0x04, Float.class),
    FIELD_TYPE_DOUBLE((byte) 0x05, Double.class),
    FIELD_TYPE_NULL((byte) 0x06, null),
    FIELD_TYPE_TIMESTAMP((byte) 0x07, LocalDateTime.class),
    FIELD_TYPE_LONGLONG((byte) 0x08, Long.class),
    FIELD_TYPE_INT24((byte) 0x09, Integer.class),
    FIELD_TYPE_DATE((byte) 0x0A, LocalDate.class),
    FIELD_TYPE_TIME((byte) 0x0B, LocalTime.class),
    FIELD_TYPE_DATETIME((byte) 0x0C, LocalDateTime.class),
    FIELD_TYPE_YEAR((byte) 0x0D, Integer.class),
    FIELD_TYPE_NEWDATE((byte) 0x0E, Integer.class),
    FIELD_TYPE_VARCHAR((byte) 0x0F, String.class),
    FIELD_TYPE_BIT((byte) 0x10, Byte.class),
    FIELD_TYPE_NEWDECIMAL((byte) 0xF6, BigDecimal.class),
    FIELD_TYPE_ENUM((byte) 0xF7, String.class),
    FIELD_TYPE_SET((byte) 0xF8, Integer.class),
    FIELD_TYPE_TINY_BLOB((byte) 0xF9, byte[].class),
    FIELD_TYPE_MEDIUM_BLOB((byte) 0xFA, byte[].class),
    FIELD_TYPE_LONG_BLOB((byte) 0xFB, byte[].class),
    FIELD_TYPE_BLOB((byte) 0xFC, byte[].class),
    FIELD_TYPE_VAR_STRING((byte) 0xFD, String.class),
    FIELD_TYPE_STRING((byte) 0xFE, String.class),
    FIELD_TYPE_GEOMETRY((byte) 0xFF, String.class);

    private final byte code;

    private final Class<?> clazz;


    FieldTypeEnum(byte code, Class<?> clazz) {
        this.code = code;
        this.clazz = clazz;
    }

    public static FieldTypeEnum parse(byte code) {
        for (FieldTypeEnum value : values()) {
            if (Objects.equals(value.getCode(), code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 将结果封装为字段
     *
     * @param obj       结果
     * @param index     index
     * @param fieldName 字段名称
     *
     * @return
     */
    public static FieldInfo makeFieldInfo(String dbName, String tableName, String tableRealName, Object obj, int index, String fieldName) {
        Class<?> clazz = obj.getClass();
        return makeFieldInfo(dbName, tableName, tableRealName, clazz, index, fieldName);
    }

    @NotNull
    public static FieldInfo makeFieldInfo(String dbName, String tableName, String tableRealName, Class<?> clazz, int index, String fieldName) {
        if (Number.class.isAssignableFrom(clazz)) {
            return new FieldInfo(dbName, tableName, tableRealName, fieldName, fieldName, 0, index, FIELD_TYPE_FLOAT, (short) 0, (byte) 0);
        } else if (String.class.isAssignableFrom(clazz)) {
            return new FieldInfo(dbName, tableName, tableRealName, fieldName, fieldName, 0, index, FIELD_TYPE_VARCHAR, (short) 0, (byte) 0);
        } else {
            throw Asserts.makeException("未知的字段类型:{}", clazz.getName());
        }
    }

    public byte getCode() {
        return code;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}
