/**
 * 
 */
package inetbas.web.outsys.tools;

import java.io.UnsupportedEncodingException;



/**
 * @author www.bip-soft.com
 *
 */
public class EDCodeUtil {
	 /**
     * 对给定的字符串进行base64解码操作
     */
    public static String decodeData(String inputData) {
        try {
            if (null == inputData) {
                return null;
            }
            //return new String(CCliTool.decode64(inputData.getBytes("utf-8")), "utf-8");
            return new String(Base64.decodeBase64(inputData.getBytes("utf-8")), "utf-8");
        } catch (UnsupportedEncodingException e) {
        	e.printStackTrace();
        }
        return null;
    }

    /**
     * 对给定的字符串进行base64加密操作
     */
    public static String encodeData(String inputData) {
        try {
            if (null == inputData) {
                return null;
            }
            return new String(Base64.encodeBase64(inputData.getBytes("utf-8")), "utf-8");
        } catch (UnsupportedEncodingException e) {
        	e.printStackTrace();
        }
        return null;
    }
}
