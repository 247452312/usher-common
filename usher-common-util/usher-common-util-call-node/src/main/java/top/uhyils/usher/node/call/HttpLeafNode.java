package top.uhyils.usher.node.call;

import cn.hutool.http.Method;
import com.alibaba.fastjson.JSONObject;
import java.util.Map;
import top.uhyils.usher.enums.QuerySqlTypeEnum;
import top.uhyils.usher.pojo.CallInfo;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.pojo.SqlInvokeCommand;
import top.uhyils.usher.pojo.TableInfo;
import top.uhyils.usher.util.Asserts;
import top.uhyils.usher.util.HttpUtil;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月10日 09时01分
 */
public class HttpLeafNode extends AbstractLeafNode {

    private static final String URL_KEY = "url";

    private static final String METHOD_KEY = "method";

    private String url;

    private Method method;

    public HttpLeafNode(SqlInvokeCommand mysqlInvokeCommand, TableInfo tableInfo) {
        super(mysqlInvokeCommand, tableInfo);
        init(mysqlInvokeCommand, tableInfo);
    }

    @Override
    public NodeInvokeResult call(Map<String, String> header, JSONObject params) {
        String send = HttpUtil.send(method, url, header, params);
        return dealStrResult(send);
    }


    /**
     * 初始化构建当前类,主要是获取url和method
     */
    private void init(SqlInvokeCommand mysqlInvokeCommand, TableInfo tableInfo) {
        CallInfo callInfo = tableInfo.getCallInfo();
        QuerySqlTypeEnum type = mysqlInvokeCommand.getType();
        Asserts.assertTrue(callInfo.getSupportSqlTypes().contains(type), "http根节点未设置指定调用方式:{}", type.getCode());
        JSONObject jsonObject = callInfo.getParams().get(type);
        this.url = jsonObject.getString(URL_KEY);
        switch (jsonObject.getString(METHOD_KEY)) {
            case "GET":
                method = Method.GET;
                break;
            case "POST":
                method = Method.POST;
                break;
            case "DELETE":
                method = Method.DELETE;
                break;
            case "PUT":
                method = Method.PUT;
                break;
            case "PATCH":
                method = Method.PATCH;
                break;
            default:
                Asserts.throwException("不支持的方法");
        }
    }
}
