package top.uhyils.usher.mysql.pojo.sys;

import com.alibaba.fastjson.JSONArray;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import top.uhyils.usher.enums.FieldTypeEnum;
import top.uhyils.usher.mysql.handler.MysqlServiceHandler;
import top.uhyils.usher.pojo.FieldInfo;
import top.uhyils.usher.pojo.NodeInvokeResult;

/**
 * 排序规则
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2023年08月02日 10时39分
 */
public class ICollatinos extends AbstractSysTable {


    public ICollatinos(MysqlServiceHandler handler, Map<String, Object> params) {
        super(handler, params);
    }

    @Override
    protected NodeInvokeResult doGetResultNoParams() {
        NodeInvokeResult nodeInvokeResult = new NodeInvokeResult(null);

        JSONArray result = new JSONArray();
        nodeInvokeResult.setResult(result);
        List<FieldInfo> fieldInfos = new ArrayList<>();
        fieldInfos.add(new FieldInfo("information_schema", "collatinos", "collatinos", "COLLATION_NAME", "COLLATION_NAME", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "collatinos", "collatinos", "CHARACTER_SET_NAME", "CHARACTER_SET_NAME", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "collatinos", "collatinos", "ID", "ID", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "collatinos", "collatinos", "IS_DEFAULT", "IS_DEFAULT", 0, 1, FieldTypeEnum.FIELD_TYPE_INT24, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "collatinos", "collatinos", "IS_COMPILED", "IS_COMPILED", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "collatinos", "collatinos", "SORTLEN", "SORTLEN", 0, 1, FieldTypeEnum.FIELD_TYPE_INT24, (short) 0, (byte) 0));
        nodeInvokeResult.setFieldInfos(fieldInfos);
        return nodeInvokeResult;
    }
}
