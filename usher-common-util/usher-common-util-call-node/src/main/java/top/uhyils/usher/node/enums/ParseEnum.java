package top.uhyils.usher.node.enums;

import java.util.ArrayList;
import java.util.List;
import top.uhyils.usher.node.plan.parser.BlockQuerySelectSqlParser;
import top.uhyils.usher.node.plan.parser.DeleteSqlParser;
import top.uhyils.usher.node.plan.parser.InsertSqlParser;
import top.uhyils.usher.node.plan.parser.SqlParser;
import top.uhyils.usher.node.plan.parser.UnionSelectSqlParser;
import top.uhyils.usher.node.plan.parser.UpdateSqlParser;
import top.uhyils.usher.ustream.UStream;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月09日 09时05分
 */
public enum ParseEnum {
    BLOCK_QUERY(1, "普通查询", new BlockQuerySelectSqlParser()),
    UNION_QUERY(2, "union查询", new UnionSelectSqlParser()),
    UPDATE(3, "update语句", new UpdateSqlParser()),
    DELETE(4, "删除语句", new DeleteSqlParser()),
    INSERT(5, "插入语句", new InsertSqlParser()),

    ;

    private final Integer code;

    private final String name;

    private final SqlParser sqlParser;

    ParseEnum(Integer code, String name, SqlParser sqlParser) {
        this.code = code;
        this.name = name;
        this.sqlParser = sqlParser;
    }

    public static ParseEnum getByCode(Integer code) {
        for (ParseEnum value : ParseEnum.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 所有解析器
     *
     * @return
     */
    public static List<SqlParser> allParser(List<SqlParser> list) {
        List<SqlParser> result;
        if (list == null) {
            result = new ArrayList<>();
        } else {
            result = new ArrayList<>(list);
        }
        result.addAll(UStream.of(values()).map(ParseEnum::getSqlParser).toList());
        return result;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public SqlParser getSqlParser() {
        return sqlParser;
    }
}
