package top.uhyils.usher.node.call;

import top.uhyils.usher.content.CallNodeContent;
import top.uhyils.usher.node.DatabaseInfo;
import top.uhyils.usher.pojo.SqlInvokeCommand;
import top.uhyils.usher.pojo.TableInfo;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月10日 13时48分
 */
public abstract class AbstractNode implements CallNode {

    protected TableInfo tableInfo;

    protected SqlInvokeCommand mysqlInvokeCommand;

    public AbstractNode(SqlInvokeCommand mysqlInvokeCommand, TableInfo tableInfo) {
        this.tableInfo = tableInfo;
        this.mysqlInvokeCommand = mysqlInvokeCommand;
    }

    /**
     * 获取当前节点的数据库名
     */
    public String datebase() {
        return tableInfo.getDatabaseName();
    }

    /**
     * 获取当前节点的表名
     */
    public String tableName() {
        return tableInfo.getTableName();
    }

    /**
     * 转换为数据库格式
     *
     * @return
     */
    @Override
    public DatabaseInfo changeToDatabaseInfo() {
        DatabaseInfo databaseInfo = new DatabaseInfo();
        databaseInfo.setCatalogName(CallNodeContent.CATALOG_NAME);
        databaseInfo.setSchemaName(datebase());
        databaseInfo.setDefaultCharacterSetName(CallNodeContent.DEFAULT_CHARACTER_SET_NAME);
        databaseInfo.setDefaultCollationName(CallNodeContent.DEFAULT_COLLATION_NAME);
        databaseInfo.setSqlPath(null);
        databaseInfo.setDefaultEncryption("NO");
        return databaseInfo;
    }

    @Override
    public TableInfo changeToTableInfo() {
        return tableInfo;
    }
}
