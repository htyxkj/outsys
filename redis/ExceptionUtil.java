package inetbas.web.outsys.redis;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by www.bip-soft.com on 2017/3/16.
 */
public class ExceptionUtil {
    public static String getTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        throwable.printStackTrace(writer);
        StringBuffer buffer = stringWriter.getBuffer();
        return buffer.toString();
    }
}
