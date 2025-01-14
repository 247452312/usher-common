package top.uhyils.usher.node.call;

import cn.hutool.http.Method;
import com.alibaba.fastjson.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import top.uhyils.usher.enums.QuerySqlTypeEnum;
import top.uhyils.usher.pojo.CallInfo;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.pojo.SqlInvokeCommand;
import top.uhyils.usher.pojo.TableInfo;
import top.uhyils.usher.util.Asserts;
import top.uhyils.usher.util.HttpUtil;
import top.uhyils.usher.util.StringUtil;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月10日 09时01分
 */
public class HttpLeafNode extends AbstractLeafNode {

    private static final String URL_KEY = "url";

    private static final String METHOD_KEY = "method";

    private static final String PARAMS_KEY = "params";

    private static final String HEADER_KEY = "headers";

    private Map<String, Object> defaultParams;

    private Map<String, String> defaultHeaders;

    private String url;

    private Method method;

    public HttpLeafNode(SqlInvokeCommand mysqlInvokeCommand, TableInfo tableInfo) {
        super(mysqlInvokeCommand, tableInfo);
        init(mysqlInvokeCommand, tableInfo);
    }

    @Override
    public NodeInvokeResult call(Map<String, String> header, JSONObject params) {

        // header来源有三处 1. 默认 2. 请求的header 3. 传入的header
        Map<String, String> sendHeaders = new HashMap<>(defaultHeaders);
        sendHeaders.putAll(header);
        String paramHeader = params.getString(HEADER_KEY);
        if (StringUtil.isNotEmpty(paramHeader)) {
            JSONObject headerJSONObject = JSONObject.parseObject(paramHeader);
            for (Entry<String, Object> entry : headerJSONObject.entrySet()) {
                sendHeaders.put(entry.getKey(), entry.getValue().toString());
            }
        }

        // params来源有两处 1.默认 2.传入的params
        Map<String, Object> sendParams = new HashMap<>(defaultParams);
        String string = params.getString(PARAMS_KEY);
        if (StringUtil.isNotEmpty(string)) {
            if (!StringUtil.isJson(string)) {
                String send = HttpUtil.send(method, url, header, string);
                return dealStrResult(send);
            }
            sendParams.putAll(JSONObject.parseObject(string));
        }
        String send = HttpUtil.send(method, url, sendHeaders, sendParams);
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
        this.defaultParams = (Map<String, Object>) jsonObject.getOrDefault(PARAMS_KEY, new HashMap<>());
        this.defaultHeaders = (Map<String, String>) jsonObject.getOrDefault(HEADER_KEY, new HashMap<>());
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
