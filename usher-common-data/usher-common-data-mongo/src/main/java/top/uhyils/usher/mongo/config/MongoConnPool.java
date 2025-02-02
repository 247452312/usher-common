package top.uhyils.usher.mongo.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * mongo连接池
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2020年07月01日 14时56分
 */
@Component
public class MongoConnPool {

    /**
     * 连接安全的集合
     */
    private volatile List<MongoConn> list = Collections.synchronizedList(new ArrayList<>());

    @Autowired
    private MongoDbFactory mongoDbFactory;

    /**
     * 核心连接数量
     */
    @Value("${mongo.pool.coreSize}")
    private Integer coreConnSize;

    /**
     * 最大连接数量
     */
    @Value("${mongo.pool.maxSize}")
    private Integer maxConnSize;

    /**
     * 核心之外的连接存活时间
     */
    @Value("${mongo.pool.keepAliveTime}")
    private Long keepAliveTime;

    public MongoConnPool() {
    }

    public List<MongoConn> getList() {
        return list;
    }

    public void setList(List<MongoConn> list) {
        this.list = list;
    }

    public Integer getCoreConnSize() {
        return coreConnSize;
    }

    public void setCoreConnSize(Integer coreConnSize) {
        this.coreConnSize = coreConnSize;
    }

    public Integer getMaxConnSize() {
        return maxConnSize;
    }

    public void setMaxConnSize(Integer maxConnSize) {
        this.maxConnSize = maxConnSize;
    }

    public Long getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(Long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public MongoConn getConn() throws Exception {
        for (MongoConn mongoConn : list) {
            // 如果没有人用 (类似双重检测锁)
            if (!mongoConn.getHaveUse()) {
                synchronized (mongoConn) {
                    if (!mongoConn.getHaveUse()) {
                        mongoConn.setHaveUse(Boolean.TRUE);
                        mongoConn.setCreateTime(System.currentTimeMillis());
                        return mongoConn;
                    }
                }
            }
        }
        if (list.size() >= maxConnSize) {
            throw new Exception("连接池已满,fuck?!!");
        }
        // 连接池没有满, 并且其他的都在用,就新建一个连接并返回
        synchronized (list) {
            MongoConn conn = mongoDbFactory.getConn(this);
            list.add(conn);
            conn.setHaveUse(Boolean.TRUE);
            return conn;
        }
    }

    public void returnConn(MongoConn conn) {
        conn.setHaveUse(false);
    }

    /**
     * 初始化
     */
    @PostConstruct
    private void init() {
        // 开启守护线程
        /**
         * 守护线程 用来销毁到期线程
         */
        Runnable guardianThread = new MongoGuardianThread(this);
        Thread thread = new Thread(guardianThread, "mongo线程池守护线程");
        thread.setDaemon(Boolean.TRUE);
        thread.start();

        /*底下的是线程池来运行的代码.感觉不需要呀?*/
        //        // 开启守护线程
        //        /**
        //         * 守护线程 用来销毁到期线程
        //         */
        //        Runnable guardianThread = new MongoGuardianThread(this);
        //        ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), r -> {
        //            Thread thread1 = new Thread(guardianThread);
        //            thread1.setDaemon(true);
        //            thread1.setName("mongo线程池守护线程");
        //            return thread1;
        //        });
        //        executorService.execute(guardianThread);
    }
}
