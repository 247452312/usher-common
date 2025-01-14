package top.uhyils.usher.node;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import top.uhyils.usher.enums.DefaultSupportTypeEnum;
import top.uhyils.usher.node.call.CallNode;
import top.uhyils.usher.node.call.HttpLeafNode;
import top.uhyils.usher.pojo.SqlInvokeCommand;
import top.uhyils.usher.pojo.TableInfo;
import top.uhyils.usher.util.Asserts;
import top.uhyils.usher.util.LogUtil;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月10日 09时44分
 */
public class NodeFactory {

    private static Map<String, BiFunction<SqlInvokeCommand, TableInfo, CallNode>> NODE_BUILD_MAP = new HashMap<>();


    static {
        NODE_BUILD_MAP.put(DefaultSupportTypeEnum.HTTP.getType(), HttpLeafNode::new);
    }

    /**
     * 根据提前设置好的方式进行一个构的建
     *
     * @param invokeCommand
     * @param tableInfo
     *
     * @return
     */
    public static CallNode makeNode(SqlInvokeCommand invokeCommand, TableInfo tableInfo) {
        Asserts.assertTrue(NODE_BUILD_MAP.containsKey(tableInfo.getType()), "不支持的节点类型:{}", tableInfo.getType());
        return NODE_BUILD_MAP.get(tableInfo.getType()).apply(invokeCommand, tableInfo);
    }

    /**
     * 根据提前设置好的方式进行一个构的建
     *
     * @param tableInfo
     *
     * @return
     */
    public static CallNode makeNode(TableInfo tableInfo) {
        return makeNode(null, tableInfo);
    }

    /**
     * 添加支持的构建方式,如果已经有,则覆盖之前的构建方式
     *
     * @param type  支持根类型
     * @param build 构建方式
     */
    public static void addSupportType(String type, BiFunction<SqlInvokeCommand, TableInfo, CallNode> build) {
        if (NODE_BUILD_MAP.containsKey(type)) {
            LogUtil.warn("类型:{} 节点已支持,正在覆盖节点构建方式!");
        }
        NODE_BUILD_MAP.put(type, build);
    }

}
