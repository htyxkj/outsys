/**
 * 
 */
package inetbas.web.outsys.tools;

import java.util.HashMap;

/**
 * @author www.bip-soft.com
 *
 */
public class CellsSessionUtil {
	private static HashMap<String,Object> _hmdu = new HashMap<String,Object>();// ;--账套Cells
	public static void cacheCells(String dbid,String cellID, Object cell) {
		_hmdu.put(dbid+"."+cellID, cell);
	}
	
	public static Object getCellsByCellId(String dbid,String cellid){
		String key = dbid+"."+cellid;
		if(_hmdu.containsKey(key)){
			return _hmdu.get(key);
		}
		return null;
	}
}
