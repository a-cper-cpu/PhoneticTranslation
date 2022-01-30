package com.example.listview;



import java.security.MessageDigest;

/**
 * 加密解密工具类(对字符串加密) MD5加密
 */
public class MD5Utils {

    /**
     * MD5加密算法使用 对字符串加密
     *
     * @param info 参数为需要加密的String
     * @return 返回加密后的String
     */
    public static String getMD5Code(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("utf-8"));//设置编码格式
            byte[] encryption = md5.digest();
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < encryption.length; i++) {
                if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
                    stringBuffer.append("0").append(Integer.toHexString(0xff & encryption[i]));
                } else {
                    stringBuffer.append(Integer.toHexString(0xff & encryption[i]));
                }
            }
            return stringBuffer.toString();
        } catch (Exception e) {
            return "MD5加密异常";
        }
    }

}
