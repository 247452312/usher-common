package top.uhyils.usher.context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import top.uhyils.usher.UsherThreadLocal;
import top.uhyils.usher.enums.LogDetailTypeEnum;
import top.uhyils.usher.enums.LogTypeEnum;
import top.uhyils.usher.util.IdUtil;
import top.uhyils.usher.util.IpUtil;
import top.uhyils.usher.util.LogUtil;
import top.uhyils.usher.util.SpringUtil;
import top.uhyils.usher.util.SupplierWithException;

/**
 * traceId生成的地方
 *
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年07月22日 13时05分
 */
public class MyTraceIdContext {

    /**
     * rpc_trace 信息
     */
    public static final String RPC_HEADER_TRACE_INFO = "rpcTraceInfo";

    /**
     * 分隔符
     */
    public static final String PIPE_SYMBOL = "|";

    /**
     * hash起始符号
     */
    public static final String HASH_SYMBOL = "^";

    /**
     * 主线程名称
     */
    private static final String MAIN_THREAD_NAME = "main";

    /**
     * 保存traceId的地方
     */
    private static final UsherThreadLocal<Long> thraceId = new UsherThreadLocal<>();

    /**
     * 保存上一次调用链顺序的地方
     */
    private static final UsherThreadLocal<List<Integer>> rpcId = new UsherThreadLocal<>();

    /**
     * 这一次调用的RPCid
     */
    private static final UsherThreadLocal<AtomicInteger> thisRpcId = new UsherThreadLocal<>();

    /**
     * 项目名称
     */
    private volatile static String projectName;

    public static String getProjectName() {
        if (projectName == null) {
            projectName = SpringUtil.getProperty("rpc.application.name", "NoName");
        }
        return projectName;
    }

    public static void setProjectName(String projectName) {
        MyTraceIdContext.projectName = projectName;
    }

    /**
     * 获取日志详情
     *
     * @param logTypeEnum
     * @param startTime
     * @param timeConsuming
     * @param otherInfo
     *
     * @return
     */
    public static String getLogInfo(String rpcIdStr, LogTypeEnum logTypeEnum, long startTime, long timeConsuming, String... otherInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(LogDetailTypeEnum.LINK.getCode());
        sb.append(getThraceId());
        sb.append(PIPE_SYMBOL);
        sb.append(startTime);
        sb.append(PIPE_SYMBOL);
        sb.append(logTypeEnum.getCode());
        sb.append(PIPE_SYMBOL);
        sb.append(IpUtil.getIp());
        sb.append(PIPE_SYMBOL);
        sb.append(rpcIdStr);
        sb.append(PIPE_SYMBOL);
        String threadName = Thread.currentThread().getName();
        sb.append(threadName);
        sb.append(PIPE_SYMBOL);
        sb.append(getProjectName());
        sb.append(PIPE_SYMBOL);
        sb.append(timeConsuming);
        for (String info : otherInfo) {
            sb.append(PIPE_SYMBOL);
            sb.append(info);
        }
        return sb.toString();
    }

    /**
     * 打印链路跟踪日志
     *
     * @param logTypeEnum
     * @param startTime
     * @param timeConsuming
     * @param otherInfo
     *
     * @return MD5 唯一值
     */
    public static String printLogInfo(String rpcIdStr, LogTypeEnum logTypeEnum, long startTime, long timeConsuming, String... otherInfo) {
        String logInfo = getLogInfo(rpcIdStr, logTypeEnum, startTime, timeConsuming, otherInfo);
        LogUtil.link(logInfo);
        return logInfo;
    }

    /**
     * 获取ThraceId
     *
     * @return
     */
    public static Long getThraceId() {
        if (thraceId.get() == null) {
            if (checkMainThread()) {
                return 1L;
            }
            synchronized (MyTraceIdContext.class) {
                if (thraceId.get() == null) {
                    IdUtil bean = SpringUtil.getBean(IdUtil.class);
                    if (bean == null) {
                        bean = new IdUtil();
                    }
                    thraceId.set(bean.newId());
                }
            }
        }

        return thraceId.get();
    }

    /**
     * 设置ThraceId
     *
     * @param thraceId
     */
    public static void setThraceId(Long thraceId) {
        MyTraceIdContext.thraceId.set(thraceId);
    }

    /**
     * 获取rpcId
     *
     * @return
     */
    public static String getAndAddRpcIdStr() {
        List<Integer> lastRpcIds = getRpcId();
        int rpcId = getThisRpcId().getAndAdd(1);
        StringBuilder sb = mergeRpcId(lastRpcIds, rpcId);
        return sb.toString();
    }

    /**
     * 获取rpcId
     *
     * @return
     */
    public static String getRpcIdStr() {
        List<Integer> lastRpcIds = getRpcId();
        int rpcId = getThisRpcId().get();
        StringBuilder sb = mergeRpcId(lastRpcIds, rpcId);
        return sb.toString();
    }

    public static void clean() {
        thraceId.remove();
        rpcId.remove();
        thisRpcId.remove();
    }

    /**
     * 获取下一个rpc应该使用的rpcId
     *
     * @return
     */
    public static List<Integer> getNextRpcIds() {
        List<Integer> nextRpcIds = new ArrayList<>(getRpcId());
        AtomicInteger atomicInteger = getThisRpcId();
        nextRpcIds.add(atomicInteger.get());
        return nextRpcIds;
    }

    /**
     * 输出日志
     *
     * @param logType    日志类型
     * @param supplier   要执行的东西
     * @param other      其他加入主日志的东西
     * @param additional 详情日志
     * @param <T>        返回值
     *
     * @return
     */
    public static <T> T printLogInfo(LogTypeEnum logType, SupplierWithException<T> supplier, String[] other, String... additional) throws Throwable {
        String rpcIdStr = getAndAddRpcIdStr();
        long startTime = System.currentTimeMillis();
        try {
            return supplier.get();
        } finally {
            long useTime = System.currentTimeMillis() - startTime;
            String hash = null;
            if (additional != null && additional.length != 0) {
                String[] realOther = new String[other.length + 1];
                System.arraycopy(other, 0, realOther, 1, other.length);
                hash = hash(additional);
                realOther[0] = hash;
                other = realOther;
            }
            printLogInfo(rpcIdStr, logType, startTime, useTime, other);
            assert additional != null;
            switch (logType) {
                case DB:
                    LogUtil.sql(getThraceId(), hash, useTime, additional[0]);
                    break;
                case MQ:
                    LogUtil.mq(getThraceId(), hash, useTime, additional[0], additional[1]);
                    break;
                case RPC:
                    LogUtil.rpc(getThraceId(), hash, useTime, additional[0], additional[1]);
                    break;
                case TASK:
                    LogUtil.task(getThraceId(), hash, useTime, additional[0], additional[1]);
                    break;
                case CONTROLLER:
                    LogUtil.controller(getThraceId(), hash, useTime, additional[0], additional[1]);
                    break;
                default:
                    break;
            }

        }

    }

    /**
     * 入口.并且输出日志完成后清理掉
     *
     * @param logType    日志类型
     * @param supplier   要执行的东西
     * @param other      其他加入主日志的东西
     * @param additional 详情日志
     * @param <T>        返回值
     *
     * @return
     */
    public static <T> T onlyOnePrintLogInfo(LogTypeEnum logType, SupplierWithException<T> supplier, String[] other, String... additional) {
        try {
            return printLogInfo(logType, supplier, other, additional);
        } catch (Throwable throwable) {
            LogUtil.error(MyTraceIdContext.class, throwable);
            return null;
        } finally {
            MyTraceIdContext.clean();
        }

    }

    private static List<Integer> getRpcId() {
        if (rpcId.get() == null) {
            if (checkMainThread()) {
                ArrayList<Integer> integers = new ArrayList<>();
                integers.add(-1);
                return integers;
            }
            synchronized (MyTraceIdContext.class) {
                if (rpcId.get() == null) {
                    ArrayList<Integer> value = new ArrayList<>();
                    value.add(1);
                    rpcId.set(value);
                    thisRpcId.set(new AtomicInteger(1));
                }
            }
        }
        return rpcId.get();
    }

    /**
     * 设置RpcId
     *
     * @param lastRpcIds
     */
    public static void setRpcId(List<Integer> lastRpcIds) {
        rpcId.set(lastRpcIds);
        thisRpcId.set(new AtomicInteger(1));
    }

    private static boolean checkMainThread() {
        return MAIN_THREAD_NAME.equals(Thread.currentThread().getName());
    }

    /**
     * rpcId
     *
     * @param lastRpcIds 上一层rpcId
     * @param rpcId      这一层的RpcId
     *
     * @return
     */
    private static StringBuilder mergeRpcId(List<Integer> lastRpcIds, int rpcId) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lastRpcIds.size(); i++) {
            sb.append(lastRpcIds.get(i));
            sb.append(".");
        }
        sb.append(rpcId);
        return sb;
    }

    private static AtomicInteger getThisRpcId() {
        if (thisRpcId.get() == null) {
            if (checkMainThread()) {
                return new AtomicInteger(-1);
            }
            synchronized (MyTraceIdContext.class) {
                if (thisRpcId.get() == null) {
                    AtomicInteger integer = new AtomicInteger(1);
                    thisRpcId.set(integer);
                }
            }
        }
        return thisRpcId.get();
    }

    private static String hash(String[] additionals) {
        StringBuilder sb = new StringBuilder();
        for (String additional : additionals) {
            sb.append(additional);
            sb.append(PIPE_SYMBOL);
        }
        if (sb.length() != 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        int hash = sb.toString().hashCode() & 0xFFFFFFF;
        return HASH_SYMBOL + Integer.toUnsignedString(hash, 16);

    }
}
