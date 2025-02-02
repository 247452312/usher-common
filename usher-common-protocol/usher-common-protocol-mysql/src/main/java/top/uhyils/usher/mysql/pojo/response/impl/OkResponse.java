package top.uhyils.usher.mysql.pojo.response.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import top.uhyils.usher.mysql.enums.MysqlServerStatusEnum;
import top.uhyils.usher.mysql.enums.SqlTypeEnum;
import top.uhyils.usher.mysql.pojo.response.AbstractMysqlResponse;
import top.uhyils.usher.mysql.util.MysqlUtil;
import top.uhyils.usher.util.Asserts;


/**
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年11月03日 20时38分
 */
public class OkResponse extends AbstractMysqlResponse {

    /**
     * sql类型
     */
    private SqlTypeEnum sqlTypeEnum;

    /**
     * 受影响行数
     */
    private long rowLength;

    /**
     * 索引id值
     */
    private long indexIdValue;

    /**
     * 数据库状态
     */
    private MysqlServerStatusEnum serverStatus;

    /**
     * 告警计数
     */
    private int warnCount;

    /**
     * 带回的消息
     */
    private String msg;

    /**
     * 固定标识是不是00 默认是00
     */
    private Boolean startWithZero = Boolean.TRUE;

    /**
     * @param sqlTypeEnum  sql类型
     * @param rowLength    受影响行数
     * @param indexIdValue 索引id值
     * @param serverStatus 数据库状态
     * @param warnCount    告警计数
     * @param msg          带回的消息
     */
    public OkResponse(SqlTypeEnum sqlTypeEnum, long rowLength, long indexIdValue, MysqlServerStatusEnum serverStatus, int warnCount, String msg) {
        super();
        this.sqlTypeEnum = sqlTypeEnum;
        this.rowLength = rowLength;
        this.indexIdValue = indexIdValue;
        this.serverStatus = serverStatus;
        this.warnCount = warnCount;
        this.msg = msg;
    }


    public OkResponse(SqlTypeEnum sqlTypeEnum) {
        super();
        this.sqlTypeEnum = sqlTypeEnum;
    }

    public OkResponse(SqlTypeEnum sqlTypeEnum, Boolean startWithZero) {
        this.sqlTypeEnum = sqlTypeEnum;
        this.startWithZero = startWithZero;
    }

    public OkResponse() {
        super();
    }

    @Override
    public String toResponseStr() {
        return "服务器处理成功";
    }

    @Override
    public byte getFirstByte() {
        // ok报文恒为0x00
        return 0x00;
    }

    @Override
    public List<byte[]> toByteNoMarkIndex() {
        Asserts.assertTrue(sqlTypeEnum != null);
        //        Asserts.assertTrue(sqlTypeEnum == null || sqlTypeEnum != SqlTypeEnum.QUERY, "查询不能返回OK消息");
        return Arrays.asList(mergeOk());
    }

    public SqlTypeEnum getSqlTypeEnum() {
        return sqlTypeEnum;
    }

    private byte[] mergeOk() {
        List<byte[]> listResult = new ArrayList<>();
        // 默认需要FE 或者 00
        listResult.add(Boolean.TRUE.equals(startWithZero) ? new byte[]{0x00} : new byte[]{(byte) 0xFE});
        // 添加影响行数报文
        byte[] e = MysqlUtil.mergeLengthCodedBinary(rowLength);
        listResult.add(e);
        // 添加索引id值
        byte[] e1 = MysqlUtil.mergeLengthCodedBinary(indexIdValue);
        listResult.add(e1);
        if (serverStatus == null) {
            serverStatus = MysqlServerStatusEnum.SERVER_STATUS_NO_BACKSLASH_ESCAPES;
        }
        // 添加服务器状态
        byte[] e2 = MysqlUtil.toBytes(serverStatus.getCode());
        listResult.add(e2);
        // 添加告警计数
        byte[] e3 = MysqlUtil.toBytes(warnCount, 2);
        listResult.add(e3);
        if (msg != null) {
            // 添加服务器消息
            byte[] bytes1 = msg.getBytes(StandardCharsets.UTF_8);
            listResult.add(bytes1);
        }
        return MysqlUtil.mergeListBytes(listResult);
    }


}
