package top.uhyils.usher;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import top.uhyils.usher.util.MysqlUtil;

/**
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年11月04日 14时11分
 */
public class FieldInfo {

    /**
     * 目录名称(恒为def)
     */
    private static final byte[] DIR_NAME = MysqlUtil.varString("def");

    /**
     * 字符编码
     */
    private static final byte[] CHAR_SET = new byte[]{(byte) 0xff, 0x00};

    /**
     * 填充值
     */
    private static final byte[] FILL_VALUE = new byte[]{0x0c};

    /**
     * 库名
     */
    private String dbName;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 表原始名称
     */
    private String tableRealName;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段真实名称
     */
    private String fieldRealName;

    /**
     * 列长度
     */
    private int length;

    /**
     * 本次返回在字段列表中的位置
     */
    private int index;


    /**
     * 列类型
     */
    private Class<?> fieldType;

    /**
     * 列标志
     */
    private short fieldMark;

    /**
     * 精度
     */
    private byte accuracy;


    public FieldInfo(String dbName, String tableName, String tableRealName, String fieldName, String fieldRealName, int length, int index, Class<?> fieldType, short fieldMark, byte accuracy) {
        this.dbName = dbName;
        this.tableName = tableName;
        this.tableRealName = tableRealName;
        this.fieldName = fieldName;
        this.fieldRealName = fieldRealName;
        this.length = length;
        this.index = index;
        this.fieldType = fieldType;
        this.fieldMark = fieldMark;
        this.accuracy = accuracy;
    }


    /**
     * 根据新名字复制一个字段
     *
     * @param newFieldName
     *
     * @return
     */
    public FieldInfo copyWithNewFieldName(String newFieldName) {
        return new FieldInfo(this.dbName, this.tableName, this.tableRealName, newFieldName, this.fieldRealName, this.length, this.index, this.fieldType, this.fieldMark, this.accuracy);
    }

    /**
     * 根据新信息复制一个字段
     *
     * @param newTableName 新表名
     * @param newFieldName 新字段名
     *
     * @return
     */
    public FieldInfo copyWithNewFieldName(String newTableName, String newFieldName) {
        return new FieldInfo(this.dbName, newTableName, this.tableRealName, newFieldName, this.fieldRealName, this.length, this.index, this.fieldType, this.fieldMark, this.accuracy);
    }

    /**
     * 根据新名字复制一个字段
     *
     * @param index 复制为第几个
     *
     * @return
     */
    public FieldInfo copyWithNewFieldName(Integer index) {
        return copyWithNewFieldName(this.fieldName + "(" + index + ")");
    }

    public String getFieldName() {
        return fieldName;
    }

    public int getIndex() {
        return index;
    }

    public String getTableName() {
        return tableName;
    }

    /**
     * 获取 表名.列表格式
     *
     * @return
     */
    public String getTableNameDotFieldName() {
        if (StringUtils.isNotEmpty(tableName)) {
            return tableName + "." + fieldName;
        }
        return fieldName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FieldInfo fieldInfo = (FieldInfo) o;
        return length == fieldInfo.length && index == fieldInfo.index && fieldMark == fieldInfo.fieldMark && accuracy == fieldInfo.accuracy && Objects.equals(dbName, fieldInfo.dbName) && Objects.equals(tableName, fieldInfo.tableName) && Objects.equals(tableRealName, fieldInfo.tableRealName) && Objects.equals(fieldName, fieldInfo.fieldName) && Objects.equals(fieldRealName, fieldInfo.fieldRealName) && fieldType == fieldInfo.fieldType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dbName, tableName, tableRealName, fieldName, fieldRealName, length, index, fieldType, fieldMark, accuracy);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("dbName", dbName)
            .append("tableName", tableName)
            .append("tableRealName", tableRealName)
            .append("fieldName", fieldName)
            .append("fieldRealName", fieldRealName)
            .append("length", length)
            .append("index", index)
            .append("fieldType", fieldType)
            .append("fieldMark", fieldMark)
            .append("accuracy", accuracy)
            .toString();
    }
}
