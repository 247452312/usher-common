package top.uhyils.usher.node.handler;

import top.uhyils.usher.node.MysqlInvokeCommand;
import top.uhyils.usher.node.call.CallNode;

/**
 * mysql这一层需要的service
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年08月16日 17时19分
 */
public interface MysqlServiceHandler {


    /**
     * 根据命令创建节点
     *
     * @param build
     *
     * @return
     */
    CallNode makeNode(MysqlInvokeCommand build);
}
