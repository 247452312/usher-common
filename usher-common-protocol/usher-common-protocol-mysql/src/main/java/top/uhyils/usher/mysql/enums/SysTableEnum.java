package top.uhyils.usher.mysql.enums;

import java.util.Map;
import java.util.function.BiFunction;
import top.uhyils.usher.annotation.NotNull;
import top.uhyils.usher.mysql.handler.MysqlServiceHandler;
import top.uhyils.usher.mysql.pojo.sys.ICollatinos;
import top.uhyils.usher.mysql.pojo.sys.IColumns;
import top.uhyils.usher.mysql.pojo.sys.IEngines;
import top.uhyils.usher.mysql.pojo.sys.IParameters;
import top.uhyils.usher.mysql.pojo.sys.IProfiling;
import top.uhyils.usher.mysql.pojo.sys.IRoutines;
import top.uhyils.usher.mysql.pojo.sys.ISchemaPrivileges;
import top.uhyils.usher.mysql.pojo.sys.ISchemata;
import top.uhyils.usher.mysql.pojo.sys.ITables;
import top.uhyils.usher.mysql.pojo.sys.IUserPrivileges;
import top.uhyils.usher.mysql.pojo.sys.IView;
import top.uhyils.usher.mysql.pojo.sys.MDual;
import top.uhyils.usher.mysql.pojo.sys.MUser;
import top.uhyils.usher.mysql.pojo.sys.PGlobalVariables;
import top.uhyils.usher.mysql.pojo.sys.SysTable;
import top.uhyils.usher.mysql.util.MysqlUtil;
import top.uhyils.usher.util.Asserts;
import top.uhyils.usher.util.StringUtil;

/**
 * 系统表枚举
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年09月01日 13时46分
 */
public enum SysTableEnum {
    /**
     * 库表元数据存储表
     */
    INFORMATION_SCHEMA_SCHEMATA("information_schema", "schemata", ISchemata::new),
    /**
     * 表元数据存储
     */
    INFORMATION_SCHEMA_TABLES("information_schema", "tables", ITables::new),
    /**
     * 列信息
     */
    INFORMATION_SCHEMA_COLUMNS("information_schema", "columns", IColumns::new),
    /**
     * 视图信息
     */
    INFORMATION_SCHEMA_PARAMETERS("information_schema", "parameters", IParameters::new),
    /**
     * 存储子程序（存储程序和函数）的信息
     */
    INFORMATION_SCHEMA_ROUTINES("information_schema", "routines", IRoutines::new),
    /**
     * 存储引擎相关的信息
     */
    INFORMATION_SCHEMA_ENGINES("information_schema", "engines", IEngines::new),
    /**
     * 视图相关的信息
     */
    INFORMATION_SCHEMA_VIEW("information_schema", "views", IView::new),
    /**
     * 可以用来分析每一条SQL在它执行的各个阶段的用时
     */
    INFORMATION_SCHEMA_PROFILING("information_schema", "profiling", IProfiling::new),
    /**
     * 排序规则
     */
    INFORMATION_SCHEMA_COLLATIONS("information_schema", "collations", ICollatinos::new),
    /**
     * 用户表另一个显示
     */
    INFORMATION_SCHEMA_USER_PRIVILEGES("information_schema", "user_privileges", IUserPrivileges::new),
    /**
     * 数据库表权限
     */
    INFORMATION_SCHEMA_SCHEMA_PRIVILEGES("information_schema", "schema_privileges", ISchemaPrivileges::new),
    /**
     * 系统参数
     */
    PERFORMANCE_SCHEMA_GLOBAL_VARIABLES("performance_schema", "global_variables", PGlobalVariables::new),
    /**
     * 用户参数表
     */
    MYSQL_USER("mysql", "user", MUser::new),
    /**
     * 系统参数
     */
    MYSQL_DUAL("mysql", "dual", MDual::new),
    ;

    /**
     * 库名称
     */
    private final String database;

    /**
     * 表名
     */
    private final String table;

    /**
     * 创建新SysTable的方法
     */
    private final BiFunction<MysqlServiceHandler, Map<String, Object>, SysTable> newSysTable;

    SysTableEnum(String database, String table, BiFunction<MysqlServiceHandler, Map<String, Object>, SysTable> newSysTable) {
        this.database = database;
        this.table = table;
        this.newSysTable = newSysTable;
    }

    /**
     * @param database
     * @param table
     *
     * @return
     */
    @NotNull
    public static SysTableEnum parse(String database, String table) {
        for (SysTableEnum value : values()) {
            if (StringUtil.equalsIgnoreCase(value.database, database)) {
                if (StringUtil.equalsIgnoreCase(value.table, table) || MysqlUtil.equalsIgnoreCaseAndQuotes(value.table, table)) {
                    return value;
                }
            }
        }
        throw Asserts.makeException("未定义此系统表:{},{}", database, table);
    }

    public String getDatabase() {
        return database;
    }

    public String getTable() {
        return table;
    }

    /**
     * 获取系统表查询类
     *
     * @param params 入参
     *
     * @return
     */
    @NotNull
    public SysTable getSysTable(MysqlServiceHandler handler, Map<String, Object> params) {
        return newSysTable.apply(handler, params);
    }
}
