package top.uhyils.usher.pojo.cqe.command;

import java.util.List;
import top.uhyils.usher.pojo.cqe.command.base.AbstractCommand;


/**
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年08月27日 09时04分
 */
public class IdsCommand extends AbstractCommand {

    private List<Long> ids;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}
