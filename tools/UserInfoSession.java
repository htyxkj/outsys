/**
 * 
 */
package inetbas.web.outsys.tools;

import java.util.HashMap;

/**
 * 用户登录信息
 * @author www.bip-soft.com
 *
 */
public class UserInfoSession {
	private static HashMap<String,Object> _hmdu = new HashMap<String,Object>();//用户登录信息
	public static void cacheCells(String sessionId,Object hm) {
		_hmdu.put(sessionId,hm);
	}
	/***
	 * 退出时，清空缓存中的用户
	 * @param sessionId
	 */
	public static void exit(String sessionId){
		_hmdu.remove(sessionId);
	}
	/***
	 * 获取当前用户信息
	 * @param sessionID
	 * @return
	 */
	public static HashMap<String,Object> getUserInfo(String sessionID) {
		synchronized (_hmdu) {
			@SuppressWarnings("unchecked")
			HashMap<String,Object> hm = (HashMap<String,Object>) _hmdu.get(sessionID);
			if (hm == null) {
				hm = new HashMap<String,Object>();
				_hmdu.put(sessionID, hm);
			}
			return hm;
		}
	}

}
