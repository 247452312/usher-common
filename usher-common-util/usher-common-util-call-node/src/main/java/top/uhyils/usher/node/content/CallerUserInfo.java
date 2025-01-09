package top.uhyils.usher.node.content;

import java.io.Serializable;
import top.uhyils.usher.pojo.DTO.UserDTO;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月08日 16时51分
 */
public class CallerUserInfo implements Serializable {

    private String databaseName;

    private UserDTO userDTO;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public UserDTO getUserDTO() {
        return userDTO;
    }

    public void setUserDTO(UserDTO userDTO) {
        this.userDTO = userDTO;
    }
}
