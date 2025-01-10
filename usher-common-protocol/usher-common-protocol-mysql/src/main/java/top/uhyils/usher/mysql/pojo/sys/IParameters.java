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
 * 该表存放这存储过程和存储函数的参数信息以及存储函数的返回值，及我们一般意义上的存储过程和函数，统称为stored routines
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年09月13日 08时28分
 */
public class IParameters extends AbstractSysTable {


    public IParameters(MysqlServiceHandler handler, Map<String, Object> params) {
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

        fieldInfos.add(new FieldInfo("information_schema", "parameters", "parameters", "SPECIFIC_CATALOG", "SPECIFIC_CATALOG", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "parameters", "parameters", "SPECIFIC_SCHEMA", "SPECIFIC_SCHEMA", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "parameters", "parameters", "SPECIFIC_NAME", "SPECIFIC_NAME", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "parameters", "parameters", "ORDINAL_POSITION", "ORDINAL_POSITION", 0, 1, FieldTypeEnum.FIELD_TYPE_INT24, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "parameters", "parameters", "PARAMETER_MODE", "PARAMETER_MODE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "parameters", "parameters", "PARAMETER_NAME", "PARAMETER_NAME", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "parameters", "parameters", "DATA_TYPE", "DATA_TYPE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "parameters", "parameters", "CHARACTER_MAXIMUM_LENGTH", "CHARACTER_MAXIMUM_LENGTH", 0, 1, FieldTypeEnum.FIELD_TYPE_INT24, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "parameters", "parameters", "CHARACTER_OCTET_LENGTH", "CHARACTER_OCTET_LENGTH", 0, 1, FieldTypeEnum.FIELD_TYPE_INT24, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "parameters", "parameters", "NUMERIC_PRECISION", "NUMERIC_PRECISION", 0, 1, FieldTypeEnum.FIELD_TYPE_INT24, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "parameters", "parameters", "NUMERIC_SCALE", "NUMERIC_SCALE", 0, 1, FieldTypeEnum.FIELD_TYPE_INT24, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "parameters", "parameters", "DATETIME_PRECISION", "DATETIME_PRECISION", 0, 1, FieldTypeEnum.FIELD_TYPE_INT24, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "parameters", "parameters", "CHARACTER_SET_NAME", "CHARACTER_SET_NAME", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "parameters", "parameters", "COLLATION_NAME", "COLLATION_NAME", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "parameters", "parameters", "DTD_IDENTIFIER", "DTD_IDENTIFIER", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "parameters", "parameters", "ROUTINE_TYPE", "ROUTINE_TYPE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        nodeInvokeResult.setFieldInfos(fieldInfos);
        return nodeInvokeResult;
    }
}
