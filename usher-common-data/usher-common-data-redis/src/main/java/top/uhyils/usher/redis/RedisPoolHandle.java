package top.uhyils.usher.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import redis.clients.jedis.exceptions.JedisConnectionException;
import top.uhyils.usher.context.UsherContext;
import top.uhyils.usher.pojo.DTO.UserDTO;
import top.uhyils.usher.util.LogUtil;
import top.uhyils.usher.util.StringUtil;

/**
 * 懒加载,除了用到redis
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2020年05月04日 12时21分
 */
@Lazy
@Component
public class RedisPoolHandle {

    /**
     * impl
     */
    private static final String IMPL = "Impl";

    /**
     * 检查方法是否允许执行所需要执行的lua脚本
     */
    private static String checkMethodDisableLua = "if redis.call(\"HEXISTS\",KEYS[2]) then\n" +
                                                  "    return redis.call(\"HGET\",KEYS[2])\n" +
                                                  "elseif redis.call(\"HEXISTS\",KEYS[1]) then\n" +
                                                  "    local classType = redis.call(\"HGET\",KEYS[1])\n" +
                                                  "    if classType == 0 then\n" +
                                                  "\n" +
                                                  "    elseif classType == 1 then\n" +
                                                  "        if KEYS[3] == 1 then\n" +
                                                  "            return 1\n" +
                                                  "    elseif classType == 2 then\n" +
                                                  "        if KEYS[3] == 2 then\n" +
                                                  "            return 1\n" +
                                                  "    elseif classType == 3 then\n" +
                                                  "        return 1\n" +
                                                  "    end\n" +
                                                  "else\n" +
                                                  "    return 0\n" +
                                                  "end\n";

    @Autowired
    private RedisPool redisPool;

    /**
     * 添加user到缓存 一共添加两个, 一个是 (token,userEntity->json) 一个是(userId,token) 都设置60分钟的过期时间
     * 缓存中的角色是没有权限的.也就是AOP中注入的user是没有记录权限集的
     *
     * @param token token
     * @param user  user
     */
    public void addUser(String token, UserDTO user) {
        try (Redisable jedis = redisPool.getJedis()) {
            String value = JSONObject.toJSONString(user);
            jedis.set(token, value);
            //半个小时
            jedis.expire(token, 60 * UsherContext.LOGIN_TIME_OUT_MIN);
            jedis.set(user.getId().toString(), token);
            jedis.expire(user.getId().toString(), 60 * UsherContext.LOGIN_TIME_OUT_MIN);

        } catch (JedisConnectionException e) {
            LogUtil.error(this, e);
        }

    }

    /**
     * 前台有操作都会经过这个方法, 通过token获取user, 同时刷新redis中用户的存在时间
     *
     * @param token token
     *
     * @return user
     */
    public Optional<UserDTO> getUser(String token) {
        Redisable jedis = redisPool.getJedis();
        try {
            String userJson = jedis.get(token);
            if (StringUtils.isEmpty(userJson)) {
                return Optional.empty();
            }
            UserDTO userEntity = JSON.parseObject(userJson, UserDTO.class);
            jedis.expire(token, 60 * UsherContext.LOGIN_TIME_OUT_MIN);
            jedis.expire(userEntity.getId().toString(), 60 * UsherContext.LOGIN_TIME_OUT_MIN);
            return Optional.of(userEntity);
        } catch (JedisConnectionException e) {
            LogUtil.error(this, e);
        } finally {
            jedis.close();
        }
        return Optional.empty();
    }

    /**
     * 根据用户id获取用户
     *
     * @param userId 用户id
     *
     * @return user
     */
    public UserDTO getUser(Long userId) {
        Redisable jedis = redisPool.getJedis();
        try {
            String token = jedis.get(userId.toString());
            if (StringUtil.isNotEmpty(token)) {
                return getUser(token).orElse(null);
            }
        } finally {
            jedis.close();
        }
        return null;
    }

    /**
     * 是否存在token
     *
     * @param token
     *
     * @return
     */
    public Boolean haveToken(String token) {
        Redisable jedis = redisPool.getJedis();
        try {
            Boolean exists = jedis.exists(token);
            return exists;
        } catch (JedisConnectionException e) {
            LogUtil.error(this, e);
        } finally {
            jedis.close();
        }
        return null;

    }

    public Boolean haveUserId(Long userId) {
        Redisable jedis = redisPool.getJedis();
        try {
            Boolean exists = jedis.exists(userId.toString());
            return exists;
        } catch (JedisConnectionException e) {
            LogUtil.error(this, e);
        } finally {
            jedis.close();
        }
        return null;

    }

    public boolean removeUserById(Long userId) {
        Redisable jedis = redisPool.getJedis();
        try {
            String key = userId.toString();
            String token = jedis.get(key);
            Long del = jedis.del(key, token);
            return del != 0;
        } catch (JedisConnectionException e) {
            LogUtil.error(this, e);
        } finally {
            jedis.close();
        }
        return Boolean.TRUE;
    }

    public boolean removeUserBytoken(String token) {
        Redisable jedis = redisPool.getJedis();
        try {
            String userJson = jedis.get(token);
            if (StringUtils.isEmpty(userJson)) {
                return Boolean.TRUE;
            }
            UserDTO userEntity = JSON.parseObject(userJson, UserDTO.class);
            Long id = userEntity.getId();
            Long del = jedis.del(id.toString(), token);
            return del != 0;
        } catch (JedisConnectionException e) {
            LogUtil.error(this, e);
        } finally {
            jedis.close();
        }
        return Boolean.TRUE;
    }

    public RedisPool getRedisPool() {
        return redisPool;
    }

    public void setRedisPool(RedisPool redisPool) {
        this.redisPool = redisPool;
    }

    public Redisable getJedis() {
        return redisPool.getJedis();
    }

    /**
     * 分布式锁,key为lock + lockName,value为 "持有此锁的线程的名称 : 此微服务的名称"
     *
     * @param lockName
     */
    public RedisLock getLock(String lockName) {
        return new RedisLock("lock_" + lockName, redisPool);

    }

    public Long exists(String... keys) {
        Redisable jedis = redisPool.getJedis();
        try {
            return jedis.exists(keys);
        } catch (JedisConnectionException e) {
            LogUtil.error(this, e);
            return -1L;
        } finally {
            jedis.close();
        }
    }

    public Long sadd(String key, List<String> values) {
        if (values == null || values.size() == 0) {
            return 0L;
        }
        Redisable jedis = redisPool.getJedis();
        try {
            return jedis.sadd(key, values.toArray(new String[]{}));
        } catch (JedisConnectionException e) {
            LogUtil.error(this, e);
            return -1L;
        } finally {
            jedis.close();
        }
    }

    public Boolean sismember(String key, String ip) {

        Redisable jedis = redisPool.getJedis();
        try {
            return jedis.sismember(key, ip);
        } catch (JedisConnectionException e) {
            LogUtil.error(this, e);
            return Boolean.FALSE;
        } finally {
            jedis.close();
        }
    }

    public Boolean hexists(String key, String ip) {
        Redisable jedis = redisPool.getJedis();
        try {
            return jedis.hexists(key, ip);
        } catch (JedisConnectionException e) {
            LogUtil.error(this, e);
            return Boolean.FALSE;
        } finally {
            jedis.close();
        }
    }

    public String hget(String key, String ip) {
        Redisable jedis = redisPool.getJedis();
        try {
            return jedis.hget(key, ip);
        } catch (JedisConnectionException e) {
            LogUtil.error(this, e);
            return "0";
        } finally {
            jedis.close();
        }
    }

    public Long hdel(String key, String ip) {
        Redisable jedis = redisPool.getJedis();
        try {
            return jedis.del(key, ip);
        } catch (JedisConnectionException e) {
            LogUtil.error(this, e);
            return -1L;
        } finally {
            jedis.close();
        }
    }

    public Long hincrby(String key, String ip) {
        Redisable jedis = redisPool.getJedis();
        try {
            return jedis.hincrby(key, ip);
        } catch (JedisConnectionException e) {
            LogUtil.error(this, e);
            return -1L;
        } finally {
            jedis.close();
        }
    }

    /**
     * 检查方法是否允许执行
     *
     * @param targetClass    目标class
     * @param declaredMethod 目标方法
     * @param readWriteType  方法的类型 1->读接口 2->写接口
     *
     * @return 是否允许执行
     */
    public Boolean checkMethodDisable(Class<?> targetClass, Method declaredMethod, Integer readWriteType) {
        String className = targetClass.getName();
        if (className.contains(IMPL)) {
            Class<?> anInterface = targetClass.getInterfaces()[0];
            className = anInterface.getName();
        }
        String methodName = className + "#" + declaredMethod.getName();
        return checkMethodDisable(className, methodName, readWriteType);
    }

    /**
     * 检查方法是否允许执行
     *
     * @param className      目标class
     * @param methodFullName 目标className+#+methodFullName
     * @param readWriteType  方法的类型 1->读接口 2->写接口
     *
     * @return 是否允许执行
     */
    public Boolean checkMethodDisable(String className, String methodFullName, Integer readWriteType) {

        try (Redisable jedis = redisPool.getJedis()) {
            Boolean exists = jedis.exists(UsherContext.SERVICE_USEABLE_SWITCH);
            // 如果不存在这个hash串,则全部放行
            if (!exists) {
                return Boolean.TRUE;
            }
            String methodPower = jedis.hget(UsherContext.SERVICE_USEABLE_SWITCH, methodFullName);
            if (methodPower != null) {
                //如果是0返回不禁用.如果不是0返回禁用
                return 0 == Integer.parseInt(methodPower);
            } else {
                String classPower = jedis.hget(UsherContext.SERVICE_USEABLE_SWITCH, className);
                if (classPower != null) {
                    int classPowerInt = Integer.parseInt(classPower);
                    if (classPowerInt == 3) {
                        return Boolean.FALSE;
                    } else if (classPowerInt == 0) {
                        return Boolean.TRUE;

                    } else {
                        if (classPowerInt == readWriteType) {
                            return Boolean.FALSE;
                        }
                        return Boolean.TRUE;
                    }
                }
                return Boolean.TRUE;
            }
        }
    }


    public Boolean removeByKey(String key) {

        try (Redisable jedis = redisPool.getJedis()) {
            if (jedis.exists(key)) {
                jedis.del(key);
            }
        }
        return Boolean.TRUE;
    }
}
