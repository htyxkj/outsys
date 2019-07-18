/**
 * 
 */
package inetbas.web.outsys.api;

import inetbas.cli.cutil.CCliTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author www.bip-soft.com
 *
 */
public class SessionUtil {
	 public SessionUtil() {
	    }

	    public static <T> void setSession(T entity, HttpServletRequest request, String key) {
	        HttpSession session = request.getSession();
	        session.setAttribute(key, entity);
	    }

	    @SuppressWarnings("unchecked")
		public static <T> T getSession(HttpServletRequest request, String key) {
	        HttpSession session = request.getSession();
	        return  (T) session.getAttribute(key);
	    }

	    public static boolean remove(HttpServletRequest request, String key) {
	        HttpSession session = request.getSession();
	        session.removeAttribute(key);
	        Object object = getSession(request, key);
	        return CCliTool.isNull(object,true);
	    }

}
