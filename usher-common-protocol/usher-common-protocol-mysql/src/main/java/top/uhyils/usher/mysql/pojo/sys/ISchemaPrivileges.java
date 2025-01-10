package top.uhyils.usher.mysql.pojo.sys;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import top.uhyils.usher.content.CallNodeContent;
import top.uhyils.usher.enums.FieldTypeEnum;
import top.uhyils.usher.mysql.handler.MysqlServiceHandler;
import top.uhyils.usher.mysql.pojo.DTO.ISchemaPrivilegesInfo;
import top.uhyils.usher.mysql.pojo.DTO.TableDTO;
import top.uhyils.usher.mysql.pojo.cqe.TableQuery;
import top.uhyils.usher.pojo.FieldInfo;
import top.uhyils.usher.pojo.NodeInvokeResult;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2023年08月23日 13时51分
 */
public class ISchemaPrivileges extends AbstractSysTable {

    public ISchemaPrivileges(MysqlServiceHandler handler, Map<String, Object> params) {
        super(handler, params);
    }

    public ISchemaPrivilegesInfo toISchemaPrivilegesInfo(TableDTO callNodeDTO) {
        ISchemaPrivilegesInfo iSchemaPrivilegesInfo = new ISchemaPrivilegesInfo();
        iSchemaPrivilegesInfo.setGrantee(callNodeDTO.getDatabase() + "@%");
        iSchemaPrivilegesInfo.setTableCatalog(CallNodeContent.CATALOG_NAME);
        iSchemaPrivilegesInfo.setPrivilegeType("SELECT");
        iSchemaPrivilegesInfo.setIsGrantable(CallNodeContent.SQL_NO);
        iSchemaPrivilegesInfo.setTableSchema(callNodeDTO.getDatabase());
        return iSchemaPrivilegesInfo;
    }

    @Override
    protected NodeInvokeResult doGetResultNoParams() {
        List<FieldInfo> fieldInfos = new ArrayList<>();
        /*数据从company里取,*/
        List<TableDTO> callNodeDTOS = handler.queryTable(new TableQuery());

        List<ISchemaPrivilegesInfo> userInfos = new ArrayList<>();
        for (TableDTO user : callNodeDTOS) {
            ISchemaPrivilegesInfo mUserInfo = toISchemaPrivilegesInfo(user);
            userInfos.add(mUserInfo);
        }

        fieldInfos.add(new FieldInfo("information_schema", "schema_privileges", "schema_privileges", "GRANTEE", "GRANTEE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "schema_privileges", "schema_privileges", "TABLE_CATALOG", "TABLE_CATALOG", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "schema_privileges", "schema_privileges", "TABLE_SCHEMA", "TABLE_SCHEMA", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "schema_privileges", "schema_privileges", "PRIVILEGE_TYPE", "PRIVILEGE_TYPE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "schema_privileges", "schema_privileges", "IS_GRANTABLE", "IS_GRANTABLE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        JSONArray objects = JSON.parseArray(JSON.toJSONString(userInfos));
        JSONArray result = new JSONArray();
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.getJSONObject(i);
            result.add(jsonObject);
        }

        return NodeInvokeResult.build(fieldInfos, result, null);
    }

}
