package top.uhyils.usher.enums;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import top.uhyils.usher.annotation.NotNull;
import top.uhyils.usher.content.CallNodeContent;
import top.uhyils.usher.content.CallerUserInfo;
import top.uhyils.usher.pojo.DTO.UserDTO;
import top.uhyils.usher.pojo.FieldInfo;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.sql.ExprParseResultInfo;
import top.uhyils.usher.util.Asserts;
import top.uhyils.usher.util.StringUtil;
import top.uhyils.usher.util.UsherSqlUtil;

/**
 * sql 方法
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2023年04月25日 16时45分
 */
public enum SqlMethodEnum {
    /**
     * count
     */
    COUNT("count", 1, Long.class, true, (lastAllPlanResult, parentInvokeResult, arguments, fieldName) -> {
        Asserts.assertTrue(arguments.size() == 1, "sql语句中方法使用错误,count入参只能为一个");
        String countParam = arguments.get(0).toString();
        Asserts.assertTrue(StringUtil.isNotEmpty(countParam), "sql语句中方法使用错误,count入参不能为空白");

        if (StringUtil.equalsIgnoreCase(countParam, "*")) {
            // todo 这里需要添加 sql语句中有group的情况
            int size = parentInvokeResult.getResult().size();

            JSONArray maps = new JSONArray();
            JSONObject e = new JSONObject();
            e.put(fieldName, size);
            maps.add(e);
            return maps;
        } else if (countParam.contains("=")) {
            Asserts.throwException("暂不支持count方法中使用等号来判断");
            return null;
        } else {
            // count中为字段名称
            JSONArray result = parentInvokeResult.getResult();
            long size = result.stream().filter(t -> ((JSONObject) t).containsKey(fieldName) && ((JSONObject) t).get(fieldName) != null).count();
            JSONArray maps = new JSONArray();
            JSONObject e = new JSONObject();
            e.put(fieldName, size);
            maps.add(e);
            return maps;
        }
    }),

    /**
     * concat
     */
    CONCAT("concat", -1, String.class, false, (lastAllPlanResult, parentInvokeResult, arguments, fieldName) -> {
        List<StringBuilder> argList = new ArrayList<>();
        for (SQLExpr sqlExpr : arguments) {
            ExprParseResultInfo<String> parse = UsherSqlUtil.parse(sqlExpr, lastAllPlanResult, parentInvokeResult);
            if (parse.isConstant()) {
                int lineIndex = 0;
                String result = parse.get();
                concatAppendString(argList, lineIndex, result);
            } else {
                List<String> listResult = parse.getListResult();
                for (int lineIndex = 0; lineIndex < listResult.size(); lineIndex++) {
                    concatAppendString(argList, lineIndex, listResult.get(lineIndex));
                }
            }
        }

        JSONArray result = new JSONArray();
        for (StringBuilder stringBuilder : argList) {
            String string = stringBuilder.toString();
            JSONObject item = new JSONObject();
            item.put(fieldName, string);
            result.add(item);
        }
        return result;
        // todo 这里还要考虑 select count(a.id) from xxx 的情况
    }),

    /**
     * left
     */
    LEFT("left", 2, String.class, false, (lastAllPlanResult, parentInvokeResult, arguments, fieldName) -> {

        ExprParseResultInfo<String> leftStr = UsherSqlUtil.parse(arguments.get(0), lastAllPlanResult, parentInvokeResult);
        ExprParseResultInfo<Number> rightNumber = UsherSqlUtil.parse(arguments.get(1), lastAllPlanResult, parentInvokeResult);

        JSONArray result = new JSONArray();
        // 如果两边都是常量,size为1 否则 哪边是列表size是哪边 如果两边都是,则使用左边字符串为准
        long size = !leftStr.isConstant() ? leftStr.getListResult().size() : (!rightNumber.isConstant() ? rightNumber.getListResult().size() : 1);
        for (int i = 0; i < size; i++) {
            JSONObject item = new JSONObject();
            Number ch = rightNumber.isConstant() ? rightNumber.get() : rightNumber.get(i);
            String ls = leftStr.isConstant() ? leftStr.get() : leftStr.get(i);
            item.put(fieldName, ls.substring(0, ch.intValue()));
            result.add(item);
        }
        return result;
    }),
    /**
     * instr 返回字符串中第一次出现指定字符串的地方
     */
    INSTR("instr", -1, String.class, false, (lastAllPlanResult, parentInvokeResult, arguments, fieldName) -> {
        ExprParseResultInfo<String> leftStr = UsherSqlUtil.parse(arguments.get(0), lastAllPlanResult, parentInvokeResult);
        ExprParseResultInfo<String> rightStr = UsherSqlUtil.parse(arguments.get(1), lastAllPlanResult, parentInvokeResult);

        // 如果两边都是常量,size为1 否则 哪边是列表size是哪边 如果两边都是,则使用左边字符串为准
        long size = !leftStr.isConstant() ? leftStr.getListResult().size() : (!rightStr.isConstant() ? rightStr.getListResult().size() : 1);

        JSONArray result = new JSONArray();
        for (int i = 0; i < size; i++) {
            String ls = leftStr.isConstant() ? leftStr.get() : leftStr.get(i);
            String rs = rightStr.isConstant() ? rightStr.get() : rightStr.get(i);
            JSONObject item = new JSONObject();
            item.put(fieldName, ls.indexOf(rs));
            result.add(item);
        }
        return result;
    }),
    SUM("sum", 1, Long.class, true, (lastAllPlanResult, parentInvokeResult, arguments, fieldName) -> {
        Asserts.assertTrue(arguments.size() == 1, "sql语句中方法使用错误,sum入参只能为一个");
        String countParam = arguments.get(0).toString();
        Asserts.assertTrue(StringUtil.isNotEmpty(countParam), "sql语句中方法使用错误,sum入参不能为空白");

        if (StringUtil.equalsIgnoreCase(countParam, "*")) {
            Asserts.throwException("sql语句中方法使用错误,sum入参不能为*");
            return null;
        } else if (countParam.contains("=")) {
            Asserts.throwException("暂不支持sum方法中使用等号来判断");
            return null;
        } else {
            // count中为字段名称
            JSONArray result = parentInvokeResult.getResult();
            long sum = result.stream().filter(t -> ((JSONObject) t).containsKey(fieldName) && ((JSONObject) t).get(fieldName) != null).mapToLong(t -> (long) ((JSONObject) t).get(fieldName)).sum();

            JSONArray maps = new JSONArray();
            JSONObject item = new JSONObject();
            item.put(fieldName, sum);
            maps.add(item);
            return maps;
        }
    }),
    VERSION("version", 0, String.class, false, (lastAllPlanResult, parentInvokeResult, arguments, fieldName) -> {

        JSONArray maps = new JSONArray();
        JSONObject item = new JSONObject();
        item.put(fieldName, CallNodeContent.VERSION);
        maps.add(item);
        return maps;
    }),
    DATABASE("database", 0, String.class, false, (lastAllPlanResult, parentInvokeResult, arguments, fieldName) -> {
        CallerUserInfo value = CallNodeContent.CALLER_INFO.get();

        JSONArray maps = new JSONArray();
        JSONObject item = new JSONObject();
        item.put(fieldName, value.getDatabaseName());
        maps.add(item);
        return maps;
    }),
    SCHEMA("schema", 0, String.class, false, (lastAllPlanResult, parentInvokeResult, arguments, fieldName) -> {
        CallerUserInfo value = CallNodeContent.CALLER_INFO.get();

        JSONArray maps = new JSONArray();
        JSONObject item = new JSONObject();
        item.put(fieldName, value.getDatabaseName());
        maps.add(item);
        return maps;
    }),
    USER("user", 0, String.class, false, (lastAllPlanResult, parentInvokeResult, arguments, fieldName) -> {
        CallerUserInfo value = CallNodeContent.CALLER_INFO.get();
        UserDTO userDTO = value.getUserDTO();

        JSONArray maps = new JSONArray();
        JSONObject item = new JSONObject();
        item.put(fieldName, userDTO.getUsername() + "@" + userDTO.getIp());
        maps.add(item);
        return maps;
    }),
    LOWER("lower", 1, String.class, false, (lastAllPlanResult, parentInvokeResult, arguments, fieldName) -> {
        SQLExpr argName = arguments.get(0);
        JSONArray maps = new JSONArray();
        for (Object objectMap : parentInvokeResult.getResult()) {
            JSONObject object = (JSONObject) objectMap;
            JSONObject item = new JSONObject();
            item.put(fieldName, object.get(argName));
            maps.add(item);
        }
        return maps;
    }),
    GROUP_CONCAT("group_concat", 1, String.class, true, (lastAllPlanResult, parentInvokeResult, arguments, fieldName) -> {
        JSONArray parentResult = parentInvokeResult.getResult();

        JSONArray result = new JSONArray();

        StringBuilder sb = new StringBuilder();
        // 遍历每一行
        for (Object lineResult : parentResult) {
            JSONObject line = (JSONObject) lineResult;
            for (SQLExpr argument : arguments) {
                // todo 这里需要处理argument 如果argument有前缀之类的(a.name)就需要单独处理

                Object o = line.get(argument.toString());
                sb.append(o);
            }
        }
        JSONObject e = new JSONObject();
        e.put(fieldName, sb.toString());
        result.add(e);
        return result;
    }),

    ;

    /**
     * 方法名称
     */
    private final String name;

    /**
     * 方法入参个数 如果是可变参数,则值为-1
     */
    private final Integer paramCount;

    /**
     * 结果类型
     */
    private final Class<?> resultType;

    /**
     * 解析结果的过程
     */
    private final MakeResultFunction function;

    /**
     * 会合并多行
     */
    private final Boolean mergeable;

    SqlMethodEnum(String name, Integer paramCount, Class<?> resultType, Boolean mergeable, MakeResultFunction function) {
        this.name = name;
        this.paramCount = paramCount;
        this.resultType = resultType;
        this.mergeable = mergeable;
        this.function = function;
    }

    /**
     * 解析mysql方法
     *
     * @param name       方法名称
     * @param paramCount 方法入参个数
     *
     * @return
     */
    @NotNull
    public static SqlMethodEnum parse(String name, Integer paramCount) {
        for (SqlMethodEnum value : values()) {
            if (StringUtil.equalsIgnoreCase(name, value.name) && (value.paramCount == -1 || Objects.equals(paramCount, value.paramCount))) {
                return value;
            }
        }
        Asserts.throwException("未知的sql方法名称,name:{},参数个数:{}", name, paramCount.toString());
        return null;
    }

    /**
     * concat添加字符串方法
     *
     * @param resultStrList 结果字符串
     * @param lineIndex     行序号
     * @param lineResult    此行解析的要被添加的结果
     */
    private static void concatAppendString(List<StringBuilder> resultStrList, int lineIndex, String lineResult) {
        if (resultStrList.size() <= lineIndex) {
            resultStrList.add(new StringBuilder());
        }
        StringBuilder stringBuilder = resultStrList.get(lineIndex);
        stringBuilder.append(lineResult);
    }

    public Boolean getMergeable() {
        return mergeable;
    }

    /**
     * 解析为结果字段
     *
     * @return
     */
    @NotNull
    public FieldInfo makeFieldInfo(String dbName, String tableName, String tableRealName, Integer index, String fieldName) {
        return FieldTypeEnum.makeFieldInfo(dbName, tableName, tableRealName, resultType, index, fieldName);
    }

    /**
     * 解析为结果
     *
     * @param parentInvokeResult 上一个执行计划的结果
     * @param arguments          入参
     * @param fieldName
     *
     * @return
     */
    public JSONArray makeResult(Map<Long, NodeInvokeResult> lastAllPlanResult, NodeInvokeResult parentInvokeResult, List<SQLExpr> arguments, String fieldName) {
        return function.makeResult(lastAllPlanResult, parentInvokeResult, arguments, fieldName);
    }


    @FunctionalInterface
    interface MakeResultFunction {

        /**
         * 解析结果
         *
         * @param parentInvokeResult 上一个执行计划的结果
         * @param arguments          入参
         *
         * @return
         */
        JSONArray makeResult(Map<Long, NodeInvokeResult> lastAllPlanResult, NodeInvokeResult parentInvokeResult, List<SQLExpr> arguments, String fieldName);
    }
}
