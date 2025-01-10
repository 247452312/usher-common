package top.uhyils.usher.mysql.pojo.DTO;

import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月10日 16时30分
 */
public class CompanyInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    private Long id;

    /**
     * 公司名称
     */
    private String name;

    /**
     * 负责人姓名
     */
    private String personName;

    /**
     * 负责人电话
     */
    private String personPhone;

    /**
     * AK
     */
    private String ak;

    /**
     * SK
     */
    private String sk;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("name", getName())
            .append("personName", getPersonName())
            .append("personPhone", getPersonPhone())
            .append("ak", getAk())
            .append("sk", getSk())
            .toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPersonPhone() {
        return personPhone;
    }

    public void setPersonPhone(String personPhone) {
        this.personPhone = personPhone;
    }

    public String getAk() {
        return ak;
    }

    public void setAk(String ak) {
        this.ak = ak;
    }

    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

}
