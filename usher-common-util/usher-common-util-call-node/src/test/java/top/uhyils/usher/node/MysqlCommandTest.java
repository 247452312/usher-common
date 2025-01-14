package top.uhyils.usher.node;


import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import top.uhyils.usher.content.CallNodeContent;
import top.uhyils.usher.content.CallerUserInfo;
import top.uhyils.usher.context.LoginInfoHelper;
import top.uhyils.usher.enums.FieldTypeEnum;
import top.uhyils.usher.enums.QuerySqlTypeEnum;
import top.uhyils.usher.handler.NodeHandler;
import top.uhyils.usher.handler.impl.AbstractNodeHandler;
import top.uhyils.usher.node.call.AbstractLeafNode;
import top.uhyils.usher.node.call.CallNode;
import top.uhyils.usher.node.call.SqlCallNode;
import top.uhyils.usher.pojo.CallInfo;
import top.uhyils.usher.pojo.FieldInfo;
import top.uhyils.usher.pojo.NodeInvokeResult;
import top.uhyils.usher.pojo.TableInfo;
import top.uhyils.usher.util.Asserts;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月08日 13时50分
 */
class MysqlCommandTest {

    private static final String DEFAULT_TABLE_NAME = "defaultTable";

    private NodeHandler handler = new AbstractNodeHandler() {


        @Override
        protected TableInfo findByDatabaseAndTable(String database, String table) {
            TableInfo tableInfo = new TableInfo();
            tableInfo.setNodeId(3213123L);
            tableInfo.setDatabaseName(CallNodeContent.CATALOG_NAME);
            tableInfo.setTableName(DEFAULT_TABLE_NAME);
            tableInfo.setType("aaa");
            CallInfo callInfo = new CallInfo();
            callInfo.setSupportSqlTypes(Arrays.asList(QuerySqlTypeEnum.QUERY, QuerySqlTypeEnum.INSERT, QuerySqlTypeEnum.UPDATE, QuerySqlTypeEnum.DELETE));
            Map<QuerySqlTypeEnum, JSONObject> params = new HashMap<>();
            JSONObject value = new JSONObject();
            value.put("url", "127.0.0.1:8001/action");
            value.put("method", "POST");
            params.put(QuerySqlTypeEnum.QUERY, value);
            params.put(QuerySqlTypeEnum.INSERT, value);
            params.put(QuerySqlTypeEnum.UPDATE, value);
            params.put(QuerySqlTypeEnum.DELETE, value);
            callInfo.setParams(params);

            tableInfo.setCallInfo(callInfo);
            return tableInfo;
        }
    };

    @BeforeAll
    static void beforeAll() {
        NodeFactory.addSupportType("aaa", (build, tableInfo) -> {

            String table = build.getTable();

            if (Objects.equals(table, "user")) {

                return new AbstractLeafNode(build, tableInfo) {

                    @Override
                    public NodeInvokeResult call(Map<String, String> header, JSONObject params) {
                        List<FieldInfo> fieldInfos = new ArrayList<>();
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "id", "id", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "name", "name", 0, 2, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
                        List<Map<String, Object>> result = new ArrayList<>();
                        return NodeInvokeResult.build(fieldInfos, result, null);
                    }
                };
            } else if (Objects.equals(table, "rule")) {
                return new AbstractLeafNode(build, tableInfo) {

                    @Override
                    public NodeInvokeResult call(Map<String, String> header, JSONObject params) {
                        List<FieldInfo> fieldInfos = new ArrayList<>();
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "id", "id", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "name", "name", 0, 2, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "user_id", "user_id", 0, 2, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "c_k", "c_k", 0, 2, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "k", "k", 0, 2, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
                        List<Map<String, Object>> result = new ArrayList<>();
                        return NodeInvokeResult.build(fieldInfos, result, null);
                    }
                };

            } else if (Objects.equals(table, "uu2")) {
                return new AbstractLeafNode(build, tableInfo) {

                    @Override
                    public NodeInvokeResult call(Map<String, String> header, JSONObject params) {
                        List<FieldInfo> fieldInfos = new ArrayList<>();
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "id", "id", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "name", "name", 0, 2, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
                        fieldInfos.add(new FieldInfo(build.getDatabase(), build.getTable(), build.getTable(), "r2", "r2", 0, 2, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
                        List<Map<String, Object>> result = new ArrayList<>();
                        return NodeInvokeResult.build(fieldInfos, result, null);
                    }
                };
            }
            return new AbstractLeafNode(build, tableInfo) {

                @Override
                public NodeInvokeResult call(Map<String, String> header, JSONObject params) {
                    List<FieldInfo> fieldInfos = new ArrayList<>();

                    fieldInfos.add(new FieldInfo("information_schema", "user_privileges", "user_privileges", "GRANTEE", "GRANTEE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
                    fieldInfos.add(new FieldInfo("information_schema", "user_privileges", "user_privileges", "TABLE_CATALOG", "TABLE_CATALOG", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
                    fieldInfos.add(new FieldInfo("information_schema", "user_privileges", "user_privileges", "PRIVILEGE_TYPE", "PRIVILEGE_TYPE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
                    fieldInfos.add(new FieldInfo("information_schema", "user_privileges", "user_privileges", "IS_GRANTABLE", "IS_GRANTABLE", 0, 1, FieldTypeEnum.FIELD_TYPE_VARCHAR, (short) 0, (byte) 0));
                    List<Map<String, Object>> result = new ArrayList<>();
                    return NodeInvokeResult.build(fieldInfos, result, null);
                }
            };
        });
        CallerUserInfo value = new CallerUserInfo();
        value.setDatabaseName("temp");
        value.setUserDTO(LoginInfoHelper.doGet());
        CallNodeContent.CALLER_INFO.set(value);
    }

    @Test
    void invoke() {
        CallNode callNode = new SqlCallNode("select * from user a left join role b on a.id = b.user_id", mysqlInvokeCommand -> {
            CallNode tempNode = handler.makeNode(mysqlInvokeCommand);
            return tempNode.call(mysqlInvokeCommand.getHeader(), mysqlInvokeCommand.getParams());
        });
        NodeInvokeResult call = callNode.call(null);
        int i = 1;
    }


    @Test
    void invoke2() {
        CallNode callNode = new SqlCallNode("select * from user a left join role b on a.id = b.user_id where id = #{id}", mysqlInvokeCommand -> {
            CallNode tempNode = handler.makeNode(mysqlInvokeCommand);
            return tempNode.call(mysqlInvokeCommand.getHeader(), mysqlInvokeCommand.getParams());
        });
        JSONObject params = new JSONObject();
        params.put("id", 1);
        NodeInvokeResult call = callNode.call(params);
        int i = 1;
    }

    @Test
    void invoke3() {
        CallNode callNode = new SqlCallNode("select * from user a left join (select * from rule where c_k = #{ck}) b on a.id = b.user_id where id = #{id}", mysqlInvokeCommand -> {
            CallNode tempNode = handler.makeNode(mysqlInvokeCommand);
            return tempNode.call(mysqlInvokeCommand.getHeader(), mysqlInvokeCommand.getParams());
        });
        JSONObject params = new JSONObject();
        params.put("id", 1);
        params.put("ck", 889);
        NodeInvokeResult call = callNode.call(params);
        int i = 1;
    }

    @Test
    void invoke4() {
        CallNode callNode = new SqlCallNode("select a.id,b.k,concat(a.id,b.k) from user a left join (select * from rule where c_k = #{ck}) b on a.id = b.user_id where id = #{id}", mysqlInvokeCommand -> {
            CallNode tempNode = handler.makeNode(mysqlInvokeCommand);
            return tempNode.call(mysqlInvokeCommand.getHeader(), mysqlInvokeCommand.getParams());
        });
        JSONObject params = new JSONObject();
        params.put("id", 1);
        params.put("ck", 889);
        NodeInvokeResult call = callNode.call(params);
        int i = 1;
    }

    @Test
    void invoke5() {
        CallNode callNode = new SqlCallNode("select a.id,b.k,concat(a.id,b.k),(select r2 from uu2 where r2 = a.id) as r2 from user a left join (select * from rule where c_k = #{ck}) b on a.id = b.user_id where id = #{id}", mysqlInvokeCommand -> {
            CallNode tempNode = handler.makeNode(mysqlInvokeCommand);
            return tempNode.call(mysqlInvokeCommand.getHeader(), mysqlInvokeCommand.getParams());
        });
        JSONObject params = new JSONObject();
        params.put("id", 1);
        params.put("ck", 889);
        Asserts.assertException(() -> callNode.call(params));
        int i = 1;
    }

    @Test
    void invokeUpdate1() {
        CallNode callNode = new SqlCallNode("update user set name = 'abc' where id = 19", mysqlInvokeCommand -> {
            CallNode tempNode = handler.makeNode(mysqlInvokeCommand);
            return tempNode.call(mysqlInvokeCommand.getHeader(), mysqlInvokeCommand.getParams());
        });
        JSONObject params = new JSONObject();
        NodeInvokeResult call = callNode.call(params);
        int i = 1;
    }

    @Test
    void invokeInsert1() {
        CallNode callNode = new SqlCallNode("insert into user value(1,'name')", mysqlInvokeCommand -> {
            CallNode tempNode = handler.makeNode(mysqlInvokeCommand);
            return tempNode.call(mysqlInvokeCommand.getHeader(), mysqlInvokeCommand.getParams());
        });
        JSONObject params = new JSONObject();
        NodeInvokeResult call = callNode.call(params);
        int i = 1;
    }

    @Test
    void invokeInsert2() {
        CallNode callNode = new SqlCallNode("insert into user(id,name) value(1,'name')", mysqlInvokeCommand -> {
            CallNode tempNode = handler.makeNode(mysqlInvokeCommand);
            return tempNode.call(mysqlInvokeCommand.getHeader(), mysqlInvokeCommand.getParams());
        });
        JSONObject params = new JSONObject();
        NodeInvokeResult call = callNode.call(params);
        int i = 1;
    }

    @Test
    void invokeDel1() {
        CallNode callNode = new SqlCallNode("delete from user where id = 1", mysqlInvokeCommand -> {
            CallNode tempNode = handler.makeNode(mysqlInvokeCommand);
            return tempNode.call(mysqlInvokeCommand.getHeader(), mysqlInvokeCommand.getParams());
        });
        JSONObject params = new JSONObject();
        NodeInvokeResult call = callNode.call(params);
        int i = 1;
    }
}
