package top.uhyils.usher.handler.impl;

import top.uhyils.usher.handler.NodeHandler;
import top.uhyils.usher.node.NodeFactory;
import top.uhyils.usher.node.call.CallNode;
import top.uhyils.usher.pojo.SqlInvokeCommand;
import top.uhyils.usher.pojo.TableInfo;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月10日 08时53分
 */
public abstract class AbstractNodeHandler implements NodeHandler {

    @Override
    public CallNode makeNode(SqlInvokeCommand build) {
        TableInfo tableInfo = findInfo(build);
        return NodeFactory.makeNode(build, tableInfo);
    }

    @Override
    public TableInfo findInfo(SqlInvokeCommand build) {
        String database = build.getDatabase();
        String table = build.getTable();
        return findByDatabaseAndTable(database, table);
    }

    /**
     * 根据数据库和表名获取表信息
     *
     * @param database
     * @param table
     *
     * @return
     */
    protected abstract TableInfo findByDatabaseAndTable(String database, String table);

}
