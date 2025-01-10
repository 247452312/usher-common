package top.uhyils.usher.handler.impl;

import top.uhyils.usher.handler.NodeHandler;
import top.uhyils.usher.node.LeafNodeFactory;
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
        String database = build.getDatabase();
        String table = build.getTable();
        TableInfo tableInfo = findByDatabaseAndTable(database, table);
        return LeafNodeFactory.makeLeafNode(build, tableInfo);
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
