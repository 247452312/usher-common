package top.uhyils.usher.enums;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月10日 09时45分
 */
public enum DefaultSupportTypeEnum {
    HTTP("HTTP", "HTTP"),
    SQL("SQL", "SQL"),
    ;

    private final String type;

    private final String name;

    DefaultSupportTypeEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
