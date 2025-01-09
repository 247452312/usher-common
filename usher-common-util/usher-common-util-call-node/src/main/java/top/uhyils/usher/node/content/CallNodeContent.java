package top.uhyils.usher.node.content;

import java.util.ArrayList;
import java.util.List;
import top.uhyils.usher.UsherThreadLocal;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月08日 16时51分
 */
public class CallNodeContent {


    /**
     * mysql版本
     */
    public static final String VERSION = "5.7.36";

    /**
     * 协议结尾
     */
    public static final String END_OF_PROTO = "mysql_native_password";

    /**
     * path中的分隔符
     */
    public static final String PATH_SEPARATOR = "/";

    /**
     * 默认库名称
     */
    public static final String CATALOG_NAME = "def";

    public static final String MYSQL_YES = "Y";

    public static final String MYSQL_NO = "N";

    /**
     * 当前发起请求的用户的用户信息
     */
    public static final UsherThreadLocal<CallerUserInfo> CALLER_INFO = new UsherThreadLocal<>();


    /**
     * 默认字符集
     */
    public static final String DEFAULT_CHARACTER_SET_NAME = "utf8mb4";

    /**
     * 数据库排序规则
     */
    public static final String DEFAULT_COLLATION_NAME = "utf8mb4_general_ci";

    /**
     * 系统默认的数据库,不允许创建,删除,修改
     */
    public static final List<String> SYS_DATABASE = new ArrayList<>();

    /**
     * 入参构造的表的表名默认值
     */
    public static final String DEFAULT_PARAM_TABLE = "param_table";

    /**
     * 方法结果的表的表名默认值
     */
    public static final String DEFAULT_METHOD_CALL_TABLE = "method_call_table";

    /**
     * dual表对应的db
     */
    public static final String DUAL_DATABASES = "mysql";

    /**
     * 通用中间字段名称
     */
    public static final String DEFAULT_RESULT_NAME = "result";

}
