package top.uhyils.usher.mq.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import org.apache.commons.lang3.StringUtils;
import top.uhyils.usher.log.content.LogContent;
import top.uhyils.usher.mq.util.LogInfoSendMqUtil;
import top.uhyils.usher.util.SpringUtil;


/**
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年07月31日 15时05分
 */
public class MyLogAppender extends RollingFileAppender {

    private static final String MSG_FORMAT = "%s|%s|%s";

    private static final String PIPE_SYMBOL = "|";


    private static final String DEBUG = "debug";

    @Override
    protected void subAppend(Object event) {
        super.subAppend(event);
        if (!this.isStarted()) {
            return;
        }
        // spring还没有启动时不进行发送
        if (SpringUtil.isNotStart()) {
            return;
        }
        LoggingEvent loggingEvent = (LoggingEvent) event;
        Level level = loggingEvent.getLevel();
        if (DEBUG.equalsIgnoreCase(level.toString())) {
            return;
        }
        String message = loggingEvent.getMessage();
        if (message == null || StringUtils.containsIgnoreCase(message, LogContent.TRACE_INFO) || StringUtils.containsIgnoreCase(Thread.currentThread().getName(), LogContent.TRACE_INFO)) {
            return;
        }
        if (message.contains(PIPE_SYMBOL)) {
            char sym = message.charAt(0);
            message = message.substring(1);
            LogInfoSendMqUtil.sendLogInfo(sym + String.format(MSG_FORMAT, loggingEvent.getLoggerName(), level, message));
        }
    }
}
