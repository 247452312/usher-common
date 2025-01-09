package top.uhyils.usher.util;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumericLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import top.uhyils.usher.FieldInfo;
import top.uhyils.usher.NodeInvokeResult;
import top.uhyils.usher.annotation.NotNull;
import top.uhyils.usher.sql.ExprParseResultInfo;

/**
 * mysql协议解析方法
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年03月18日 08时51分
 */
public final class MysqlUtil {

    /**
     * 单引号前缀
     */
    private static final String QUOTES_PREFIX = "`";

    public static byte[] varString(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[bytes.length + 1];
        result[0] = (byte) bytes.length;
        System.arraycopy(bytes, 0, result, 1, bytes.length);
        return result;
    }

    /**
     * 解析str
     *
     * @param arg                局部字符串
     * @param allResult          之前所有执行计划的结果
     * @param parentInvokeResult 上一个执行计划的结果
     *
     * @return
     */
    @NotNull
    public static <T> ExprParseResultInfo<T> parse(SQLExpr arg, Map<Long, NodeInvokeResult> allResult, NodeInvokeResult parentInvokeResult) {
        // &开头的都是变量,代表着要从之前其他的执行计划结果中获取值
        if (arg instanceof SQLCharExpr && MysqlStringUtil.cleanQuotation(((SQLCharExpr) arg).getText()).startsWith("&")) {
            String planId = ((SQLCharExpr) arg).getText().substring(1);
            // 带`.`符号的都是获取内部变量
            if (planId.contains(".")) {
                String[] split = planId.split("\\.");
                planId = split[0];
                NodeInvokeResult nodeInvokeResult = allResult.get(Long.parseLong(planId));
                Asserts.assertTrue(nodeInvokeResult != null, "未找到临时变量对应的执行计划");
                String fieldName = MysqlStringUtil.repeatStr(split[1]);
                String defaultValue = null;
                // :代表设置了默认值 同spring的value :之前是变量名 后面是默认值,默认是字符串类型,如果只有一个:(例XXX:)则默认值为空字符串, 如果没有设置:且没有值,则为null
                if (fieldName.contains(":")) {
                    defaultValue = fieldName.substring(fieldName.indexOf(":") + 1);
                    fieldName = fieldName.substring(0, fieldName.indexOf(":"));
                }
                List<FieldInfo> fieldInfos = nodeInvokeResult.getFieldInfos();
                String finalFieldName = fieldName;
                Optional<FieldInfo> first = fieldInfos.stream().filter(t -> Objects.equals(t.getFieldName(), finalFieldName)).findFirst();
                if (!first.isPresent()) {
                    return ExprParseResultInfo.buildConstant((T) defaultValue);
                } else {
                    JSONArray result = nodeInvokeResult.getResult();
                    List<T> collect = result.stream().map(t -> (T) ((JSONObject) t).get(finalFieldName)).collect(Collectors.toList());
                    return ExprParseResultInfo.buildListConstant(collect);
                }
            } else {
                NodeInvokeResult nodeInvokeResult = allResult.get(Long.parseLong(planId));
                Asserts.assertTrue(nodeInvokeResult != null, "未找到临时变量对应的执行计划");
                Asserts.assertTrue(nodeInvokeResult.getFieldInfos().size() == 1, "方法入参不能是多列");
                FieldInfo fieldInfo = nodeInvokeResult.getFieldInfos().get(0);
                JSONArray result = nodeInvokeResult.getResult();
                List<T> collect = result.stream().map(t -> (T) ((JSONObject) t).get(fieldInfo.getFieldName())).collect(Collectors.toList());
                return ExprParseResultInfo.buildListConstant(collect);

            }
        } else if (arg instanceof SQLCharExpr) {
            String text = ((SQLCharExpr) arg).getText();
            return ExprParseResultInfo.buildConstant((T) text);
        } else if (arg instanceof SQLNumericLiteralExpr) {
            Number value = ((SQLNumericLiteralExpr) arg).getNumber();
            return ExprParseResultInfo.buildConstant((T) value);
        } else if (arg instanceof SQLIdentifierExpr) {
            String name = ((SQLIdentifierExpr) arg).getName();
            List<T> collect = parentInvokeResult.getResult().stream().map(t -> (T) ((JSONObject) t).get(name)).collect(Collectors.toList());
            return ExprParseResultInfo.buildListConstant(collect);
        } else if (arg instanceof SQLPropertyExpr) {
            // 出现情况 sql参数中带有concat(a.id,b.uu) 这种情况解析a.id时会命中这里
            String owner = ((SQLPropertyExpr) arg).getOwnernName();
            String name = ((SQLPropertyExpr) arg).getName();
            Optional<FieldInfo> first = parentInvokeResult.getFieldInfos().stream().filter(t -> {
                if (owner != null) {
                    boolean equals = Objects.equals(t.getTableName(), owner);
                    if (!equals) {
                        return false;
                    }
                }
                return t.getFieldName().equals(name);
            }).findFirst();
            if (!first.isPresent()) {
                return ExprParseResultInfo.buildConstant(null);
            }
            List<T> collect = parentInvokeResult.getResult().stream().map(t -> (T) ((JSONObject) t).get(first.get().getFieldName())).collect(Collectors.toList());
            return ExprParseResultInfo.buildListConstant(collect);
        }
        Asserts.throwException("未找到解析方法入参的类型");
        return null;
    }

    /**
     * 去除表名中的单引号
     *
     * @param table
     *
     * @return
     */
    private static String removeQuotes(String table) {
        if (table.startsWith(QUOTES_PREFIX) && table.endsWith(QUOTES_PREFIX)) {
            return table.substring(1, table.length() - 1);
        }
        return table;
    }

}
