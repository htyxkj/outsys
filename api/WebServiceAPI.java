package inetbas.web.outsys.api;

import inet.HVector;
import inetbas.cli.cutil.CCliTool;
import inetbas.pub.coob.CBasTool;
import inetbas.pub.coob.CData;
import inetbas.pub.coob.CIDVAL;
import inetbas.pub.coob.CRecord;
import inetbas.pub.coob.Cell;
import inetbas.pub.coob.Cells;
import inetbas.pub.cutil.CPubTool;
import inetbas.sserv.SQLConnection;
import inetbas.sserv.SQLExecQuery;
import inetbas.web.cutil.WAToolkit;
import inetbas.web.outsys.api.uidata.UICData;
import inetbas.web.outsys.api.uidata.UIRecord;
import inetbas.web.outsys.entity.DempInfo;
import inetbas.web.outsys.entity.Menu;
import inetbas.web.outsys.entity.MenuParams;
import inetbas.web.outsys.entity.PageLayOut;
import inetbas.web.outsys.entity.QueryEntity;
import inetbas.web.outsys.entity.ReturnObj;
import inetbas.web.outsys.entity.UserInfo;
import inetbas.web.outsys.entity.WebCEAPars;
import inetbas.web.outsys.tools.APIUtil;
import inetbas.web.outsys.tools.CellsSessionUtil;
import inetbas.web.outsys.tools.CellsUtil;
import inetbas.web.outsys.tools.EDCodeUtil;
import inetbas.web.outsys.tools.MenuUtil;
import inetbas.web.outsys.tools.UserInfoSession;
import inetbas.web.outsys.uiparam.LayCell;
import inetbas.web.outsys.uiparam.LayCells;
import inetbas.web.webpage.ITLoginMang;
import inetbas.webserv.SErvVars;
import inetbas.webserv.SKeyMang;
import inetbas.webserv.WACODEOBJ;
import inetbas.webserv.WebAppPara;
import inetbas.webserv.WebAppParaEx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cl.ICL;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @author www.bip-soft.com
 * 
 */

public class WebServiceAPI extends HttpServlet {
	private static Logger _log = LoggerFactory.getLogger(WebServiceAPI.class);
	private WAToolkit _app = WAToolkit.getInstance();
	public static final String APIIV = "inetbas.web.outsys.api.WebApiInvoke";
	public static final String APIPera = "inetbas.web.outsys.api.WebOperationInvoke";//凭证
	public static final String APIWork = "inetbas.web.outsys.api.WebApiWorkInvoke";//审批
	public static final String APIRQ = "inetbas.web.outsys.api.WebRabbitMQInvoke";//RabbitMQ
	public static final String APIAID2 = "inetbas.web.outsys.api.WebApiAidInvoke2";//辅助、常量、自定义sql、变量查询服务
	public static final String APIWorkFlow = "inetbas.web.outsys.api.WebApiWorkFlowInvoke";//工作流
	public static final String APIPage = "inetbas.web.outsys.api.WebApiPageInvoke";//工作流
	public static final String APIRPT = "inetbas.web.outsys.api.WebApiRptInvoke";//RPT
	public static final String UTF8 = "utf-8";
	public static final String BIKey="sitande@2017";
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.setCharacterEncoding(UTF8);
		response.setCharacterEncoding(UTF8);
		String apiStr = request.getParameter(APIConst.APIID);
		ReturnObj error = new ReturnObj();
		error.makeFaile();
		if(apiStr==null){
			WriteJsonString(response, error);
			return ;
		}
		try{
			if (APIConst.APIID_LOGIN.equals(apiStr)) {// WEB端登录，需要用户名，密码 
				login(request, response); 
			}else if (APIConst.APIID_LOGOUT.equals(apiStr)){// WEB端退出
				exit(request, response);
			}else if (APIConst.APIID_OUTLOGIN.equals(apiStr)) {//WEB端登录，需要用户名
				loginWithOutPwd(request, response);
			}else if (APIConst.APIID_SINGLELOGIN.equals(apiStr)) {//WEB端登录，需要用秘钥
				signIn(request, response);
			}else if (APIConst.APIID_MPARAMS.equals(apiStr)){//获取菜单参数
//				procdbf(request, response);
				getMenuParams(request, response);
			}else if (APIConst.APIID_CELLPARAMS.equals(apiStr)) {//获取cell元素  参数：dbid,usercode,pcell
				getMenuCells(request, response); 
			}else if(APIConst.APIID_OPERATION.equals(apiStr)){//获取业务定义
				getOperationInfo(request,response);
			}else if (APIConst.APIID_SAVEDATA.equals(apiStr)){//保存或者删除数据
				saveData(request,response);
			}else if (APIConst.APIID_FINDDATA.equals(apiStr)){//查询对象数据
				findValues(request, response);
			} else if(APIConst.APIID_CHKUP.equals(apiStr)) {
				try {
					checkAndSubmit(request,response);
				} catch (Exception e) {
					_log.error("",e);
					error.setMessage(e.getMessage());
					WriteJsonString(response, error);
				}
			} else if(APIConst.APIID_WORKFLOW.equals(apiStr)) {
				getWorkFlow(request, response);
			}else if (APIConst.APIID_FINDSTATDATA.equals(apiStr)){//报表页面进行图表统计
			    findStatData(request, response);
		  } else if(APIConst.APIID_BIPINSAID.equals(apiStr)) {
			  getBipInsAidInfo(request, response);
		  }else if(APIConst.APIID_TA_MSG.equals(apiStr)){//任务消息操作
			  taskAndIM(request, response);
		  }else if(APIConst.APIID_DLGSQLRUN.equals(apiStr)){//自定义按钮执行SQL
			  dlgSqlRun(request, response);
		  }else if(APIConst.APIID_DLGCELLRUN.equals(apiStr)){//自定义按钮对象保存
			  
		  }else if(APIConst.APIID_RPT.equals(apiStr)) {
			  rptInfo(request, response);
		  }
		else{
			error.setMessage("错误的请求："+apiStr);
			WriteJsonString(response, error);
		}
		}catch (Exception e){
			_log.error("",e);
			WriteJsonString(response, error);
		}  
	} 
	
	/**
	 * @param request
	 * @param response
	 * 2019-07-25 17:33:31
	 */
	private void rptInfo(HttpServletRequest request, HttpServletResponse response) {
		String dbid = request.getParameter("dbid"), userCode = request
				.getParameter("usercode"),id = request.getParameter("id");
		HttpSession hss = request.getSession();
		HashMap<String, Object> mp = APIUtil.getdbuser(dbid, userCode);
		APIUtil.cpTOHttpSession(mp, hss);
		ReturnObj reoReturnObj = new ReturnObj();
		int rid = CCliTool.objToInt(id, 200);
		Object o0 = null;
		if(!checkLogin(response, hss, reoReturnObj))
			return ;
		try {
			String qeString = decode(request.getParameter("qe"));
			_log.info(qeString);
			QueryEntity qe = JSON.parseObject(qeString, QueryEntity.class);
			o0 =  universaInvoke(rid, APIRPT, null, new Object[]{qe}, false, hss);
			HashMap<String, Object> mp1 = new HashMap<String, Object>();
			mp1.put("rpt", o0);
			reoReturnObj.setData(mp1);
			WriteJsonString(response, o0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * WEB端登录，需要用户名，密码
	 * @param request 参数有dbid(账套编码),usercode(用户编码),username(用户名称)
	 * @param res
	 */
	private void login(HttpServletRequest request, HttpServletResponse res) {
		String ioutsys = "1";
		ioutsys = request.getParameter("ioutsys");
		ioutsys = ioutsys == null?"1":ioutsys;
		int cf = CCliTool.objToInt(ioutsys, 0)-1<0?0:CCliTool.objToInt(ioutsys, 0)-1;
		int aa = (int) Math.pow(2,cf);
		String ioutsysStr = "ioutsys&"+aa+"="+aa;
		//检查系统是否注册 
		_log.info("进入login方法");
		ReturnObj ret = new ReturnObj();
		String dbid="",userCode="";
		try {
			dbid = request.getParameter("dbid");
			userCode = request.getParameter("usercode");
			WebAppPara _wa = new WebAppPara();
			_wa.oprid = 0; 
			_wa.procName = "inetbas.serv.fzj.SRegServ"; 
			_wa.params = new Object[] {"isReg"};
			_wa.usetran = false;
			_wa.db_id = dbid;
			Object[] obj = (Object[]) _app.universalInvoke(_wa);
			if(CCliTool.objToInt(obj[0],2) == 2){
				ret.setId(2);
				ret.setMessage(obj[1].toString());
				WriteJsonString(res, ret);
				return ;
			}
			
			userCode = decode(userCode);
			boolean islog = checkISLogin(dbid, userCode);
			if(!islog){
				ret.setId(2);
				ret.setMessage("账号:"+userCode+"已经登陆，请切换账号！");
				WriteJsonString(res, ret);
				return ;
			}
//			userCode = decode(userCode);
			String pwd = request.getParameter("pwd");
			String lang = request.getParameter("lang");
			if(!CCliTool.isNull(pwd, true))
				pwd = EDCodeUtil.decodeData(pwd);
//			_log.info("密码："+pwd);
			pwd = SKeyMang.toTran(dbid, pwd, true);// ;-转化成密文方式
//			_log.info("加密密码："+pwd);
			String saddr = request.getRemoteAddr();
			_log.info("dbid:"+dbid+";"+"usercode"+userCode+";pwd"+pwd);
			 WebAppParaEx wa = new WebAppParaEx(dbid);
			 wa.oprid = cl.ICL.RQ_LOGIN;//登录id
			 wa.procName = cl.CLPF.MenuServ;//登录实体类
			 wa.params =  new Object[] { userCode, pwd,lang,saddr, null };
			 HashMap<String,Object> hm = APIUtil.getdbuser(dbid, userCode);
			 if(hm==null){
				 _log.info("login_b,:",hm);
			 }
			 _log.info("login_b");
			 APIUtil.login_b(wa, hm, request.getParameter(cl.ICL.applogin), dbid, saddr);//;--不需要服务登陆接口
			 _log.info("login_a");
			 APIUtil.login_a(_app.universalInvoke(wa), hm);
			 _log.info("login_a,完");
			 hm.put(cl.ICL.LOGINYN, 0);
			 hm.put(cl.ICL.MNUCHK, ioutsysStr);
			 APIUtil.cpTOHttpSession(hm, request.getSession());//;--登陆成功后信息同步。
//			 _log.info("SSSS",hm);
			 String key = SKeyMang.toTran(dbid, dbid+"_"+hm.get(cl.ICL.USRCODE), true);
			 UserInfoSession.cacheCells(key, hm);
			 wa.hssv = new WACODEOBJ();
			 wa.copy(hm, new Object[]{new Integer(1),hm.get(cl.ICL.MNUCHK) });
			 wa.oprid = cl.CLPF.MS_CCMENU;
			 HVector vmnu = (HVector) _app.universalInvoke(wa);
			 ArrayList<Menu> mlist = MenuUtil.makeTreeMenu(vmnu);
			 ret.makeSuccess();
			 Map<String, Object> resu = new HashMap<String, Object>();
			 resu.put("menulist", mlist);
			 UserInfo userInfo=makeUserInfo(hm);
			 resu.put("user", userInfo);
			 resu.put("snkey", key);
			 ret.setData(resu);
		} catch (Exception e) {
			String errString = CCliTool.traceToString(e);
			_log.info(errString);
			if(errString.indexOf("ERRUSRPAS")>0)
				errString = "用户名或密码错误";
			if(errString.indexOf("Communications")>=0)
				errString = "数据库连接失败";
			ret.makeFaile(errString);
		}finally{
			_log.info("loginfinally");
			WriteJsonString(res, ret);
		}
	}
	
	/***
	 * 用户退出，清空系统用户登录信息Map
	 * @param request 参数有dbid(),usercode(用户编码),username(用户名称),snkey(系统生成的key值)
	 * @param response
	 */
	public static void exit(HttpServletRequest request, HttpServletResponse response){
		ReturnObj ret = new ReturnObj();
		String dbid = request.getParameter("dbid");
		String userCode = request.getParameter("usercode");
		String userName = request.getParameter("username");
		String sn = request.getParameter("snkey");
		if(sn.length()>0){
			HashMap<String, Object> inf = UserInfoSession.getUserInfo(sn);
			dbid = CCliTool.objToString(inf.get(cl.ICL.DB_ID_T));
			userCode = CCliTool.objToString(inf.get(cl.ICL.USRCODE));
			userName = CCliTool.objToString(inf.get(cl.ICL.USRNAME));
		}else {
			dbid = dbid == null?"":dbid;
			userCode = userCode==null?"":userCode;
			userName = userName==null?"":decode(userName);
		}
		if(dbid.length()==0 || userCode.length()==0){
			ret.makeFaile("登出参数错误");
		}else{
			APIUtil.exit(dbid, userCode);
			APIUtil.exit(dbid, userName);
			if(sn.length()>0){
				UserInfoSession.exit(sn);
			}
			ret.makeSuccess("登出成功！");
		}
		WriteJsonString(response, ret);
	}
	
	/***
	 * 登录接口，只有数据库连接地址和账户code,不验证密码是否正确
	 *
	 * @param request
	 * @param res
	 */
	private void loginWithOutPwd(HttpServletRequest request,
	HttpServletResponse res) {
		ReturnObj ret = new ReturnObj();
		try {
			String ioutsys = null;
			ioutsys = request.getParameter("ioutsys");
			ioutsys = ioutsys == null?"1":ioutsys;
			//钉钉信息
			String ding = request.getParameter("ding");
			String code = request.getParameter("code");
			String corpId = request.getParameter("corpId");
			
			String dbid = request.getParameter("dbid"); 
			String userCode = request.getParameter("usercode");
			String saddr = request.getRemoteAddr();
			//验证注册信息
			WebAppPara _wa = new WebAppPara();
			_wa.oprid = 0; 
			_wa.procName = "inetbas.serv.fzj.SRegServ"; 
			_wa.params = new Object[] {"isReg"};
			_wa.usetran = false;
			_wa.db_id = dbid;
			Object[] obj = (Object[]) _app.universalInvoke(_wa);
			if(CCliTool.objToInt(obj[0],2) == 2){
				ret.setId(2);
				ret.setMessage(obj[1].toString());
				WriteJsonString(res, ret);
				return ;
			}
			//钉钉登录  从钉钉获取userid
			if(ding!=null){
				_wa.oprid = 200; 
				_wa.procName = "inetbas.web.webpage.ddserv.DingWorkMang"; 
				_wa.params = new Object[] {code,corpId};
				_wa.usetran = false;
				_wa.db_id = dbid;
				Object u = _app.universalInvoke(_wa);
				userCode = CCliTool.objToString(u);
			}
			int cf = CCliTool.objToInt(ioutsys, 0)-1<0?0:CCliTool.objToInt(ioutsys, 0)-1;
			int aa = (int) Math.pow(2,cf); 
			String ioutsysStr = "ioutsys&"+aa+"="+aa; //拼接获取菜单的条件  web,移动，钉钉
			WebAppParaEx wa = new WebAppParaEx(dbid);
			wa.oprid = 36;// 登录id
			wa.procName = cl.CLPF.MenuServ;// 登录实体类
			wa.params = new Object[] { userCode, null, saddr, null, null };
			HashMap<String, Object> hm = APIUtil.getdbuser(dbid, userCode);
			APIUtil.login_b(wa, hm, request.getParameter(cl.ICL.applogin),dbid, saddr);// ;--不需要服务登陆接口
			APIUtil.login_a(_app.universalInvoke(wa), hm);
			hm.put(cl.ICL.LOGINYN, 0); 
			hm.put(cl.ICL.MNUCHK, ioutsysStr);
			APIUtil.cpTOHttpSession(hm, request.getSession());// ;--登陆成功后信息同步。
			String key = SKeyMang.toTran(dbid, dbid+"_"+hm.get(cl.ICL.USRCODE), true);
		 	UserInfoSession.cacheCells(key, hm);
		 	ret.makeSuccess();
		 	Map<String, Object> resu = new HashMap<String, Object>();
		 	UserInfo userInfo = makeUserInfo(hm);
		 	APIUtil.cpTOHttpSession(hm, request.getSession());//;--登陆成功后信息同步。
		 	wa.hssv = new WACODEOBJ();
		 	wa.copy(hm, new Object[]{new Integer(1),hm.get(cl.ICL.MNUCHK) });
		 	wa.oprid = cl.CLPF.MS_CCMENU;
		 	HVector vmnu = (HVector) _app.universalInvoke(wa);
		 	ArrayList<Menu> mlist = MenuUtil.makeTreeMenu(vmnu);
		 	resu.put("menulist", mlist);
		 	resu.put("user", userInfo);
		 	resu.put("snkey", key);
		 	ret.setData(resu);
		} catch (Exception e) {
			String errString = CCliTool.traceToString(e);
			if (errString.indexOf("ERRUSRPAS") > 0)
				errString = "用户名或密码错误";
			if (errString.indexOf("Communications") >= 0)
				errString = "数据库连接失败";
			ret.makeFaile(errString);
		} finally {
			WriteJsonString(res, ret);
		}
	}

	/***
	 * 单点登录 用户名，数据库标识，秘钥
	 *
	 * @param request
	 * @param res
	 */
	private void signIn(HttpServletRequest request,HttpServletResponse res) {
		ReturnObj ret = new ReturnObj();
		try {
			String dbid = request.getParameter("dbid");
			String userCode = request.getParameter("usercode");
			WebAppPara _wa = new WebAppPara();
			_wa.oprid = 0; 
			_wa.procName = "inetbas.serv.fzj.SRegServ"; 
			_wa.params = new Object[] {"isReg"};
			_wa.usetran = false;
			_wa.db_id = dbid;
			Object[] obj = (Object[]) _app.universalInvoke(_wa);
			if(CCliTool.objToInt(obj[0],2) == 2){
				ret.setId(2);
				ret.setMessage(obj[1].toString());
				WriteJsonString(res, ret);
				return ;
			}
			String ioutsys = "1";
			ioutsys = request.getParameter("ioutsys");
			ioutsys = ioutsys == null?"1":ioutsys; 
			String curr = request.getParameter("timestamp");
//			long curr1 = System.currentTimeMillis();// 当前时间
//			if(curr1 - Long.valueOf(curr).longValue() >6000L){
//				ret.makeFaile("认证失败");
//				return;
//			}
			//秘钥
			String secret = request.getParameter("secret"); 
			HttpSession hss = request.getSession();   
			HashMap<String, Object> mp = APIUtil.getdbuser(dbid, userCode);
			APIUtil.cpTOHttpSession(mp, hss); 
			String varid = "BWBLOGKEY";
			String key0 = null;//CMain.getGBVar("D.DBOAKEY");
			Object o0 = universaInvoke(WebApiAidInvoke2.AID_CL, APIAID2, null, new Object[] {varid}, false, hss);
			key0 = CCliTool.objToString(o0);
			if(key0==null)
				key0 =  BIKey;
			String tokenid=curr+key0;//当前时间+秘钥
			tokenid = CCliTool.objToString( CBasTool.sha_md5(tokenid.getBytes(), null, true));//md5加密
			if(!tokenid.equals(secret)){
				ret.makeFaile("认证失败");
				return;
			}
			
			String saddr = request.getRemoteAddr();
			WebAppParaEx wa = new WebAppParaEx(dbid);
			wa.oprid = 36;// 登录id
			wa.procName = cl.CLPF.MenuServ;// 登录实体类
			wa.params = new Object[] { userCode, null, saddr, null, null };
			HashMap<String, Object> hm = APIUtil.getdbuser(dbid, userCode);
			APIUtil.login_b(wa, hm, request.getParameter(cl.ICL.applogin),
					dbid, saddr);// ;--不需要服务登陆接口
			 APIUtil.login_a(_app.universalInvoke(wa), hm);
			 hm.put(cl.ICL.LOGINYN, 0);
			 hm.put(cl.ICL.MNUCHK, "ioutsys="+ioutsys);
			 APIUtil.cpTOHttpSession(hm, request.getSession());// ;--登陆成功后信息同步。
			 String key = SKeyMang.toTran(dbid, dbid+"_"+hm.get(cl.ICL.USRCODE), true);
			 UserInfoSession.cacheCells(key, hm);
			 ret.makeSuccess();
			 Map<String, Object> resu = new HashMap<String, Object>();
			 UserInfo userInfo = makeUserInfo(hm);
			 hm.put(cl.ICL.LOGINYN, 0);
			 hm.put(cl.ICL.MNUCHK, "ioutsys="+ioutsys);
			 APIUtil.cpTOHttpSession(hm, request.getSession());//;--登陆成功后信息同步。
			 wa.hssv = new WACODEOBJ();
			 wa.copy(hm, new Object[]{new Integer(1),hm.get(cl.ICL.MNUCHK) });
			 wa.oprid = cl.CLPF.MS_CCMENU;
			 HVector vmnu = (HVector) _app.universalInvoke(wa);
			 ArrayList<Menu> mlist = MenuUtil.makeTreeMenu(vmnu);
			 resu.put("menulist", mlist);
			 resu.put("user", userInfo);
			 resu.put("snkey", key);
			 ret.setData(resu);
		} catch (Exception e) {
			String errString = CCliTool.traceToString(e);
			if (errString.indexOf("ERRUSRPAS") > 0)
				errString = "用户名或密码错误";
			if (errString.indexOf("Communications") >= 0)
				errString = "数据库连接失败";
			ret.makeFaile(errString);
		} finally {
			WriteJsonString(res, ret);

		}
	}

	/***
	 * 获取菜单参数 并获取数据 暂时未用到
	 * @param request
	 * request中的参数：pbuid,dbid,usercode,pmenuid
	 * @param response
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void procdbf(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String buid = request.getParameter(cl.ICL.pbuid), dbid = request
				.getParameter("dbid"), userCode = request
				.getParameter("usercode");
		String sfld = cl.ICL.DBFILED, s0 = "select " + sfld + ",pbds";
		HttpSession hss = request.getSession();
		HashMap<String, Object> mp = APIUtil.getdbuser(dbid, userCode);
		APIUtil.cpTOHttpSession(mp, hss);
		ReturnObj reoReturnObj = new ReturnObj();
		if (!checkLogin(response, hss, reoReturnObj))
			return;
		String sex = (String) hss.getAttribute(cl.ICL.WA_LANG);
		boolean bex = sex != null && sex.length() > 0;
		if (bex) {
			sex = cl.ICL.playout + sex;
			s0 += "," + sex;
		}
		s0 += " from insurl where pbuid='" + buid + "'";
		int ico = CPubTool.to_co((Number) hss.getAttribute(cl.ICL.CORPCODE));
		if (ico > 0)
			s0 += " and (" + cl.ICL.F_CORP + "<1 or " + cl.ICL.F_CORP + "="
					+ (ico & 0xFFFF) + ")";
		Object os0[] = (Object[]) checkMenu(request, s0, hss), o1 = os0[1];
		if (o1 == null)
			throw new RuntimeException("insurl: " + buid);
		Hashtable hts = CPubTool.toParas(sfld, (Object[]) o1);
		if (bex) {
			sex = (String) hts.get(sex);// 优先采用外语布局。
			if (sex != null && sex.length() > 0)
				hts.put(cl.ICL.playout, sex);
		}
		readParas(request, response, hts, true);
		Object otr = os0[0];
		if (otr != null)
			checkAttr(hts, otr);
		s0 = getParameter(cl.ICL.plabel, request);
		String clazz = CCliTool.objToString(hts.get(cl.ICL.pclass));
		clazz = clazz == null ? "" : clazz;
		boolean isbill = true;
		if (clazz.length() > 0) {
			if (!clazz.equals("inetbas.cli.cenv.CBaseApplet")) {
				isbill = false;
			}
		}
		if (s0 != null && s0.length() > 0)
			hts.put(cl.ICL.plabel, s0);
		Object cells = universaInvoke(34, cl.CLPF.SUnivServ, null, new Object[] { hts.get(cl.ICL.pcell) }, false, hss);
		Cells c1 = null, tjcell = null;
		if (cells instanceof Cells) {
			c1 = (Cells) cells;
			c1.init();
			CellsUtil.initCells(((Cells) cells));
		} else {
			Cells[] cells2 = (Cells[]) cells;
			CellsUtil.initCells(cells2);
			for (int i = 0; i < cells2.length; i++) {
				Cells ci = cells2[i];
				if ((ci.attr & ICL.ocCondiction) > 0 && !isbill) {
					tjcell = ci;
				}
				if ((ci.attr & ICL.ocCondiction) == 0) {
					c1 = ci;
					break;
				}
			}
		}
		int currPage = CCliTool
				.objToInt(request.getParameter("currentPage"), 0);
		_log.info("currentPage:" + currPage);
		int pageSize = CCliTool.objToInt(request.getParameter("pageSize"), 20);
		_log.info("pageSize:" + pageSize);
		String cellid = request.getParameter("cellid");
		cellid = cellid == null ? "" : cellid;
		if (cellid.length() > 0) {
			c1 = c1.getChild(cellid, false);
		}
		PageLayOut pageLayOut = new PageLayOut(currPage, pageSize,
				(String)hts.get(cl.ICL.pdata));
		LayCells layCells = new LayCells(c1);
		makeValues(hss, c1, pageLayOut);
		reoReturnObj.makeSuccess();
		Map retMap = new HashMap<String, Object>();
		if (!isbill) {
			LayCells contLayCel = new LayCells(tjcell);
			retMap.put("contCel", contLayCel);
		}
		if (cellid.length() == 0) {
			retMap.put("menuparam", hts);
			retMap.put("layCels", layCells);
			retMap.put("beBill", isbill);
		}
		retMap.put("pages", pageLayOut);
		reoReturnObj.setData(retMap);
		WriteJsonString(response, reoReturnObj);
	}
	/***
	 * 获取菜单参数 不获取数据
	 * @param request
	 * request中的参数：pbuid,dbid,usercode,pmenuid
	 * @param response
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void  getMenuParams(HttpServletRequest request,HttpServletResponse response) throws Exception {
		String buid = request.getParameter(cl.ICL.pbuid), dbid = request
				.getParameter("dbid"), userCode = request
				.getParameter("usercode");
		String sfld = cl.ICL.DBFILED + ",pbds", s0 = "select " + sfld;
		HttpSession hss = request.getSession();
		HashMap<String, Object> mp = APIUtil.getdbuser(dbid, userCode);
		APIUtil.cpTOHttpSession(mp, hss);
		ReturnObj reoReturnObj = new ReturnObj();
		if(!checkLogin(response, hss, reoReturnObj)){
			return ;
		}
		String sex = (String) hss.getAttribute(cl.ICL.WA_LANG);
		boolean bex = sex != null && sex.length() > 0;
		if (bex) {
			sex = cl.ICL.playout + sex;
			s0 += "," + sex;
		}
		s0 += " from insurl where pbuid='" + buid + "'";
		int ico = CPubTool.to_co((Number) hss.getAttribute(cl.ICL.CORPCODE));
		if (ico > 0)
			s0 += " and (" + cl.ICL.F_CORP + "<1 or " + cl.ICL.F_CORP + "="
					+ (ico & 0xFFFF) + ")";
		Object os0[] = (Object[]) checkMenu(request, s0, hss), o1 = os0[1];
		if (o1 == null)
			throw new RuntimeException("insurl: " + buid);
		Hashtable<String,String> hts = CPubTool.toParas(sfld, (Object[])o1);
		if (bex) {
			sex = (String) hts.get(sex);// 优先采用外语布局。
			if (sex != null && sex.length() > 0)
				hts.put(cl.ICL.playout, sex);
		}
		readParas(request, response, hts, true);
		Object otr = os0[0];
		if (otr != null)
			checkAttr(hts, otr);
		s0 = getParameter(cl.ICL.plabel, request);
		String clazz = CCliTool.objToString(hts.get(cl.ICL.pclass));
		clazz = clazz==null?"inetbas.cli.cenv.CBaseApplet":clazz;
		boolean isbill = true;
		if(clazz.length()>0){
			if(!clazz.equals("inetbas.cli.cenv.CBaseApplet")){
				isbill = false;
			}
		}
		hts.put(ICL.pclass, clazz);
		if (s0 != null && s0.length() > 0)
			hts.put(cl.ICL.plabel, s0);
		MenuParams mparam = new MenuParams(); 
		mparam.setBeBill(isbill);
		mparam.initParams(hts);
		mparam.setBeBill(isbill);
		mparam.setPbuid(buid);
		reoReturnObj.makeSuccess();
		HashMap<String, Object> retMap = new HashMap<String, Object>();
		Object[] obj = null;
		
		if(!mparam.isBeBill()&&buid!=null){
			Object o0 = makeCountFLD(hss, buid);
			if(o0!=null){
				//grplist,sumArrayList,chartType,orderby,width
				Object[] o11 = (Object[]) o0;
				obj = new Object[o11.length];
				for (int i = 0; i < o11.length; i++) {
					MenuParams mparam0 = new MenuParams(); 
					mparam0.initParams(hts);
					mparam0.setBeBill(isbill);
					mparam0.setPbuid(buid);
					Object[] oo1 = (Object[]) o11[i]; 
					ArrayList<String> groupfilds = (ArrayList<String>) oo1[0];
					ArrayList<String> sumfilds = (ArrayList<String>) oo1[1];
					String ctype = CCliTool.objToString(oo1[2]); 
					String width = CCliTool.objToString(oo1[3]);
					mparam0.setBgroup(true);
					mparam0.setCtype(ctype);
					mparam0.setGroupfilds(groupfilds);
					mparam0.setSumfilds(sumfilds);
					mparam0.setWidth(width); 
					obj[i]=mparam0;
				} 
			}
		}
		if(obj == null)
			retMap.put("mparams", mparam);
		else
			retMap.put("mparams", obj);
		reoReturnObj.setData(retMap);
		WriteJsonString(response, reoReturnObj);
	}
	
	/***
	 * 获取cell元素
	 * @param request
	 * request 中参数：dbid,usercode,pcell
	 * @param response
	 * @throws Exception
	 */
	public void  getMenuCells(HttpServletRequest request,HttpServletResponse response) throws Exception {
		String dbid = request.getParameter("dbid"), 
			   userCode = request.getParameter("usercode");
		String pcell = request.getParameter(cl.ICL.pcell);
		HttpSession hss = request.getSession();
		HashMap<String, Object> mp = APIUtil.getdbuser(dbid, userCode);
		APIUtil.cpTOHttpSession(mp, hss);
		ReturnObj reoReturnObj = new ReturnObj();
		if(!checkLogin(response, hss, reoReturnObj))
			return ;
		Object cells = getCellsBypcell(pcell, hss);
		Map<String, Object> retMap = new HashMap<String, Object>();
		Cells c1 = null;
		ArrayList<LayCells> retcels = new ArrayList<LayCells>();
		if (cells instanceof Cells) {
			c1 = (Cells) cells;
			c1.init();
			CellsUtil.initCells(((Cells) cells));
			c1 = (Cells)universaInvoke(WebApiInvoke.API_InitCelInc, APIIV, null, new Object[]{cells}, false, hss);
			CellsSessionUtil.cacheCells(dbid, pcell, c1);
			LayCells layCells = new LayCells(c1);
//			layCells = getAssistType(layCells, hss);
			retcels.add(layCells);
		} else {
			Cells[] cells2 = (Cells[]) cells;
			CellsUtil.initCells(cells2);
			for (int i = 0; i < cells2.length; i++) {
				Cells ci = cells2[i];
				LayCells cc = new LayCells(ci);
				cc = getAssistType(cc, hss);
				retcels.add(cc);
			}
			CellsSessionUtil.cacheCells(dbid, pcell, cells2);
		}

		retMap.put("layCels", retcels);
		reoReturnObj.makeSuccess();
		reoReturnObj.setData(retMap);
		WriteJsonString(response, reoReturnObj);
	}
	
	
	/**
	 * 获取业务定义
	 * @param request
	 * @param response
	 * @throws Exception 
	 */
	private void getOperationInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String dbid = request.getParameter("dbid"), userCode = request
				.getParameter("usercode"),sbuid = request.getParameter("buid");
		HttpSession hss = request.getSession();
		HashMap<String, Object> mp = APIUtil.getdbuser(dbid, userCode);
		APIUtil.cpTOHttpSession(mp, hss);
		ReturnObj reoReturnObj = new ReturnObj();
		if(!checkLogin(response, hss, reoReturnObj))
			return ;
		Object o0 =  universaInvoke(WebOperationInvoke.OPERATIONID, APIPera, null, new Object[]{sbuid }, false, hss);
		if(o0!=null){
			Map<String, Object> opt = new HashMap<String, Object>();
			opt.put("opt", o0);
			reoReturnObj.setData(opt);
			reoReturnObj.makeSuccess();
		}else{
			reoReturnObj.makeFaile();
			reoReturnObj.setMessage("没有该业务");
		}
		WriteJsonString(response, reoReturnObj);
	}
	
	/**
	 * 获取业务定义
	 * @param request
	 * @param response
	 * @throws Exception 
	 */
	private void getBipInsAidInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String dbid = request.getParameter("dbid"), userCode = request
				.getParameter("usercode"),id = request.getParameter("id"),aid = request.getParameter("aid");
		HttpSession hss = request.getSession();
		HashMap<String, Object> mp = APIUtil.getdbuser(dbid, userCode);
		APIUtil.cpTOHttpSession(mp, hss);
		ReturnObj reoReturnObj = new ReturnObj();
		int rid = CCliTool.objToInt(id, 200);
		Object o0 = null;
		if(!checkLogin(response, hss, reoReturnObj))
			return ;
		if(rid==200||rid==300) {
			o0 =  universaInvoke(rid, APIAID2, null, new Object[]{aid }, false, hss);
		}else if(rid==210) {
			String qeString = decode(request.getParameter("qe"));
			_log.info(qeString);
			QueryEntity qe = JSON.parseObject(qeString, QueryEntity.class);
			o0 =  universaInvoke(rid, APIAID2, null, new Object[]{aid,qe}, false, hss);
		}else if(rid==400) {
			o0 =  universaInvoke(rid, APIAID2, null, new Object[]{aid }, false, hss);
		}
		if(o0!=null){
			Map<String, Object> opt = new HashMap<String, Object>();
			opt.put("data", o0);
			reoReturnObj.setData(opt);
			reoReturnObj.makeSuccess();
		}else{
			reoReturnObj.makeFaile();
			reoReturnObj.setMessage(rid==200?("没有辅助:"+aid):"没有数据");
		}
		WriteJsonString(response, reoReturnObj);
	}
	
	
	/***
	 * 保存、删除数据
	 * UIRecord
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private void saveData(HttpServletRequest request,HttpServletResponse response) throws Exception{
		String dbid = request
				.getParameter("dbid"), userCode = request
				.getParameter("usercode"),pcell = request.getParameter(cl.ICL.pcell);		
		int dataType = CCliTool.objToInt(request.getParameter("datatype"), 0); 
		HttpSession hss = request.getSession();
		HashMap<String, Object> mp = APIUtil.getdbuser(dbid, userCode);
		APIUtil.cpTOHttpSession(mp, hss);
		ReturnObj reoReturnObj = new ReturnObj();
		if(!checkLogin(response, hss, reoReturnObj))
			return ;
		Cells cells = (Cells) getCellsBypcell(pcell, hss);
		CellsUtil.initCells(cells);
		cells = (Cells)universaInvoke(WebApiInvoke.API_InitCelInc, APIIV, null, new Object[]{cells}, false, hss);
		CRecord cr = new CRecord(0);
		if (dataType == APIConst.CELLDATA) {
			//celldata格式提交数据
		}else if (dataType == APIConst.JSONDATA) {
			String jdsString = request.getParameter("jsonstr");
			_log.info(jdsString);
			String jsonData = decode(jdsString);// request.getParameter(cl.ICL.pdata);
			//json格式提交数据
			_log.info(jsonData);
			cr = makeCRecordByJsonStr(cells,jsonData);
			if(cr.c_state == 4){
				cr = cr.delete(cells.pkIndexs(), cells.xs_BDNL, false);
			}
		}
		if((cr.c_state & CRecord.INSERT)==1)
			CCliTool.inc_Calc2(cells, cr.getValues(), -1);
		 CIDVAL o0 = (CIDVAL)universaInvoke(1, null, "SAVE", new Object[]{cells,cr}, true, hss);
		 Map<String, Object> redata = new HashMap<String, Object>();
		 if((cr.c_state&CRecord.INSERT)>0 ){
			 if(o0 == null){
//				 redata.put(pkid, ((CRecord)o0.value).getValue(0));
			 }else{
				 if(o0.value instanceof CRecord){
					 for (int i = 0; i < cells.pkIndexs().length; i++) {
						 String pkid = cells.all_cels[cells.pkIndexs()[i] ].ccName;
						 redata.put(pkid, ((CRecord)o0.value).getValue(i));
					}
				 }
			 }
		 }else if(cr.c_state == 4){
			 _log.info("delData");
		 }
		 reoReturnObj.makeSuccess();
		 reoReturnObj.setData(redata);
		 WriteJsonString(response, reoReturnObj);
	}

	private void getWorkFlow(HttpServletRequest request,HttpServletResponse response) {
		String id=request.getParameter("id");
		String dbid = request.getParameter("dbid");
		String userCode = request.getParameter("usercode");
		String buidto = request.getParameter("buidto");
		String buidfr = request.getParameter("buidfr");
		int keyId = CCliTool.objToInt(id, 200);
		HashMap<String, Object> mp = APIUtil.getdbuser(dbid, userCode);
		HttpSession hss = request.getSession();
		APIUtil.cpTOHttpSession(mp, hss);
		ReturnObj reoReturnObj = new ReturnObj();
		if(!checkLogin(response, hss, reoReturnObj)){
			return ;
		}
		try {
			Object[] paras = null;
			if(keyId==200) {
				paras = new Object[]{buidto,buidfr};
			}else if(keyId==205) {
				String q = request.getParameter("qe");
				QueryEntity qEntity = JSONObject.parseObject(q, QueryEntity.class);
				paras = new Object[]{buidto,buidfr,qEntity};
			}else if(keyId==210) {
				String q = request.getParameter("qe");
				QueryEntity qEntity = JSONObject.parseObject(q, QueryEntity.class);
				paras = new Object[]{buidto,buidfr,qEntity};
			}
			Object o0 =  universaInvoke(keyId, APIWorkFlow, null, paras, false, hss);
			if(o0==null) {
				reoReturnObj.makeFaile("not get workflow");
			}else {
				HashMap<String, Object> data = new HashMap<String, Object>();
				reoReturnObj.makeSuccess();
				if(keyId==200) {
					data.put("flowlist", o0);
				}else {
					data.put("info", o0);
				}			
				reoReturnObj.setData(data);
			}
			WriteJsonString(response, reoReturnObj);
		} catch (Exception e) {
			e.printStackTrace();
			reoReturnObj.makeFaile(CCliTool.traceException0(e));
			WriteJsonString(response, reoReturnObj);
		}
	}

	/**
	 * 审核提交数据处理方法
	 * @param request
	 * @param response
	 */
	private void checkAndSubmit(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		int checkUpId = CCliTool.objToInt(request
				.getParameter("chkid"), 33);
		String dbid = request
				.getParameter("dbid"), userCode = request
				.getParameter("usercode");
		String ceaStr = request.getParameter("cea");
		ceaStr = decode(ceaStr);
		WebCEAPars cea = JSONObject.parseObject(ceaStr,WebCEAPars.class);
		HttpSession hss = request.getSession();
		HashMap<String, Object> mp = APIUtil.getdbuser(dbid, userCode);
		APIUtil.cpTOHttpSession(mp, hss);
		ReturnObj reoReturnObj = new ReturnObj();
		if(!checkLogin(response, hss, reoReturnObj))
			return ;
		Object o0 =  universaInvoke(checkUpId, APIWork, null, new Object[]{cea}, false, hss);
		if(o0==null){
			reoReturnObj.makeFaile();
			reoReturnObj.setMessage("没有找到审批人;");
		}else{
			reoReturnObj.makeSuccess();
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("info", o0);
			reoReturnObj.setData(data);
		}
		WriteJsonString(response, reoReturnObj);

	}

	/**
	 * 组成CRecord数据，来源数据是JSON格式数据
	 * @param cells
	 * @param jsonData
	 * @return
	 */
	private CRecord makeCRecordByJsonStr(Cells cells, String jsonData) {
		UIRecord uir = JSON.parseObject(jsonData, UIRecord.class);
		CRecord cr = makeSysRecord(cells, uir);
		makeSubData(cells, cr, uir.getSubs());
		return cr;
	}
	/**
	 * 组成CRecord数据
	 * @param cells
	 * @param uir
	 * @return
	 */
	private CRecord makeSysRecord(Cells cells, UIRecord uir) {
		JSONObject cc = uir.getData();
		int state = uir.getC_state();
		CRecord cr = new CRecord(state);
		for (int i = 0; i < cells.db_cels.length; i++) {
			Cell c = cells.db_cels[i];
			if(c.ccType==3) {
				cr.setValue(CCliTool.objToDecimal(cc.get(c.ccName),false,BigDecimal.ZERO), i);
			}else
			cr.setValue(cc.get(c.ccName), i);
		}
		return cr;
	}
	/**
	 * 组成子表数据
	 * @param cells
	 * @param cr
	 * @param cc
	 */
	private void makeSubData(Cells cells, CRecord cr, List<UICData> cc) {
		for(int j=0;j<cc.size();j++){
			UICData uicd = cc.get(j);
			Cells subcell = cells.find(uicd.getObj_id());
			if(subcell != null){
				CData cData = new CData(subcell.obj_id);
				for(int i=0;i<uicd.getData().size();i++){
					UIRecord uir = uicd.getData().get(i);
					CRecord cr0 = makeSysRecord(subcell, uir);
					if(subcell.getChildCount() > 0){
						makeSubData(subcell, cr0, uir.getSubs());
					}
					cData.add(cr0, -1);
				}
				for(int i=0;i<uicd.getRmdata().size();i++){
					UIRecord uir = uicd.getRmdata().get(i);
					CRecord cr0 = makeSysRecord(subcell, uir);
					cData.add(cr0, -1);
					cData.remove(uicd.getData().size(),subcell.pkIndexs());
				}
				if(cData.size()>0){
					cr.addChild(cData);
				}
			}
		}
	}

//	private CData getChildDataBySubCell(Cells subcell,String childbb){
//		CData cData = new CData(subcell.obj_id);
//		JSONArray dd = JSON.parseArray(childbb);
//		HVector delhv = new HVector();
//		for(int j=0;j<dd.size();j++){
//			JSONObject subJsonO = dd.getJSONObject(j);
//			int state = CCliTool.objToInt(subJsonO.get("sys_stated"), 3);
//			CRecord crd = new CRecord(0);
//			for(int k=0;k<subcell.db_cels.length;k++){
//				Cell c0 = subcell.db_cels[k];
//				if(c0.ccType==3) {
//					crd.setValue(CCliTool.objToDecimal(subJsonO.get(c0.ccName),false,BigDecimal.ZERO), k);
//				}else
//				crd.setValue(subJsonO.get(c0.ccName), k);
//			}
//			makeSubData(subcell,crd,subJsonO);
//			crd.c_state = state;
//			if(crd.c_state == 4){
//				delhv.addElement(j);
//			}
//			cData.add(crd, -1);
//		}
//		for(int i=0;i<delhv.size();i++){
//			int line = CCliTool.objToInt(delhv.elementAt(i),-1);
//			if(line>0){
//				cData.remove(line, subcell.pkIndexs());
//			}
//		}
//		return cData;
//	}
	
	public void findValues(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String dbid = request
				.getParameter("dbid"), userCode = request
				.getParameter("usercode");
		String pcellcont = request.getParameter("qe");
		HttpSession hss = request.getSession();
		HashMap<String, Object> mp = APIUtil.getdbuser(dbid, userCode);
		APIUtil.cpTOHttpSession(mp, hss);
		ReturnObj reoReturnObj = new ReturnObj();
		if(!checkLogin(response, hss, reoReturnObj))
			return ;
		String jsonData = decode(pcellcont);
		QueryEntity queryEntity = JSONObject.parseObject(jsonData, QueryEntity.class);
		Object oo = universaInvoke(213, APIPage, null, new Object[]{queryEntity}, false, hss);
		ReturnObj rtn = new ReturnObj();
		if(oo!=null) {
			rtn.makeSuccess();
		}
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("data", oo);
		rtn.setData(map);
		WriteJsonString(response, rtn);
	}

	

	/**
	 * 统计获取数据
	 * @param request
	 * @param response
	 */
	private void findStatData(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String dbid = request
				.getParameter("dbid"), userCode = request
				.getParameter("usercode");
		String pcell = request.getParameter(cl.ICL.pcell);
		String pdata1 = request.getParameter(cl.ICL.pdata);
		int pageSize = CCliTool.objToInt(request.getParameter("pageSize"),20);
		int currentPage = CCliTool.objToInt(request.getParameter("currentPage"),1);
		  
		if(pdata1 == null || pdata1.equals("{}")){
			pdata1 = "";
		}
		String pdata =decode(pdata1);// request.getParameter(cl.ICL.pdata);
		String pgrpfld = request.getParameter("groupfilds");
		String pgrpdatafld = request.getParameter("groupdatafilds"); 
		String psearch = request.getParameter("psearch");		
		HttpSession hss = request.getSession();
		HashMap<String, Object> mp = APIUtil.getdbuser(dbid, userCode);
		APIUtil.cpTOHttpSession(mp, hss);
		ReturnObj reoReturnObj = new ReturnObj();
		if(!checkLogin(response, hss, reoReturnObj))
			return ;
		if(psearch!=null){
			Cells tjcells = (Cells) universaInvoke(34, cl.CLPF.SUnivServ, null, new Object[] { psearch }, false, hss);
			APIUtil.cpTOHttpSession(mp, hss);
			pdata = makeSearchTj(tjcells, pdata);
		}else{
			pdata = "";
		}
		Cells cells = (Cells) universaInvoke(34, cl.CLPF.SUnivServ, null, new Object[] { pcell }, false, hss);
		Cells countCell = (Cells)cells.clone(true);
		pgrpfld = pgrpfld.substring(1,pgrpfld.length()-1);
		pgrpdatafld = pgrpdatafld.substring(1,pgrpdatafld.length()-1);
		Object[] o0 = CellsUtil.makeCellsCell(countCell,pgrpfld,pgrpdatafld,cells);
		countCell = (Cells)o0[0];
		pgrpfld =  CCliTool.objToString(o0[1]);
		pgrpdatafld = CCliTool.objToString(o0[2]);
		PageLayOut pageLayOut = new PageLayOut(currentPage,pageSize,pdata); 
		LayCells layCells = new LayCells(countCell);
		Map<String,Object> retMap = new HashMap<String, Object>();
		makeCountValues(hss, countCell,pageLayOut,pgrpfld,pgrpdatafld);
		retMap.put("tjpages", pageLayOut);
		retMap.put("tjlayCels", layCells);
		reoReturnObj.setData(retMap);
		reoReturnObj.makeSuccess();
		WriteJsonString(response, reoReturnObj);
		
	}




	//获取辅助类型
	private LayCells getAssistType (LayCells retcels,HttpSession hss) throws Exception{
		LayCell[] cell = (LayCell[]) retcels.cels;
		for (int i = 0; i < cell.length; i++) {
			LayCell lCell = cell[i];
			if(lCell.assist){//是否是辅助
				String editName = lCell.editName;//辅助名称
				String[] str = (String[]) universaInvoke(WebApiInvoke.API_ASSISTTYPE, APIIV, null, new Object[]{editName}, false, hss);
				retcels.cels[i].assType = str[0];
				if(str[1] != null && retcels.cels[i].script !=null){
					retcels.cels[i].script += ";"+str[1];
				} else if(str[1] != null && retcels.cels[i].script ==null) {
					retcels.cels[i].script = str[1];
				}
			}
		}
		if(retcels.haveChild){
			for (int i = 0; i < retcels.subLayCells.length; i++) {
				retcels.subLayCells[i] = getAssistType(retcels.subLayCells[i], hss);
			}
		}
		return retcels;
	}
	
	@SuppressWarnings("unchecked")
	protected void getCellData(HttpServletRequest request,HttpServletResponse response) throws Exception {
		String dbid = request
				.getParameter("dbid"), userCode = request
				.getParameter("usercode");
		String pcell = request.getParameter(cl.ICL.pcell);
		String pdata1 = request.getParameter(cl.ICL.pdata);
		String allColumns =   request.getParameter("allColumnsLike");
		String celCondiction = request.getParameter("celCondiction");
		allColumns=allColumns==null?"":allColumns;
		allColumns = URLDecoder.decode(allColumns,"UTF-8");
		if(pdata1 == null || pdata1.equals("{}")){
			pdata1 = "";
		}
		String pdata =decode(pdata1);// request.getParameter(cl.ICL.pdata);
		String pbill = request.getParameter("bebill");
		int bid = CCliTool.objToInt(pbill, 1);
		boolean isbill = bid==1;
		HttpSession hss = request.getSession();
		HashMap<String, Object> mp = APIUtil.getdbuser(dbid, userCode);
		APIUtil.cpTOHttpSession(mp, hss);
		ReturnObj reoReturnObj = new ReturnObj();
		if(!checkLogin(response, hss, reoReturnObj))
			return ;
		Object cells = CellsSessionUtil.getCellsByCellId(dbid, pcell);
		
		Cells c1 = null,tjcell=null;
		if (cells instanceof Cells) {
			c1 = (Cells) cells;
			if(celCondiction != null && celCondiction.equals("false"))
				c1.condiction="";
		} else {
			Cells[] cells2 = (Cells[]) cells;
			for (int i = 0; i < cells2.length; i++) {
				Cells ci = cells2[i];
				if((ci.attr & ICL.ocCondiction) > 0 && !isbill){
					tjcell = ci;
				}
				if ((ci.attr & ICL.ocCondiction) == 0) {
					c1 = ci;
					break;
				}
			}
		}
		int currPage = CCliTool.objToInt(request.getParameter("currentPage"),0);
		_log.info("currentPage:"+currPage);
		int pageSize = CCliTool.objToInt(request.getParameter("pageSize"),20);
		_log.info("pageSize:"+pageSize);
		String cellid=request.getParameter("cellid");
		cellid = cellid==null ? "" : cellid;
		if(cellid.length()>0){
			if (cells instanceof Cells) {
				c1 = (Cells) cells;
				if(c1.obj_id != cellid&&cellid.length()>0)
					c1 = c1.getChild(cellid, false);
			}else{
				Cells[] cells2 = (Cells[]) cells;
				for(int i=0;i<cells2.length;i++){
					Cells ci = cells2[i];
					if(ci.obj_id.equals(cellid)){
						c1 = ci;
						break;
					}else {
						if(ci.getChildCount()>0) {
							c1 = c1.getChild(cellid, false);
						}
					}
				}
			}
		}
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(!isbill&&tjcell!=null){
			LayCells contLayCel = new LayCells(tjcell);
			retMap.put("contCel", contLayCel);
		}
//		LayCells layCells = new LayCells(c1);
		boolean isopenTjnow = false;
		String groupStr1 = "",sumfildstr = "";
		Cells cc = null;
		String pbuid = request.getParameter(cl.ICL.pbuid);
		if(pbuid!=null&&pbuid.length()>0){
			Object o0 = makeCountCell(hss,c1,pbuid);
			if(!CCliTool.isNull(o0, true)){
				Object[] oo1 = (Object[])o0;
				cc = (Cells) oo1[0];
				LayCells tjlayCels = new LayCells(cc);
				retMap.put("tjlayCels", tjlayCels);
				ArrayList<String> groupfilds = (ArrayList<String>) oo1[1];
				ArrayList<String> sumfilds = (ArrayList<String>) oo1[2];
				groupStr1 = CCliTool.objToString(oo1[4]);
				sumfildstr = CCliTool.objToString(oo1[5]);
				retMap.put("groupfilds", groupfilds);
				retMap.put("groupdatafilds", sumfilds);
				retMap.put("bcount", true);
				retMap.put("chartType", oo1[3]);
				isopenTjnow = true;
			}
		}else{
			retMap.put("bcount", false);
		}
//		if(cellid.length()==0){
//			retMap.put("layCels", layCells);
//		}
		if(pdata.length()>0 && tjcell == null){
			tjcell = c1;
		}
		pdata = makeSearchTj(tjcell,pdata);
		pdata= pdata==null?"":pdata;
		if(pdata.equals("")&&allColumns!=null&&!allColumns.equals("")){
			allColumns = allColumns.trim();
			Cells cel = null;			
			cel = (Cells) cells;
			Cell[] all_cels = cel.db_cels;
			for (int i = 0; i < all_cels.length; i++) {
//				if((all_cels[i].attr&Cell.HIDDEN)<=0){ 
					String str = all_cels[i].ccName;
					byte b = 0;
					SQLExecQuery eq = SQLConnection.getExecQuery(dbid, 0,b);
					int dateType = eq.db_type;
					eq.close();
					if(dateType == 4){
						if(all_cels[i].ccType==91){  
							str = "date_format("+str+", '%Y-%m-%d')";//MYSQL
						}
						if(all_cels[i].ccType==93){ 
							str = "date_format("+str+", '%Y-%m-%d %H:%i:%s')";//MYSQL
						}
					}else{
						if(all_cels[i].ccType==91){
							str = "Convert(VARCHAR,"+str+",23)";//SQLSERVER 
						}
						if(all_cels[i].ccType==93){
							str = "Convert(VARCHAR,"+str+",120)";//SQLSERVER
						}
					}
					if(i ==0){
						pdata += str+" like '%"+allColumns+"%' ";
					}else{
						pdata += " or "+str+" like '%"+allColumns+"%' ";
					}
//				}
			}
		}
		PageLayOut pageLayOut =  new PageLayOut(currPage,pageSize,pdata);
		if(!isopenTjnow){
			makeValues(hss, c1,pageLayOut);
			isopenTjnow = false;
		}else{
			makeCountValues(hss, cc, pageLayOut, groupStr1, sumfildstr);
		}
		retMap.put("pages", pageLayOut);
		reoReturnObj.makeSuccess();
		reoReturnObj.setData(retMap);
		WriteJsonString(response, reoReturnObj);  
	}

	protected void expData(HttpServletRequest request,HttpServletResponse response) throws Exception {
		String dbid = request
				.getParameter("dbid"), userCode = request
				.getParameter("usercode");
		String pcell = request.getParameter(cl.ICL.pcell);
		String pdata1 = request.getParameter(cl.ICL.pdata);
		if(pdata1 == null || pdata1.equals("{}")){
			pdata1 = "";
		}
		String pdata =decode(pdata1);// request.getParameter(cl.ICL.pdata);
		String pbill = request.getParameter("bebill");
		int bid = CCliTool.objToInt(pbill, 1);
		boolean isbill = bid==1;
		HttpSession hss = request.getSession();
		HashMap<String, Object> mp = APIUtil.getdbuser(dbid, userCode);
		APIUtil.cpTOHttpSession(mp, hss);
		ReturnObj reoReturnObj = new ReturnObj();
		if(!checkLogin(response, hss, reoReturnObj))
			return ;
		Object cells = CellsSessionUtil.getCellsByCellId(dbid, pcell);
		Cells c1 = null,tjcell=null;
		if (cells instanceof Cells) {
			c1 = (Cells) cells;
		} else {
			Cells[] cells2 = (Cells[]) cells;
			for (int i = 0; i < cells2.length; i++) {
				Cells ci = cells2[i];
				if((ci.attr & ICL.ocCondiction) > 0 && !isbill){
					tjcell = ci;
				}
				if ((ci.attr & ICL.ocCondiction) == 0) {
					c1 = ci;
					break;
				}
			}
		}
		int currPage = CCliTool.objToInt(request.getParameter("currentPage"),0);
		_log.info("currentPage:"+currPage);
		int pageSize = CCliTool.objToInt(request.getParameter("pageSize"),20);
		_log.info("pageSize:"+pageSize);
		String cellid=request.getParameter("cellid");
		cellid = cellid==null ? "" : cellid;
		if(cellid.length()>0){
			if (cells instanceof Cells) {
				c1 = (Cells) cells;
				if(!c1.obj_id.equals(cellid))
					c1 = c1.getChild(cellid, false);
			}else{
				Cells[] cells2 = (Cells[]) cells;
				for(int i=0;i<cells2.length;i++){
					Cells ci = cells2[i];
					if(ci.obj_id.equals(cellid)){
						c1 = ci;
						break;
					}
				}
			}
		}
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(!isbill&&tjcell!=null){
			LayCells contLayCel = new LayCells(tjcell);
			retMap.put("contCel", contLayCel);
		}
		pdata = makeSearchTj(tjcell,pdata);
		PageLayOut pageLayOut =  new PageLayOut(currPage,pageSize,pdata);
		String file = expValues(hss, c1,pageLayOut);
		FileInputStream fis = new FileInputStream(file);
		byte[] b = new byte[1024];
		response.setCharacterEncoding(UTF8);
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods","POST, GET, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers","X-HTTP-Method-Override, Content-Type, x-requested-with, Authorization");
		response.setCharacterEncoding("UTF-8");
		String file_name = new String("fdsfds".getBytes(), "ISO-8859-1");
		response.setHeader("Content-Disposition",
				String.format("attachment; filename=\"%s\"", file_name));
		response.setContentType("application/octet-stream;charset=utf-8");
		_log.info(file_name);
		// 获取响应报文输出流对象
		ServletOutputStream out = response.getOutputStream();
		// 输出
		int n = 0;
		while ((n = fis.read(b)) != -1) {
			out.write(b, 0, n);
		}
		fis.close();
		out.flush();
		out.close();
		File file2 = new File(file);
		file2.delete();
		_log.info("发送完成！"); 
//		retMap.put("pages", pageLayOut);
//		reoReturnObj.makeSuccess();
//		reoReturnObj.setData(retMap);
//		WriteJsonString(response, reoReturnObj);
	}

	/**
	 * 生成查询条件，传入JSON格式对象
	 * @param tjcell
	 * @param pdata
	 * @return
	 */
	private String makeSearchTj(Cells tjcell, String pdata) {
		if(pdata.equals(""))
			return "";
		if(tjcell==null) {
			return pdata;
		}
		JSONObject jsobj = JSONObject.parseObject(pdata);
		pdata = "1=1";
		Cells cc1 = tjcell.c_par;
		int cc = tjcell.db_cels.length;
		int total = cc;
		if(cc1!=null){
			total += 1;
		}
		String[] fld = new String[total];//toCondictions
		Object[] vals = new Object[total];//toCondictions
		int[] types = new int[total];
		for(int i=0;i<cc;i++){
			Cell cel = tjcell.all_cels[i];
			Object o0 = jsobj.get(cel.ccName);
			fld[i] = cel.ccName;
			o0 = o0 == null?"":o0;
			if(!o0.equals("")){
					vals[i] = o0;
			}
			types[i] = cel.ccType;
		}
		if(cc1!=null){
			int pkid = 0;
			if(cc1.pkIndexs().length>=1){
				pkid = cc1.autoInc-1;
			}else{
				pkid = cc1.pkcc-1;
			}
			pkid = pkid<0?0:pkid;
			Cell cel = cc1.all_cels[pkid];
			fld[total-1] =cel.ccName;
			vals[total-1] =  jsobj.get(cel.ccName);
			types[total-1] = cel.ccType;
		}
		pdata = CCliTool.toCondictions(fld, types, CCliTool.toAutoFit(tjcell, vals));
//		pdata = CCliTool.toCondictions(fld, types, vals);
		_log.info(pdata);
		return pdata;
	}

	private Object getCellsBypcell(String pcell, HttpSession hss)
			throws Exception {
		return universaInvoke(34, cl.CLPF.SUnivServ, null, new Object[] { pcell }, false, hss);
	}

	private boolean checkLogin(HttpServletResponse response, HttpSession hss,ReturnObj reoReturnObj) {
		if (hss.getAttribute(cl.ICL.LOGINYN) == null || ITLoginMang.iskill(hss)){
			reoReturnObj.makeFaile("请重新登录");
			reoReturnObj.setId(-2);
			WriteJsonString(response, reoReturnObj);
			return false;
		}
		return true;
	}

	private void makeValues(HttpSession hss, Cells c1, PageLayOut pageLayOut)
			throws Exception {
		pageLayOut =  (PageLayOut)universaInvoke(WebApiInvoke.API_FIND_DATA, APIIV, null, new Object[] { c1,pageLayOut}, false, hss);
	}
	
	private String expValues(HttpSession hss, Cells c1, PageLayOut pageLayOut)
			throws Exception {
		String file =  (String)universaInvoke(WebApiInvoke.API_exportExcel, APIIV, null, new Object[] { c1,pageLayOut}, false, hss);
		return file;
	}
	/***
	 * 获取统计展开项和合计项
	 * @param hss
	 * @param c1
	 * @param pbuid
	 * @return
	 * @throws Exception
	 */
	private Object makeCountCell(HttpSession hss,Cells c1, String pbuid)
			throws Exception {
		return universaInvoke(WebApiInvoke.API_Count, APIIV, null, new Object[] { pbuid,c1}, false, hss);
	}
	
	
	/***
	 * 在自动展开中获取统计项
	 * @param hss
	 * @param pbuid 菜单参数标识号
	 * @return
	 * @throws Exception
	 */
	private Object makeCountFLD(HttpSession hss, String pbuid)
			throws Exception {
		return universaInvoke(WebApiInvoke.API_CountFLD, APIIV, null, new Object[] { pbuid}, false, hss);
	}

	private void makeCountValues(HttpSession hss, Cells c1, PageLayOut pageLayOut,String pgfld,String pgdatafld)
			throws Exception {
		pageLayOut =  (PageLayOut) universaInvoke(WebApiInvoke.API_FIND_DATACount, APIIV, null, new Object[] { c1, pageLayOut,pgfld,pgdatafld}, false, hss);
	}
	
	public static void addElement(HVector hdes,HVector hsrc){
		for(int i=0;i<hsrc.size();i++){
			hdes.addElement(hsrc.elementAt(i));
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void checkAttr(Map hm0, Object attr) {
		 Object o0 = hm0.get(cl.ICL.pattr);
		 String s0 = o0 != null ? o0.toString() : null;
		 int t0 = s0 == null ? 0 : s0.length();
		 if (t0 > 0) {
		  char c0 = t0 > 2 ? s0.charAt(1) : '0';
		  if (c0 == 'X' || c0 == 'x')
		   t0 = Integer.parseInt(s0.substring(2), 16);
		  else
		   t0 = Integer.parseInt(s0, 10);
		 }
		 if (t0 < 1 || (t0 & (cl.ICL.B_IADD | cl.ICL.B_IDEL | cl.ICL.B_ISAVE)) != 0)
		  hm0.put(cl.ICL.pattr, attr);//;-替换成新和操作属性。
		}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void readParas(HttpServletRequest request, HttpServletResponse response, Map hm0, boolean brep) throws Exception {
		 Enumeration enu = request.getParameterNames();
		 String s0, s1;
		 if (brep) {
		  while (enu.hasMoreElements()) {
		   s0 = (String) enu.nextElement();
		   if (CPubTool.indexSub(cl.ICL.PARAFILTER, s0, ',', 0) < 0) {
		    s1 = getParameter(s0,request);
		    if (s1 != null && s1.length() > 0)
		     hm0.put(s0, s1);
		   }
		  }
		 } else {
		  while (enu.hasMoreElements()) {
		   s0 = (String) enu.nextElement();
		   s1 = getParameter(s0,request);
		   if (s1 != null && s1.length() > 0)
		    hm0.put(s0, s1);
		  }
		 }
		}
	
	protected Object checkMenu(HttpServletRequest request, String sql,HttpSession hss) throws Exception{
		 String s0 = request.getParameter(cl.ICL.pmenuid);
		 if (s0 == null || s0.length() < 1)
		  return null;
		 String s1 = CCliTool.objToString(hss.getAttribute(cl.ICL.MNUCHK)); //;-缓存的是其相关条件
		 if (s1 != null && s1.length() > 0)
		  s0 += "|" + s1;
		 return universaInvoke(cl.ICL.RQ_MNUURL, null, null, new Object[]{s0, sql }, false, hss);
		}


	private Object universaInvoke(int oprid,String procName,String oprcmd,Object[] params,boolean usetran,HttpSession hss) throws Exception{
		 WebAppPara wa = new WebAppPara();
		 wa.oprid = oprid;
		 if(procName != null)
			 wa.procName = procName;
		 if(oprcmd != null )
			 wa.oprcmd = oprcmd;
		 wa.params = params;
		 wa.usetran = usetran;
		 wa.copy(hss, null);
		 return _app.universalInvoke(wa);
	}
	
	public static String getParameter(String name, HttpServletRequest request) {
		 /*如果从客户端通过URL传回的参数，第一个用"^"表示带汉字，系统将根据配置定义进行字符集转化 */
		 String s0 = request.getParameter(name);
		 return s0 == null || s0.length() < 1 || s0.charAt(0) != '^' ? s0 : decode(s0.substring(1));
		}
	
	public static String decode(String s0) {
		 String sch = SErvVars.CHARSET; /*"8859_1"*/
		 if (sch == null || sch.length() < 2)
		  return s0;
		 //;--处理如TOMCAT5是以8859_1方式硬编码时，还原成系统传输的UTF-8编码。
		 try {
		  return new String(s0.getBytes(sch), cl.INN.UTF_8);
		 } catch (Exception err) {
		 }
		 return s0;
		}
	
	/**
	 * 获取用户信息
	 * @param hm
	 */
	private UserInfo makeUserInfo(HashMap<String, Object> hm) {
		UserInfo uu = new UserInfo();
		uu.setUserCode(CCliTool.objToString(hm.get("USR.USRCODE")));
		uu.setUserName(CCliTool.objToString(hm.get("USR.USRNAME")));
		uu.setGwCode(CCliTool.objToString(hm.get("USR.GWCODE")));
		uu.setAttr(CCliTool.objToInt(hm.get("USR.USRATTR"), ICL.USR_ONE));//USR.USRATTR
		DempInfo dept = new DempInfo();
		dept.setCmcCode(CCliTool.objToString(hm.get("ORG.COCODE")));
		dept.setCmcName(CCliTool.objToString(hm.get("ORG.CONAME")));
		dept.setDeptCode(CCliTool.objToString(hm.get("USR.ORGCODE")));
		dept.setDeptName(CCliTool.objToString(hm.get("ORG.SELECT USRNAME")));
		uu.setDeptInfo(dept);
		return uu;
	}
	//输出json内容
	public  static void WriteJsonString(HttpServletResponse response, Object vv) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods","POST, GET, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers","X-HTTP-Method-Override, Content-Type, x-requested-with, Authorization");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=utf-8");
		String jsonStr = getJsonString(vv);
		_log.info(jsonStr);
		PrintWriter out = null;
		try {
			out = response.getWriter();
			out.write(jsonStr);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	public static String getJsonString(Object o0) {
		try {
			Object o1 = JSONObject.toJSONString(o0);
			return o1.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取任务和消息
	 * @param request
	 * @param response
	 */
	private void taskAndIM(HttpServletRequest request,
			HttpServletResponse response) {
		String userCode = request.getParameter("usercode");
		String dbid = request.getParameter("dbid");
		ReturnObj reoReturnObj = new ReturnObj();
		HashMap<String, Object> mp = APIUtil.getdbuser(dbid, userCode);
		HttpSession hss = request.getSession();
		APIUtil.cpTOHttpSession(mp, hss);
		if(!checkLogin(response, hss, reoReturnObj)){
			return ;
		}
		try { 
			int taskid = CCliTool.objToInt(request.getParameter("tskim"),APIConst.APIID_TM_MSG);
			if(taskid==APIConst.APIID_TM_MSG_UPD){
				int iid = CCliTool.objToInt(request.getParameter("iid"),1);
				int state = CCliTool.objToInt(request.getParameter("state"),2);
				universaInvoke(taskid, APIRQ, null, new Object[] {iid,state}, false,hss);
			}else if(taskid == APIConst.APIID_TM_TASK_UPD){
				String buno = request.getParameter("buno");
				String buid = request.getParameter("buid");
				String tousr = request.getParameter("tousr");
				universaInvoke(taskid, APIRQ, null, new Object[] {buno,buid,tousr}, false,hss);
			}else{
				int index = CCliTool.objToInt(request.getParameter("page"),1);
				int size = CCliTool.objToInt(request.getParameter("size"),20);
				String keyword = request.getParameter("keyword");
				keyword = decode(keyword);
				PageLayOut page = new PageLayOut(index,size);
				Object o0 = universaInvoke(taskid, APIRQ, null, new Object[] {page,keyword}, false,hss);
				if(o0!=null){
					page = (PageLayOut)o0;
				}
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("page", page);
				reoReturnObj.setData(data);
			}
			reoReturnObj.makeSuccess(); 
		} catch (Exception e) {
//			e.printStackTrace();
		}finally{
			WriteJsonString(response, reoReturnObj);
		}
	}
	
	private void dlgSqlRun(HttpServletRequest request, HttpServletResponse response){
		ReturnObj reoReturnObj = new ReturnObj();
		try {
			String userCode = request.getParameter("usercode");
			String dbid = request.getParameter("dbid");
			HashMap<String, Object> mp = APIUtil.getdbuser(dbid, userCode);
			HttpSession hss = request.getSession();
			APIUtil.cpTOHttpSession(mp, hss);
			if(!checkLogin(response, hss, reoReturnObj)){
				return;
			}
			String jsonstr = request.getParameter("value");
			jsonstr = decode(jsonstr);
			String btnInfo = request.getParameter("btn"); 
			btnInfo = decode(btnInfo);
			
			Object o0 =  universaInvoke(WebApiInvoke.API_DLGSQLRUN,APIIV, null, new Object[]{jsonstr,btnInfo}, false, hss);
			Map<String, String> mp1 = (Map<String, String>) o0;
			String state = mp1.get("state");
			if(state != null){
				if(state.equals("0")){
					reoReturnObj.makeFaile(mp1.get("msg"));
				}else if(state.equals("1")){
					reoReturnObj.makeSuccess(mp1.get("msg"));
				}
			}else{
				reoReturnObj.makeFaile("操作失败！");
			}
		} catch (Exception e) {
			e.printStackTrace();
			reoReturnObj.makeFaile("系统故障！");
		}finally{
			WriteJsonString(response, reoReturnObj);
		}
	}
	
	
	/***
	 * 检测用户是否已经登陆
	 * @param dbid 数据库连接ID
	 * @param user 用户编码/用户名
	 * @return true是没有登陆
	 */
	public static boolean checkISLogin(String dbid,String user){
//		HashMap<String, Object> o0= APIUtil.getdbuser(dbid, user);
//		Object o1 = o0.get(cl.ICL.LOGINYN);
//		return (o1==null)?true:false;
		return true;
	}
}
