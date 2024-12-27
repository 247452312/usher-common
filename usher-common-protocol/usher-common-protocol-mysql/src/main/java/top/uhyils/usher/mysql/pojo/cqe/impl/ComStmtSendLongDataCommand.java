package top.uhyils.usher.mysql.pojo.cqe.impl;

import java.util.List;
import top.uhyils.usher.mysql.enums.MysqlCommandTypeEnum;
import top.uhyils.usher.mysql.pojo.cqe.AbstractMysqlCommand;
import top.uhyils.usher.mysql.pojo.response.MysqlResponse;


/**
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年11月03日 18时47分
 */
public class ComStmtSendLongDataCommand extends AbstractMysqlCommand {

    public ComStmtSendLongDataCommand(byte[] mysqlBytes) {
        super(mysqlBytes);
    }

    @Override
    public List<MysqlResponse> invoke() {
        return null;
    }

    @Override
    public MysqlCommandTypeEnum type() {
        return MysqlCommandTypeEnum.COM_STMT_SEND_LONG_DATA;
    }

    @Override
    protected void load() {

    }

}
