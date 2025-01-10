package top.uhyils.usher.mysql.pojo.sys;

import com.alibaba.fastjson.JSONArray;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import top.uhyils.usher.context.LoginInfoHelper;
import top.uhyils.usher.enums.FieldTypeEnum;
import top.uhyils.usher.mysql.handler.MysqlServiceHandler;
import top.uhyils.usher.pojo.DTO.UserDTO;
import top.uhyils.usher.pojo.FieldInfo;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.util.Asserts;

/**
 * 该表提供存储引擎相关的信息，主要用来确认数据库是否支持该存储引擎以及是否是默认的该表不是标准的INFORMATION_SCHEMA表
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年09月14日 09时40分
 */
public class IEngines extends AbstractSysTable {


    public IEngines(MysqlServiceHandler handler, Map<String, Object> params) {
        super(handler, params);
    }

    @Override
    public NodeInvokeResult doGetResultNoParams() {
        Optional<UserDTO> userOptional = LoginInfoHelper.get();
        if (!userOptional.isPresent()) {
            throw Asserts.makeException("未登录");
        }

        JSONArray newResults = new JSONArray();

        NodeInvokeResult nodeInvokeResult = new NodeInvokeResult(null);
        nodeInvokeResult.setResult(newResults);
        List<FieldInfo> fieldInfos = new ArrayList<>();

        fieldInfos.add(new FieldInfo("information_schema", "engines", "engines", "ENGINE", "ENGINE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "engines", "engines", "SUPPORT", "SUPPORT", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "engines", "engines", "COMMENT", "COMMENT", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "engines", "engines", "TRANSACTIONS", "TRANSACTIONS", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "engines", "engines", "XA", "XA", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "engines", "engines", "SAVEPOINTS", "SAVEPOINTS", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        nodeInvokeResult.setFieldInfos(fieldInfos);
        return nodeInvokeResult;
    }
}
