package top.uhyils.usher.redis;

import top.uhyils.usher.rpc.config.RpcConfigFactory;
import top.uhyils.usher.util.LogUtil;

/**
 * redis实现的分布式锁
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2020年07月14日 17时26分
 */
public class RedisLock {

    /**
     * 分布式count_down_latch 名称
     */
    private static final String REDIS_COUNT_DOWN_LATCH = "count_down_latch_";

    /**
     * 默认尝试加锁次数
     */
    private static final Integer MAX_LOCK_COUNT = 3;

    /**
     * 锁名称 = lock_ + 传入的名称
     */
    private String lockName;

    /**
     * 锁的重入数量
     */
    private String lockCountName;

    /**
     * redis线程池
     */
    private RedisPool pool;

    /**
     * 加锁的线程
     */
    private Thread thread;

    /**
     * 这把锁持有时的value
     */
    private String value;

    /**
     * 默认持有锁3分钟
     */
    private Long lockTime = 3 * 60L;

    /**
     * @param lockName    分布式锁名称
     * @param pool        redis线程池
     * @param valueSuffix value后缀
     */
    public RedisLock(String lockName, RedisPool pool, String valueSuffix) {
        Thread nowThread = Thread.currentThread();
        this.lockName = lockName;
        this.pool = pool;
        this.thread = nowThread;

        this.value = this.thread.getName() + ":" + valueSuffix;
        this.lockCountName = lockName + "_count";
    }


    public RedisLock(String lockName, RedisPool pool, Long lockTime) {
        this(lockName, pool, RpcConfigFactory.getInstance().getApplication().getName());
        this.lockTime = lockTime;
    }

    public RedisLock(String lockName, RedisPool pool, Long lockTime, String valueSuffix) {
        this(lockName, pool, valueSuffix);
        this.lockTime = lockTime;
    }

    public RedisLock(String lockName, RedisPool pool) {
        this(lockName, pool, RpcConfigFactory.getInstance().getApplication().getName());
    }

    public RedisLock() {
    }

    public String getLockName() {
        return lockName;
    }

    public RedisPool getPool() {
        return pool;
    }

    public Thread getThread() {
        return thread;
    }

    /**
     * 加锁
     */
    public boolean lock(Long time) throws InterruptedException {
        int count = 0;
        while (count < MAX_LOCK_COUNT) {
            count++;
            boolean b = tryLock(time);
            if (b) {
                return true;
            }
            Thread.sleep(500L);
        }
        return false;
    }

    /**
     * 解锁
     */
    public void unlock() {
        try (Redisable jedis = pool.getJedis()) {
            Long lockCount = jedis.incrBy(lockCountName, -1L);
            if (lockCount == 0) {
                jedis.del(lockName);
                jedis.del(lockCountName);
            }
        } catch (Exception e) {
            LogUtil.error(this, e);
        }

    }

    /**
     * 暂时没有写
     */
    @Deprecated
    public void lockInterruptibly() {

    }

    public boolean tryLock() {
        return tryLock(lockTime);
    }

    public final boolean tryLock(Long time) {
        try (Redisable jedis = pool.getJedis()) {
            // 如果此线程已经获取到这个锁,那么不需要再次获取
            String s = jedis.get(lockName);
            if (s != null && s.equals(value)) {
                // (因为redis为原子操作 所以不需要再次检测)
                jedis.expire(lockName, time.intValue());
                // 此时该线程真正持有了这把锁
                // 重入数量+1
                jedis.incrBy(lockCountName, 1L);
                jedis.expire(lockCountName, time.intValue());
                return Boolean.TRUE;
            } else if (s == null) {
                // 尝试获取锁
                Long getLockSuccess = jedis.setnx(lockName, value);
                if (getLockSuccess == 1L) {
                    // (因为redis为原子操作 所以不需要再次检测)
                    jedis.expire(lockName, time.intValue());
                    // 此时该线程真正持有了这把锁
                    // 重入数量+1
                    jedis.incrBy(lockCountName, 1L);
                    jedis.expire(lockCountName, time.intValue());
                    return Boolean.TRUE;
                }
            }

        } catch (Exception e) {
            LogUtil.error(this, e);
        }
        return Boolean.FALSE;

    }

    /**
     * 使用分布式CountDownLatch
     *
     * @param uniqueName
     *
     * @return
     */
    public boolean countDown(String uniqueName) throws RedisCountDownLatchExistsException, InterruptedException {
        try (Redisable jedis = pool.getJedis()) {
            String key = REDIS_COUNT_DOWN_LATCH + uniqueName;
            if (!jedis.exists(key)) {
                throw new RedisCountDownLatchExistsException(key);
            }
            Long incrBy = jedis.incrBy(key, -1L);
            if (incrBy == 0L) {
                jedis.del(key);
                return true;
            }
            return loopToWaitCountDownLatch(jedis, uniqueName);
        }
    }

    /**
     * 获取指定countDownLatch的count
     *
     * @param uniqueName
     *
     * @return
     */
    public int getCountDownLatchCount(String uniqueName) {
        try (Redisable jedis = pool.getJedis()) {
            String count = jedis.get(uniqueName);
            return Integer.parseInt(count);
        }
    }

    /**
     * 方法介绍见{@link RedisLock#createCountDownLatch(java.lang.String, java.lang.Integer, java.lang.Integer)}
     *
     * @param uniqueName
     * @param count
     *
     * @return
     *
     * @throws RedisCountDownLatchExistsException
     */
    public boolean createCountDownLatch(String uniqueName, Integer count) throws RedisCountDownLatchExistsException {
        return createCountDownLatch(uniqueName, count, 3600);
    }

    /**
     * 创建一个countDownLatch
     *
     * @param uniqueName    这个countDownLatch的名称
     * @param count         要创建的数量
     * @param expireSeconds 超时时间(-1为不设置)
     *
     * @return
     *
     * @throws RedisCountDownLatchExistsException
     */
    public boolean createCountDownLatch(String uniqueName, Integer count, Integer expireSeconds) throws RedisCountDownLatchExistsException {
        String key = REDIS_COUNT_DOWN_LATCH + uniqueName;
        try (Redisable jedis = pool.getJedis()) {
            if (jedis.exists(key)) {
                throw new RedisCountDownLatchExistsException(key);
            }
            jedis.set(key, String.valueOf(count));
            if (expireSeconds != -1) {
                jedis.expire(key, expireSeconds);
            }
            return Boolean.TRUE;
        }
    }

    /**
     * 用循环的方式等待countDownLatch完成
     *
     * @param jedis
     * @param uniqueName
     *
     * @return
     *
     * @throws InterruptedException
     */
    private boolean loopToWaitCountDownLatch(Redisable jedis, String uniqueName) throws InterruptedException {
        String key = REDIS_COUNT_DOWN_LATCH + uniqueName;
        while (jedis.exists(key) && Integer.parseInt(jedis.get(key)) <= 0 && !Thread.interrupted()) {
            Thread.sleep(1000L);
        }
        return !Thread.interrupted();
    }
}
