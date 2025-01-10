package top.uhyils.usher.mysql.pojo.DTO;


import top.uhyils.usher.mysql.enums.FieldTypeToByteEnum;

/**
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年11月06日 16时33分
 */
public class PrepareParamInfo {

    /**
     * 预处理参数类型
     */
    private final FieldTypeToByteEnum parse;

    /**
     * 预处理参数
     */
    private final Object paramValue;

    public PrepareParamInfo(FieldTypeToByteEnum parse, Object paramValue) {
        this.parse = parse;
        this.paramValue = paramValue;
    }

    public FieldTypeToByteEnum getParse() {
        return parse;
    }

    public Object getParamValue() {
        return paramValue;
    }
}
