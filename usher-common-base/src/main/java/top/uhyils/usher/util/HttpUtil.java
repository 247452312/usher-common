package top.uhyils.usher.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.Method;
import com.alibaba.fastjson2.JSON;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

/**
 * http请求类
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2020年07月26日 10时56分
 */
public final class HttpUtil {


    private HttpUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 发送GET请求
     *
     * @param url  请求url
     * @param head 请求参数
     *
     * @return JSON或者字符串
     */
    public static String sendHttpGet(String url, Map<String, String> head) {
        return send(Method.GET, url, head, null);
    }

    /**
     * 发送POST请求
     *
     * @param url    url
     * @param heads  请求头
     * @param params 参数
     *
     * @return JSON或者字符串
     */
    public static String sendHttpPost(String url, Map<String, String> heads, Map<String, ? extends Object> params) {
        return send(Method.POST, url, heads, params);
    }

    /**
     * 发送请求
     *
     * @param url    url
     * @param heads  请求头
     * @param params 参数
     *
     * @return JSON或者字符串
     */
    public static String send(Method method, String url, Map<String, String> heads, Map<String, ? extends Object> params) {
        HttpRequest request = cn.hutool.http.HttpUtil.createRequest(method, url);
        heads.forEach(request::header);
        if (params != null) {
            request = request.body(JSON.toJSONString(params));
        }
        return request.execute().body();
    }

    public static boolean ping(String ip) throws IOException {
        InetAddress inet = InetAddress.getByName(ip);
        return inet.isReachable(5000);
    }


}
