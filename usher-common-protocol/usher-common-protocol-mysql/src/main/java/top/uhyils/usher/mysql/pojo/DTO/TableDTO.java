package top.uhyils.usher.mysql.pojo.DTO;

import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 调用表信息
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2023年08月30日 17时17分
 */
public class TableDTO implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 厂商id
     */
    private Long companyId;

    /**
     * 转换节点id
     */
    private Long nodeId;

    /**
     * 数据库名称
     */
    private String database;

    /**
     * 表
     */
    private String table;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("companyId", getCompanyId())
            .append("nodeId", getNodeId())
            .append("database", getDatabase())
            .append("table", getTable())
            .toString();
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }
}
