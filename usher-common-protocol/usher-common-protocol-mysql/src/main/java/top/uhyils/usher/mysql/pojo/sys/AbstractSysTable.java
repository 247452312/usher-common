package top.uhyils.usher.mysql.pojo.sys;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import top.uhyils.usher.mysql.handler.MysqlServiceHandler;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.util.MapUtil;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2023年08月01日 10时14分
 */
public abstract class AbstractSysTable implements SysTable {

    /**
     * 入参
     */
    protected Map<String, Object> params;

    protected MysqlServiceHandler handler;

    protected AbstractSysTable(MysqlServiceHandler handler, Map<String, Object> params) {
        this.params = params;
        this.handler = handler;
    }

    @Override
    public NodeInvokeResult getResult() {
        NodeInvokeResult nodeInvokeResult = doGetResultNoParams();
        if (MapUtil.isEmpty(params)) {
            return nodeInvokeResult;
        }

        List<Map<String, Object>> result = nodeInvokeResult.getResult();
        Iterator<Map<String, Object>> iterator = result.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> jsonNext = iterator.next();
            boolean pass = true;
            // 遍历条件.筛选
            for (Entry<String, Object> param : params.entrySet()) {
                Object o = jsonNext.get(param.getKey().toUpperCase());
                if (param.getValue() instanceof List) {
                    if (!((List<?>) param.getValue()).contains(o)) {
                        pass = false;
                        break;
                    }
                } else if (!Objects.equals(o, param.getValue())) {
                    pass = false;
                    break;
                }
            }
            if (!pass) {
                iterator.remove();
            }
        }
        return nodeInvokeResult;
    }

    /**
     * 无参数获取指定结果
     *
     * @return
     */
    protected abstract NodeInvokeResult doGetResultNoParams();


}
