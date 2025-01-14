package top.uhyils.usher.mysql.util;

import org.junit.jupiter.api.Test;
import top.uhyils.usher.util.Asserts;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月14日 15时23分
 */
class MysqlUtilTest {

    @Test
    public void longToByte() {
        long l = 620L;
        byte[] bytes = MysqlUtil.toBytes(l);
        Asserts.assertEqual(bytes[0], (byte) 108);
        Asserts.assertEqual(bytes[1], (byte) 2);
    }

    @Test
    public void longToByte2() {
        long l = 620L;
        byte[] bytes = MysqlUtil.toBytes(l, 3);
        Asserts.assertEqual(bytes[0], (byte) 108);
        Asserts.assertEqual(bytes[1], (byte) 2);
    }

    @Test
    public void stringToByte() {
        String value = "/* ApplicationName=IntelliJ IDEA 2023.3.5 */ select tt.akfjlashdlfkjahsdf as c,tt.bjliakdhflakshjdf,tt.ciuovhoaiusdvh,tt.dkjqherlkqjrhbe,tt.eiuabyosiuyboaisuybauiydaiou,tt.fiuabyosiuyboaisuybauiydaiou,tt.giuabyosiuyboaisuybauiydaiou,tt.hiuabyosiuyboaisuybauiydaiou,tt.iiuabyosiuyboaisuybauiydaiou,tt.jiuabyosiuyboaisuybauiydaiou,tt.kiuabyosiuyboaisuybauiydaiou,tt.liuabyosiuyboaisuybauiydaiou,tt.miuabyosiuyboaisuybauiydaiou,tt.niuabyosiuyboaisuybauiydaioufrom from temp_db.temp_tb tt where token = '123'\n";
        byte[] bytes = MysqlUtil.mergeLengthCodedBinary(value);
        int length = value.length();
        byte[] bytes1 = MysqlUtil.toBytes(length);
        System.out.println(MysqlUtil.dump(bytes1));
        System.out.println(MysqlUtil.dump(bytes));
        Asserts.assertEqual(bytes[0], (byte) 108);
        Asserts.assertEqual(bytes[1], (byte) 2);
    }

}
