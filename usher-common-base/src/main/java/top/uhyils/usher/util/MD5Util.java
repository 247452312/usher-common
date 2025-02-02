package top.uhyils.usher.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2020年04月26日 14时46分
 */
public final class MD5Util {

    public static final String MD5 = "MD5";

    private MD5Util() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * MD5加密
     *
     * @param str 待加密的字符串
     *
     * @return 密文(16位)
     */
    public static String MD5Encode(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance(MD5);
            md.update(str.getBytes());
            byte[] b = md.digest();
            int i;
            StringBuilder buf = new StringBuilder();
            for (byte value : b) {
                i = value;
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            return buf.substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            LogUtil.error(MD5Util.class, e);
        }
        return null;
    }

    public static void main(String[] args) {
        String s = MD5Encode("123456");
        LogUtil.info(s);
    }
}
