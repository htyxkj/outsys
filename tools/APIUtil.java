/**
 * 
 */
package inetbas.web.outsys.tools;
  
import inetbas.web.webpage.LoginPreProc;
import inetbas.web.webpage.WDBCFG;
import inetbas.webserv.SErvVars;
import inetbas.webserv.WSTool;
import inetbas.webserv.WebAppPara;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author www.bip-soft.com
 * 
 */
public class APIUtil {
	private static Logger _log = LoggerFactory.getLogger(APIUtil.class);
	@SuppressWarnings("rawtypes")
	private static HashMap _hmdu = new HashMap();// ;--账套用户数据
	@SuppressWarnings({ "rawtypes" })
	public static void cpTOHttpSession(HashMap hm, HttpSession hss) {
		Iterator ols = hm.keySet().iterator();
		Object oc;
		while (ols.hasNext()) {
			oc = ols.next();
			if (oc instanceof String)
				hss.setAttribute((String) oc, hm.get(oc));
		}
	}

	public static void exit(String dbid, String user) {
		_hmdu.remove(dbid + "." + user);// ;--删除某一个登陆。
	}

	/**
	 * 不存在时返回为空。
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static HashMap<String,Object> getdbuser(String dbid, String user) {
		if(user==null||dbid==null){
			return new HashMap<String, Object>();
		}
		dbid += "." + user;
		synchronized (_hmdu) {
			HashMap hm = (HashMap) _hmdu.get(dbid);
			if (hm == null) {
				hm = new HashMap();
				_hmdu.put(dbid, hm);
			}
			return hm;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object login_a(Object value, HashMap hs) {
		String dbid = (String) hs.get(cl.ICL.DB_ID_T);
		hs.put(cl.ICL.DB_ID, dbid);
		String s0 = SErvVars.readPara(dbid + ".title");
		if (s0 == null || s0.length() < 1)
			s0 = SErvVars.TITLE;
		Object[] os0 = (Object[]) value;
		Map ocv = (Map) os0[0];
		LoginPreProc.toHashtable(hs, ocv);
		hs.put(cl.INN.ORGUSR, WSTool.makeorgusr(ocv));
		String s1 = SErvVars.rm_usern;
		if (s1 != null && s1.length() > 0)
			s0 = s0 + "-" + s1;// ;-增加单位名称识别号
		hs.put("TITLE", s0);
		String usercode = (String)ocv.get(cl.ICL.USRCODE);
		_hmdu.put(dbid+"."+usercode, hs);
		_log.info("登录后用户：",_hmdu);
		return new Object[] { os0[1], os0[2], os0[3] };
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean login_b(WebAppPara wa, HashMap hm, String sal,
			String dbid, String saddr) {
		Object[] ops = wa.params;
		if (dbid == null || dbid.length() < 1)
			throw new RuntimeException("system config error:" + dbid); 
		WDBCFG cfg = LoginPreProc.login_cfg(dbid, (String) ops[2]);
		Map ovs = SErvVars.getAllVars();
		hm.put(cl.ICL.DB_TYPE, new Integer(cfg.dbtype));
		String s0 = cfg.dbLang;
		if (s0 != null && s0.length() > 0) {
			wa.setLANG(s0);
			hm.put(cl.ICL.WA_LANG, s0);
		}
		int attr = cfg.attr;
		hm.put(cl.ICL.WA_ATTR, new Integer(attr));
		hm.put(cl.ICL.CLI_ATTR, cfg.cliattr);
		hm.put(cl.ICL.DB_ID_T, dbid);
		hm.put("SYSVID", cfg.sysvid);
		wa.attr = attr;
		wa.db_id = dbid;
		wa.oprcmd = sal;
		if (wa.oprid == cl.ICL.RQ_UNDEF)
			wa.oprid = cl.ICL.RQ_LOGIN;// ;--系统登录
		s0 = (String) ops[3];
		if (s0 != null && s0.length() > 0)
			saddr += "/" + s0;
		hm.put(cl.ICL.CLI_INFO, saddr);
		ops[3] = saddr;
		if ((attr & cl.ICL.WA_RMDB) != 0) {
			ovs.put(dbid + ".user", ops[0]);// ;--起用远程
			Object o0 = ops[1];
			if (o0 != null)
				ovs.put(dbid + ".password", o0);
			o0 = ops[4];
			s0 = o0 == null ? null : o0.toString();
			if (s0 != null && s0.length() > 0)
				ovs.put(dbid + "." + cl.ICL.F_CORP, s0);
		}
		return true;
	}
	
    public static boolean isMessyCode(String strName) {
        Pattern p = Pattern.compile("\\s*|t*|r*|n*");
        Matcher m = p.matcher(strName);
        String after = m.replaceAll("");
        String temp = after.replaceAll("\\p{P}", "");
        char[] ch = temp.trim().toCharArray();
        float chLength = ch.length;
        float count = 0;
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (!Character.isLetterOrDigit(c)) {
                if (!isChinese(c)) {
                    count = count + 1;
                }
            }
        }
        float result = count / chLength;
        if (result > 0.4) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }
}
