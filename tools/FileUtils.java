/**
 * 
 */
package inetbas.web.outsys.tools;

import inetbas.cli.cutil.CCliTool;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author www.bip-soft.com
 *
 */
public class FileUtils {
	private static Logger _log = LoggerFactory.getLogger(FileUtils.class);
	/***
	 * 合并文件
	 * @param newFile 新的文件名称
	 * @param fils 文件map<放置的是第几项的文件>
	 * @param delPatch <是否删除Map中的文件>
	 * @throws IOException IO异常
	 */
	public static synchronized boolean mergeFile(String newFile,Map<String, String> fils,boolean delPatch) throws IOException {
        _log.info("开始合并文件");
        if(fils==null||fils.size()==0)
        	return false;
		File file = new File(newFile);
        file.createNewFile();
        RandomAccessFile in = new RandomAccessFile(file, "rw");
        in.setLength(0);
        in.seek(0);
        byte[] bytes = new byte[1024];
        int len = -1;
        for(int i = 0; i < fils.size(); i++) {
        	String fildir = fils.get(i+"");
        	_log.info(fildir);
            File f = new File(fils.get(i+""));
            if(f.exists()){
            	RandomAccessFile out = new RandomAccessFile(f,"rw");
            	while((len = out.read(bytes)) != -1) {
            		in.write(bytes, 0, len);
            	}
            	out.close();
            	if(delPatch)
            		f.delete();
            }
        }
        in.close();
        _log.info("结束合并文件");
        return true;
    }
	
    public static String getTimeStamp(){
    	Calendar calendar = Calendar.getInstance();
    	StringBuffer dt1 = new StringBuffer(CCliTool.dateToString(calendar, false, 1));
    	String dt2 = System.currentTimeMillis()+"";
    	return dt1.append(dt2).toString();
    }
}
