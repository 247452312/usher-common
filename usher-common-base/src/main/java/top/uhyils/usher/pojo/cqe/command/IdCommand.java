package top.uhyils.usher.pojo.cqe.command;

import top.uhyils.usher.pojo.cqe.command.base.AbstractCommand;

/**
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年08月26日 09时01分
 */
public class IdCommand extends AbstractCommand {

    private Long id;

    /**
     * 快捷创建
     */
    public static IdCommand build(Long id) {
        IdCommand build = new IdCommand();
        build.id = id;
        return build;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
