package top.uhyils.usher.node.call;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import top.uhyils.usher.content.CallNodeContent;
import top.uhyils.usher.pojo.FieldInfo;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.pojo.SqlInvokeCommand;
import top.uhyils.usher.pojo.TableInfo;
import top.uhyils.usher.util.FieldUtil;
import top.uhyils.usher.util.StringUtil;

/**
 * 叶子节点,承担着对实际业务发布的正常业务进行调用的功能,可以分别对不同种类的服务进行调用, 包括但不限于 http,mysql,mq,rpc
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月08日 16时34分
 */
public abstract class AbstractLeafNode extends AbstractNode {


    public AbstractLeafNode(SqlInvokeCommand mysqlInvokeCommand, TableInfo tableInfo) {
        super(mysqlInvokeCommand, tableInfo);
    }

    @NotNull
    protected NodeInvokeResult dealStrResult(String send) {
        JSONArray result;
        if (StringUtil.isJson(send)) {
            if (StringUtil.isJsonObject(send)) {
                result = new JSONArray();
                result.add(JSONObject.parseObject(send));
            } else {
                result = JSONObject.parseArray(send);
            }
        } else {
            result = new JSONArray();
            JSONObject resultItem = new JSONObject();
            resultItem.put(CallNodeContent.DEFAULT_RESULT_NAME, send);
            result.add(resultItem);
        }
        List<FieldInfo> fieldInfos = FieldUtil.makeFieldInfo(tableInfo.getDatabaseName(), tableInfo.getTableName(), tableInfo.getTableName(), 0, result);

        NodeInvokeResult nodeInvokeResult = new NodeInvokeResult(null);
        nodeInvokeResult.setFieldInfos(fieldInfos);
        nodeInvokeResult.setResult(result);
        return nodeInvokeResult;
    }
}
