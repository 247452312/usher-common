package top.uhyils.usher.mysql.pojo.sys;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import top.uhyils.usher.enums.FieldTypeEnum;
import top.uhyils.usher.mysql.assembler.CompanyAssembler;
import top.uhyils.usher.mysql.handler.MysqlServiceHandler;
import top.uhyils.usher.mysql.pojo.DTO.CompanyInfo;
import top.uhyils.usher.mysql.pojo.DTO.IUserPrivilegesInfo;
import top.uhyils.usher.mysql.pojo.cqe.UserQuery;
import top.uhyils.usher.pojo.FieldInfo;
import top.uhyils.usher.pojo.NodeInvokeResult;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2023年08月23日 13时51分
 */
public class IUserPrivileges extends AbstractSysTable {


    public IUserPrivileges(MysqlServiceHandler handler, Map<String, Object> params) {
        super(handler, params);
    }

    @Override
    protected NodeInvokeResult doGetResultNoParams() {
        List<FieldInfo> fieldInfos = new ArrayList<>();
        /*数据从company里取,*/
        List<CompanyInfo> users = handler.queryUser(new UserQuery());

        List<IUserPrivilegesInfo> userInfos = new ArrayList<>();
        for (CompanyInfo user : users) {
            IUserPrivilegesInfo mUserInfo = CompanyAssembler.INSTANCE.toIUserPrivileges(user);
            userInfos.add(mUserInfo);
        }

        fieldInfos.add(new FieldInfo("information_schema", "user_privileges", "user_privileges", "GRANTEE", "GRANTEE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "user_privileges", "user_privileges", "TABLE_CATALOG", "TABLE_CATALOG", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "user_privileges", "user_privileges", "PRIVILEGE_TYPE", "PRIVILEGE_TYPE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        fieldInfos.add(new FieldInfo("information_schema", "user_privileges", "user_privileges", "IS_GRANTABLE", "IS_GRANTABLE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
        JSONArray objects = JSON.parseArray(JSON.toJSONString(userInfos));
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.getJSONObject(i);
            result.add(jsonObject);
        }

        return NodeInvokeResult.build(fieldInfos, result, null);
    }
}
