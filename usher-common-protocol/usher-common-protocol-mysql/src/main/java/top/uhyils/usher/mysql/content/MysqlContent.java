package top.uhyils.usher.mysql.content;

import io.netty.channel.ChannelId;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
import top.uhyils.usher.UsherThreadLocal;
import top.uhyils.usher.mysql.pojo.entity.MysqlTcpLink;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年03月22日 19时25分
 */
public class MysqlContent {


    /**
     * 当前连接的mysql信息
     */
    public static final UsherThreadLocal<MysqlTcpLink> MYSQL_TCP_INFO = new UsherThreadLocal<>();


    /**
     * 全局预处理语句id
     */
    private static final AtomicLong PREPARE_ID = new AtomicLong(0);

    /**
     * mysql的tcp缓存
     */
    private static final WeakHashMap<ChannelId, MysqlTcpLink> mysqlTcpInfoWeakHashMap = new WeakHashMap<>();

    public static long getAndIncrementPrepareId() {
        return PREPARE_ID.getAndIncrement();
    }

    /**
     * 提交新的tcp连接信息
     *
     * @param channelId
     *
     * @return
     */
    public static MysqlTcpLink putMysqlTcpInfo(ChannelId channelId) {
        return mysqlTcpInfoWeakHashMap.put(channelId, MYSQL_TCP_INFO.get());
    }

    /**
     * 查询新的tcp连接信息
     *
     * @param channelId
     *
     * @return
     */
    public static MysqlTcpLink findMysqlTcpInfo(ChannelId channelId) {
        return mysqlTcpInfoWeakHashMap.put(channelId, MYSQL_TCP_INFO.get());
    }

}
