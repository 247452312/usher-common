package top.uhyils.usher.pojo;

import java.io.Serializable;
import top.uhyils.usher.enums.DefaultSupportTypeEnum;
import top.uhyils.usher.node.NodeFactory;

/**
 * 根节点对应的实际执行的信息,对外表现为一个数据库表,所以类名为tableInfo
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月10日 09时28分
 */
public class TableInfo implements Serializable {

    /**
     * 节点的唯一id
     */
    private Long nodeId;

    /**
     * 数据库名称
     */
    private String databaseName;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 节点类型{@link DefaultSupportTypeEnum} 因为是字符串类型,所以支持扩展,如果需要扩展,则需要从{@link NodeFactory#addSupportType} 添加自定义的根节点类型以及对应构建方式
     */
    private String type;

    /**
     * 执行节点需要的相关信息
     */
    private CallInfo callInfo;

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public CallInfo getCallInfo() {
        return callInfo;
    }

    public void setCallInfo(CallInfo callInfo) {
        this.callInfo = callInfo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
