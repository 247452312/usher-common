package top.uhyils.usher.node;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import top.uhyils.usher.context.LoginInfoHelper;
import top.uhyils.usher.node.call.AbstractLeafNode;
import top.uhyils.usher.node.call.CallNode;
import top.uhyils.usher.node.call.SqlCallNode;
import top.uhyils.usher.node.content.CallNodeContent;
import top.uhyils.usher.node.content.CallerUserInfo;
import top.uhyils.usher.node.enums.FieldTypeEnum;
import top.uhyils.usher.node.handler.MysqlServiceHandler;
import top.uhyils.usher.util.Asserts;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月08日 13时50分
 */
class MysqlCommandTest {

    private MysqlServiceHandler handler = new MysqlServiceHandler() {

        @Override
        public CallNode makeNode(MysqlInvokeCommand build) {
            String table = build.getTable();
            if (Objects.equals(table, "user")) {
                return new AbstractLeafNode() {

                    @Override
                    public NodeInvokeResult call(Map<String, String> header, JSONObject params) {
                        List<FieldInfo> fieldInfos = new ArrayList<>();
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "id", "id", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR.getClazz(), (short) 0, (byte) 0));
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "name", "name", 0, 2, FieldTypeEnum.FIELD_TYPE_VARCHAR.getClazz(), (short) 0, (byte) 0));
                        JSONArray result = new JSONArray();
                        return NodeInvokeResult.build(fieldInfos, result, null);
                    }
                };
            } else if (Objects.equals(table, "rule")) {
                return new AbstractLeafNode() {

                    @Override
                    public NodeInvokeResult call(Map<String, String> header, JSONObject params) {
                        List<FieldInfo> fieldInfos = new ArrayList<>();
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "id", "id", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR.getClazz(), (short) 0, (byte) 0));
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "name", "name", 0, 2, FieldTypeEnum.FIELD_TYPE_VARCHAR.getClazz(), (short) 0, (byte) 0));
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "user_id", "user_id", 0, 2, FieldTypeEnum.FIELD_TYPE_VARCHAR.getClazz(), (short) 0, (byte) 0));
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "c_k", "c_k", 0, 2, FieldTypeEnum.FIELD_TYPE_VARCHAR.getClazz(), (short) 0, (byte) 0));
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "k", "k", 0, 2, FieldTypeEnum.FIELD_TYPE_VARCHAR.getClazz(), (short) 0, (byte) 0));
                        JSONArray result = new JSONArray();
                        return NodeInvokeResult.build(fieldInfos, result, null);
                    }
                };

            } else if (Objects.equals(table, "uu2")) {
                return new AbstractLeafNode() {

                    @Override
                    public NodeInvokeResult call(Map<String, String> header, JSONObject params) {
                        List<FieldInfo> fieldInfos = new ArrayList<>();
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "id", "id", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR.getClazz(), (short) 0, (byte) 0));
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "name", "name", 0, 2, FieldTypeEnum.FIELD_TYPE_VARCHAR.getClazz(), (short) 0, (byte) 0));
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "r2", "r2", 0, 2, FieldTypeEnum.FIELD_TYPE_VARCHAR.getClazz(), (short) 0, (byte) 0));
                        JSONArray result = new JSONArray();
                        return NodeInvokeResult.build(fieldInfos, result, null);
                    }
                };
            }
            return new AbstractLeafNode() {

                @Override
                public NodeInvokeResult call(Map<String, String> header, JSONObject params) {
                    List<FieldInfo> fieldInfos = new ArrayList<>();

                    fieldInfos.add(new FieldInfo("information_schema", "user_privileges", "user_privileges", "GRANTEE", "GRANTEE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR.getClazz(), (short) 0, (byte) 0));
                    fieldInfos.add(new FieldInfo("information_schema", "user_privileges", "user_privileges", "TABLE_CATALOG", "TABLE_CATALOG", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR.getClazz(), (short) 0, (byte) 0));
                    fieldInfos.add(new FieldInfo("information_schema", "user_privileges", "user_privileges", "PRIVILEGE_TYPE", "PRIVILEGE_TYPE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR.getClazz(), (short) 0, (byte) 0));
                    fieldInfos.add(new FieldInfo("information_schema", "user_privileges", "user_privileges", "IS_GRANTABLE", "IS_GRANTABLE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR.getClazz(), (short) 0, (byte) 0));
                    JSONArray result = new JSONArray();
                    return NodeInvokeResult.build(fieldInfos, result, null);
                }
            };
        }
    };

    @BeforeAll
    static void beforeAll() {
        CallerUserInfo value = new CallerUserInfo();
        value.setDatabaseName("temp");
        value.setUserDTO(LoginInfoHelper.doGet());
        CallNodeContent.CALLER_INFO.set(value);
    }

    @Test
    void invoke() {
        CallNode callNode = new SqlCallNode("select * from user a left join role b on a.id = b.user_id", handler);
        NodeInvokeResult call = callNode.call(null);
        int i = 1;
    }


    @Test
    void invoke2() {
        CallNode callNode = new SqlCallNode("select * from user a left join role b on a.id = b.user_id where id = #{id}", handler);
        JSONObject params = new JSONObject();
        params.put("id", 1);
        NodeInvokeResult call = callNode.call(params);
        int i = 1;
    }

    @Test
    void invoke3() {
        CallNode callNode = new SqlCallNode("select * from user a left join (select * from rule where c_k = #{ck}) b on a.id = b.user_id where id = #{id}", handler);
        JSONObject params = new JSONObject();
        params.put("id", 1);
        params.put("ck", 889);
        NodeInvokeResult call = callNode.call(params);
        int i = 1;
    }

    @Test
    void invoke4() {
        CallNode callNode = new SqlCallNode("select a.id,b.k,concat(a.id,b.k) from user a left join (select * from rule where c_k = #{ck}) b on a.id = b.user_id where id = #{id}", handler);
        JSONObject params = new JSONObject();
        params.put("id", 1);
        params.put("ck", 889);
        NodeInvokeResult call = callNode.call(params);
        int i = 1;
    }

    @Test
    void invoke5() {
        CallNode callNode = new SqlCallNode("select a.id,b.k,concat(a.id,b.k),(select r2 from uu2 where r2 = a.id) as r2 from user a left join (select * from rule where c_k = #{ck}) b on a.id = b.user_id where id = #{id}", handler);
        JSONObject params = new JSONObject();
        params.put("id", 1);
        params.put("ck", 889);
        Asserts.assertException(() -> callNode.call(params));
        int i = 1;
    }

    @Test
    void invokeUpdate1() {
        CallNode callNode = new SqlCallNode("update user set name = 'abc' where id = 19", handler);
        JSONObject params = new JSONObject();
        NodeInvokeResult call = callNode.call(params);
        int i = 1;
    }

    @Test
    void invokeInsert1() {
        CallNode callNode = new SqlCallNode("insert into user value(1,'name')", handler);
        JSONObject params = new JSONObject();
        NodeInvokeResult call = callNode.call(params);
        int i = 1;
    }

    @Test
    void invokeInsert2() {
        CallNode callNode = new SqlCallNode("insert into user(id,name) value(1,'name')", handler);
        JSONObject params = new JSONObject();
        NodeInvokeResult call = callNode.call(params);
        int i = 1;
    }

    @Test
    void invokeDel1() {
        CallNode callNode = new SqlCallNode("delete from user where id = 1", handler);
        JSONObject params = new JSONObject();
        NodeInvokeResult call = callNode.call(params);
        int i = 1;
    }
}
