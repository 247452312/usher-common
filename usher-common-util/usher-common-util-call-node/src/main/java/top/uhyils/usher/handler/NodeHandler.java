package top.uhyils.usher.handler;

import top.uhyils.usher.node.call.CallNode;
import top.uhyils.usher.pojo.SqlInvokeCommand;
import top.uhyils.usher.pojo.TableInfo;

/**
 * mysql这一层需要的service
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年08月16日 17时19分
 */
public interface NodeHandler {


    /**
     * 根据命令创建节点
     *
     * @param build
     *
     * @return
     */
    CallNode makeNode(SqlInvokeCommand build);


    /**
     * 获取节点信息
     *
     * @param build
     *
     * @return
     */
    TableInfo findInfo(SqlInvokeCommand build);
}
