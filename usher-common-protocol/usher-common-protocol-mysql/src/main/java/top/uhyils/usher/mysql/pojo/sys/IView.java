package top.uhyils.usher.mysql.pojo.sys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import top.uhyils.usher.context.LoginInfoHelper;
import top.uhyils.usher.enums.FieldTypeEnum;
import top.uhyils.usher.mysql.handler.MysqlServiceHandler;
import top.uhyils.usher.pojo.DTO.UserDTO;
import top.uhyils.usher.pojo.FieldInfo;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.util.Asserts;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年09月16日 09时54分
 */
public class IView extends AbstractSysTable {

    public IView(MysqlServiceHandler handler, Map<String, Object> params) {
        super(handler, params);
        this.params = params.entrySet().stream().collect(Collectors.toMap(t -> t.getKey().toLowerCase(), Entry::getValue));
    }

    @Override
    public NodeInvokeResult doGetResultNoParams() {
        Optional<UserDTO> userOptional = LoginInfoHelper.get();
        if (!userOptional.isPresent()) {
            throw Asserts.makeException("未登录");
        }

        List<Map<String, Object>> newResults = new ArrayList<>();

        NodeInvokeResult nodeInvokeResult = new NodeInvokeResult(null);
        nodeInvokeResult.setResult(newResults);
        List<FieldInfo> fieldInfos = new ArrayList<>();
        fieldInfos.add(new FieldInfo("information_schema", "view", "view", "TABLE_CATALOG", "TABLE_CATALOG", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "view", "view", "TABLE_SCHEMA", "TABLE_SCHEMA", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "view", "view", "TABLE_NAME", "TABLE_NAME", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "view", "view", "VIEW_DEFINITION", "VIEW_DEFINITION", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "view", "view", "CHECK_OPTION", "CHECK_OPTION", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "view", "view", "IS_UPDATABLE", "IS_UPDATABLE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "view", "view", "DEFINER", "DEFINER", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "view", "view", "SECURITY_TYPE", "SECURITY_TYPE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "view", "view", "CHARACTER_SET_CLIENT", "CHARACTER_SET_CLIENT", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "view", "view", "COLLATION_CONNECTION", "COLLATION_CONNECTION", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        nodeInvokeResult.setFieldInfos(fieldInfos);
        return nodeInvokeResult;
    }
}
