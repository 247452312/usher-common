package top.uhyils.usher.log.filter.controller;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import top.uhyils.usher.context.MyTraceIdContext;
import top.uhyils.usher.enums.LogTypeEnum;
import top.uhyils.usher.util.IpUtil;
import top.uhyils.usher.util.LogUtil;


/**
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年07月27日 22时58分
 */
public class ControllerLogFilter implements Filter {

    /**
     * 静态资源标记
     */
    private static final char STATIC_RESOURCE_MARK = '.';

    /**
     * 接口标记
     */
    private static final char CONTROLLER_MARK = '/';

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        String requestUri = req.getRequestURI();
        boolean isController = true;
        for (int i = requestUri.length() - 1; i >= 0; i--) {
            char c = requestUri.charAt(i);
            if (c == STATIC_RESOURCE_MARK) {
                isController = false;
                break;
            } else if (c == CONTROLLER_MARK) {
                break;
            }
        }
        if (!isController) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String ip = IpUtil.getServletIP(req);
        // 防止共享traceId
        MyTraceIdContext.clean();
        MyTraceIdContext.onlyOnePrintLogInfo(LogTypeEnum.CONTROLLER, () -> {
            try {
                ((HttpServletResponse) servletResponse).addHeader("trace", MyTraceIdContext.getRpcIdStr());
                filterChain.doFilter(servletRequest, servletResponse);

            } catch (IOException | ServletException e) {
                LogUtil.error(e);
            }
            return null;
        }, new String[]{requestUri, ip}, requestUri, ip);
    }


    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
