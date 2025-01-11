package top.uhyils.usher.content;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import top.uhyils.usher.plan.parser.SqlParser;
import top.uhyils.usher.util.CollectionUtil;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月11日 13时20分
 */
public class ParserContent {

    private static final Collection<SqlParser> PARSERS = new HashSet<>();


    public static void addParser(Collection<SqlParser> parsers) {
        if (CollectionUtil.isNotEmpty(parsers)) {
            parsers.addAll(parsers);
        }
    }

    public static void addParser(SqlParser... parsers) {
        if (CollectionUtil.isNotEmpty(parsers)) {
            List<SqlParser> collect = parsers.stream().collect(Collectors.toList());
            PARSERS.addAll(collect);
        }
    }

    public static Collection<SqlParser> otherParsers() {
        return PARSERS;
    }

}
