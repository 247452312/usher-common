package top.uhyils.usher.util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import top.uhyils.usher.context.UsherContext;

/**
 * 本项目id生产规则
 * <p>
 * long类型共计8字节64位,最高位恒为0代表正数,第1位到第43位存储时间戳,44-54位为10位序列数(防重),第44位到底48位(5位)代表分布式节点index,59-64位预留,等待其他业务需要
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2020年12月06日 19时27分
 */
public class IdUtil {

    /**
     * 分布式节点index_key
     */
    public static final String SYSTEM_CODE_KEY = "ENTITY_ID_CODE";

    public static final Random RANDOM = new Random();

    private static volatile IdUtil instance;

    /**
     * 序列号
     */
    private final AtomicLong sequence = new AtomicLong(0L);

    /**
     * 区别不同应用或者同一应用的不同节点的标识
     */
    private final Long code;

    /**
     * 存储上一次生成的时间,保证系统时间不正确时不产生错误的id
     */
    private volatile Long lastTime = 0L;


    private IdUtil(Long code) {
        this.code = code;
    }

    public static IdUtil getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (IdUtil.class) {
            if (instance != null) {
                return instance;
            }
            instance = new IdUtil(Long.valueOf(System.getProperty(SYSTEM_CODE_KEY, Long.toString(RANDOM.nextLong()))));
            return instance;
        }
    }

    public static long newId() {
        return getInstance().nextId();
    }

    public static long newId(Long code) {
        return getInstance().nextId(code);
    }

    public synchronized long nextId() {
        return nextId(this.code);
    }

    /**
     * 产生下一个分布式唯一编码
     *
     * @param code 分布式节点code
     *
     * @return
     */
    public synchronized long nextId(Long code) {
        if (code == null) {
            code = 1L;
        }
        // 生成时间
        long time = System.currentTimeMillis();
        Asserts.assertTrue(time >= lastTime, "系统时间不正确");
        if (lastTime != time) {
            // 如果不是之前的毫秒,则sequence归零,继续生成序列号
            sequence.set(0L);
            lastTime = time;
        }
        // 获取序列号
        long sq = sequence.getAndIncrement();

        // 如果序列号超出,则阻塞到下一个毫秒继续获取序列号
        if (sq > UsherContext.SEQUENCE_MASK) {
            try {
                Thread.sleep(1L);
            } catch (InterruptedException e) {
                LogUtil.error(e);
                Thread.currentThread().interrupt();
            }
            return newId();
        }
        // 从配置文件中获取 代表分布式唯一编码
        long distributedResult = (code & UsherContext.DISTRIBUTED_MASK) << UsherContext.DISTRIBUTED_DISPLACEMENT;

        //时间戳
        long timeResult = (time & UsherContext.TIME_MASK) << UsherContext.TIME_DISPLACEMENT;

        // 序列数
        long sqResult = (sq & UsherContext.SEQUENCE_MASK) << UsherContext.SEQUENCE_DISPLACEMENT;

        return timeResult | sqResult | distributedResult;
    }
}
