package top.uhyils.usher.mysql.pojo.cqe;

import top.uhyils.usher.content.CallNodeContent;
import top.uhyils.usher.content.CallerUserInfo;
import top.uhyils.usher.context.LoginInfoHelper;
import top.uhyils.usher.pojo.DTO.UserDTO;
import top.uhyils.usher.pojo.cqe.command.base.AbstractCommand;

/**
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年11月03日 15时03分
 */
public abstract class AbstractMysqlCommand extends AbstractCommand implements MysqlCommand {

    /**
     * 要处理的字节数组
     */
    protected byte[] mysqlBytes;

    protected AbstractMysqlCommand(byte[] mysqlBytes) {
        CallerUserInfo callerUserInfo = CallNodeContent.CALLER_INFO.get();
        UserDTO userInfo = callerUserInfo.getUserDTO();
        if (userInfo != null) {
            String token = userInfo.findToken();
            LoginInfoHelper.setToken(token);
            LoginInfoHelper.setUser(userInfo);
        }
        this.mysqlBytes = mysqlBytes;
        load();
    }

    /**
     * 加载
     */
    protected abstract void load();
}
