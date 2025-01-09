package top.uhyils.usher.node.call;

import com.alibaba.fastjson.JSONObject;
import java.util.HashMap;
import java.util.Map;
import top.uhyils.usher.node.NodeInvokeResult;

/**
 * 执行节点
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月08日 16时26分
 */
public interface CallNode {


    /**
     * 节点执行
     *
     * @param header 请求头,按需使用
     * @param params 请求体
     */
    NodeInvokeResult call(Map<String, String> header, JSONObject params);

    /**
     * 节点执行
     *
     * @param params 请求体
     */
    default NodeInvokeResult call(JSONObject params) {
        return call(new HashMap<>(16), params);
    }
}
