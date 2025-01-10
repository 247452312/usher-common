package top.uhyils.usher.plan;


import java.util.Map;
import java.util.function.Function;
import top.uhyils.usher.enums.SqlPlanTypeEnum;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.pojo.SqlInvokeCommand;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年03月29日 08时40分
 */
public interface SqlPlan {

    /**
     * 执行执行计划
     *
     * @return
     */
    NodeInvokeResult invoke(Map<String, String> headers);

    /**
     * 补全执行计划参数
     *
     * @param planArgs 计划参数<执行计划id,执行计划结果>
     */
    void complete(Map<Long, NodeInvokeResult> planArgs, Function<SqlInvokeCommand, NodeInvokeResult> handler);

    /**
     * 获取此执行计划的类型
     *
     * @return
     */
    SqlPlanTypeEnum type();

    /**
     * 获取此plan唯一标示
     *
     * @return
     */
    long getId();


}
