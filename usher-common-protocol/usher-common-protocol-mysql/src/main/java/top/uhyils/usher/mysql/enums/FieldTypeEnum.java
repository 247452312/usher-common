package top.uhyils.usher.mysql.enums;

import java.util.Objects;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import top.uhyils.usher.mysql.pojo.DTO.FieldInfo;
import top.uhyils.usher.mysql.util.Proto;
import top.uhyils.usher.util.Asserts;

/**
 * mysql列类型
 *
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年11月04日 14时27分
 */
public enum FieldTypeEnum {
    /**
     * mysql列类型
     */
    FIELD_TYPE_DECIMAL((byte) 0x00, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_TINY((byte) 0x01, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_SHORT((byte) 0x02, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_LONG((byte) 0x03, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_FLOAT((byte) 0x04, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_DOUBLE((byte) 0x05, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_NULL((byte) 0x06, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_TIMESTAMP((byte) 0x07, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_LONGLONG((byte) 0x08, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_INT24((byte) 0x09, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_DATE((byte) 0x0A, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_TIME((byte) 0x0B, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_DATETIME((byte) 0x0C, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_YEAR((byte) 0x0D, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_NEWDATE((byte) 0x0E, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_VARCHAR((byte) 0x0F, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_BIT((byte) 0x10, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_NEWDECIMAL((byte) 0xF6, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_ENUM((byte) 0xF7, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_SET((byte) 0xF8, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_TINY_BLOB((byte) 0xF9, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_MEDIUM_BLOB((byte) 0xFA, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_LONG_BLOB((byte) 0xFB, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_BLOB((byte) 0xFC, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_VAR_STRING((byte) 0xFD, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_STRING((byte) 0xFE, input -> {
        return input.getFixedInt(4);
    }),
    FIELD_TYPE_GEOMETRY((byte) 0xFF, input -> {
        return input.getFixedInt(4);
    });

    private final byte code;

    /**
     * 解析byte
     */
    private final Function<Proto, Object> parseByte;

    FieldTypeEnum(byte code, Function<Proto, Object> parseByte) {
        this.code = code;
        this.parseByte = parseByte;
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

    /**
     * 执行解析
     *
     * @return
     */
    public Object invokeProto(Proto proto) {
        return parseByte.apply(proto);
    }

}
