package top.uhyils.usher.mysql.pojo.sys;

import java.util.Map;
import top.uhyils.usher.mysql.enums.SysTableEnum;
import top.uhyils.usher.mysql.handler.MysqlServiceHandler;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.util.Asserts;
import top.uhyils.usher.util.StringUtil;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年09月01日 11时02分
 */
public class SysProviderInterface {

    /**
     * 系统数据库名称
     */
    private final String database;

    /**
     * 表
     */
    private final String table;

    /**
     * mysql服务处理
     */
    private final MysqlServiceHandler mysqlServiceHandler;

    public SysProviderInterface(String database, String table, MysqlServiceHandler mysqlServiceHandler) {
        this.database = database;
        this.table = table;
        this.mysqlServiceHandler = mysqlServiceHandler;
    }

    public NodeInvokeResult getResult(Map<String, String> header, Map<String, Object> params) {
        Asserts.assertTrue(StringUtil.isNotEmpty(database) && StringUtil.isNotEmpty(table), "数据库不存在或表不存在");
        SysTableEnum parse = SysTableEnum.parse(database, table);
        SysTable sysTable = parse.getSysTable(mysqlServiceHandler, params);
        return sysTable.getResult();
    }
}
