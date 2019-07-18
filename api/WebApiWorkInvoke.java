/**
 * 
 */
package inetbas.web.outsys.api;

import inet.HVector;
import inetbas.cli.cutil.CCliTool;
import inetbas.pub.coob.CEAPara;
import inetbas.serv.csys.DBInvoke;
import inetbas.serv.systool.SWorkMang;
import inetbas.sserv.SQLExecQuery;
import inetbas.sserv.SSTool;
import inetbas.web.outsys.entity.ApprovalFlowObj;
import inetbas.web.outsys.entity.CWorkInfo;
import inetbas.web.outsys.entity.UserInfo;
import inetbas.web.outsys.entity.WebCEAPars;
import inetbas.web.webpage.wxpub.ApproveTool;
import inetbas.web.webpage.wxserv.WeiWorkMang;
import inetbas.webserv.WebAppPara;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author www.bip-soft.com
 *
 */
public class WebApiWorkInvoke extends DBInvoke {
	private static Logger _log = LoggerFactory.getLogger(WebApiWorkInvoke.class);
	public static final int APIID_CHKUPNEXT = 33;
	public static final int APIID_CHKUPEQ = 34;
	public static Map<String, String> stateMap;
	public static Map<String, String> userMap;
	public Object processOperator(SQLExecQuery eq, WebAppPara wa) throws Exception {
		int id = wa.oprid;
		if(stateMap==null){
			getStateMap(eq);
			getUserMap(eq);
		}
		if(id == cl.CLPF.WM_TOCHK){
			//获取下一个审批节点
			_log.info("获取下一个审批节点");
			return getWorkStateAndMan(eq,wa);
		}
		if(id==cl.CLPF.WM_TOUP){
			//执行审批
			_log.info("执行审批");
			return submitWork(eq,wa);
		}
		if(id== cl.CLPF.WM_BKUP){
			_log.info("执行审批退回");
//			_log.info("审批流参数错误");
			return tochkupbk(eq,wa,true);
		}
		if(id == cl.CLPF.WM_BKCHK)
		{
			_log.info("执行放弃审核");
//			_log.info("审批流参数错误");
			return tochkupbk(eq,wa,false);
		}
		_log.info("审批流参数错误");
		return null;
	}
	/**
	 * @param eq
	 */
	private void getStateMap(SQLExecQuery eq) {
		try {
			String stateStr = SSTool.loadConst1(eq, "D.STATE");
			stateStr = SSTool.formatVarMacro(stateStr, eq);
			HVector hh = eq.queryVec(stateStr);
			if(hh!=null){
				stateMap = new HashMap<String, String>();
				for(int i=0;i<hh.size();i++){
					Object[] o0 = (Object[])hh.elementAt(i);
					stateMap.put(CCliTool.objToString(o0[0]), CCliTool.objToString(o0[1]));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param eq
	 */
	private void getUserMap(SQLExecQuery eq) {
		try {
			HVector hh = eq.queryVec("select usrcode,usrname from insuser");
			if(hh!=null){
				userMap = new HashMap<String, String>();
				for(int i=0;i<hh.size();i++){
					Object[] o0 = (Object[])hh.elementAt(i);
					userMap.put(CCliTool.objToString(o0[0]), CCliTool.objToString(o0[1]));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 审核和驳回
	 * @param eq
	 * @param wa
	 * @return
	 * @throws Exception 
	 */
	private Object submitWork(SQLExecQuery eq, WebAppPara wa) throws Exception {
		WebCEAPars ceaPars = (WebCEAPars) wa.params[0];
		CEAPara ea = ApproveTool.getCeaPara(eq, CCliTool.objToString(ceaPars.sbuid));
		String reuObj=ceaPars.stateto+"";
		boolean agr = CCliTool.objToInt(ceaPars.bup, 2) == 1 ? true : false;
		if(agr){
			eq.beginTrans();
			try {
				ea.vid = ceaPars.sid;
				ea.stfr = CCliTool.objToInt(ceaPars.statefr, 0);
				ea.stto = CCliTool.objToInt(ceaPars.statefr, 0);
				ea.sdsc = ceaPars.yjcontext.length() < 1 ? "审批同意" : ceaPars.yjcontext;
				String fmu = CCliTool.objToString(eq.queryOne("select sflds from insbufield where buid='"
								+ ceaPars.sbuid+ "' and rlid=" + ea.stfr));
				if (fmu != null && fmu.length() > 0) {
					fmu = fmu.replace(';', ',');
					ea.fmu = fmu;
				}
				wa.params = new Object[] { ea, "" };
				if(!ceaPars.ckd){
					if(ea.stfr!=cl.ICL.EANEW &&ea.stfr!=cl.ICL.EABACK){
						SWorkMang.tochkup(eq, wa, false, false);
						WeiWorkMang.tochkup(eq, wa, false, false);
						WebRabbitMQInvoke.sendRMQTask(eq,wa.orgusr.USRCODE);
						String makeu = WeiWorkMang.getMakeUser(eq, ea);
						if(makeu!=null){
							if(!makeu.equals(wa.orgusr.USRCODE))
								WebRabbitMQInvoke.sendRMQMSG(eq,makeu);
						}
					}
				}
				ea.stto = ceaPars.stateto;
				ea.schk_mk = ceaPars.stateto + "," + ceaPars.tousr;
				wa.params = new Object[] { ea };
				ea.sdsc = null;
				ea.fmu = null;
				SWorkMang.tochkup(eq, wa, true, false);
				wa.params = new Object[] { ea, ceaPars.content };
				WeiWorkMang.tochkup(eq, wa, true, false);
				WebRabbitMQInvoke.sendRMQTask(eq,ceaPars.tousr);
//				WebRabbitMQInvoke.sendRMQMSG(eq,ceaPars.tousr);
			} catch (Exception e) {
				e.printStackTrace();
			}
			eq.endTrans(true);
		}else{
			ea.vid = ceaPars.sid;
			ea.sdsc = ceaPars.yjcontext.length() > 0 ? ceaPars.yjcontext : "驳回!";
			wa.params = new Object[] { ea };
			ea.stfr = ceaPars.statefr;
			ea.stto = ceaPars.stateto;
			ea.schk_mk=ceaPars.tousr;
			String fmu = CCliTool.objToString(eq.queryOne("select sflds from insbufield where buid='"
							+ ceaPars.sbuid + "' and rlid=" + ea.stfr));
			if (fmu != null && fmu.length() > 0) {
				fmu = fmu.replace(';', ',');
				ea.fmu = fmu;
			}
			eq.beginTrans();
			try {
				Object o0 = SWorkMang.tochkup(eq, wa, false, true);
				reuObj = CCliTool.objToString(((Object[])o0)[2]);
				wa.params = new Object[] { ea, ceaPars.content };
				WeiWorkMang.tochkup(eq, wa, false, true);
				WebRabbitMQInvoke.sendRMQTask(eq,wa.orgusr.USRCODE);
				WebRabbitMQInvoke.sendRMQMSG(eq,wa.orgusr.USRCODE);
				if(!wa.orgusr.USRCODE.equals(ceaPars.tousr)){
					WebRabbitMQInvoke.sendRMQTask(eq,ceaPars.tousr);
					WebRabbitMQInvoke.sendRMQMSG(eq,ceaPars.tousr);
				}
				eq.endTrans(true);
			} catch (Exception e) {
				e.printStackTrace();
				eq.endTrans(false);
			}
		}
//		WebRabbitMQInvoke.sendRMQTask(eq, wa.orgusr.USRCODE);
		return reuObj;
	}

	/***
	 * 提交退回和放弃审核
	 * 
	 * @param eq
	 * @param wa
	 * @return
	 * @throws Exception
	 */
	private Object tochkupbk(SQLExecQuery eq, WebAppPara wa, boolean bup)
			throws Exception {
		WebCEAPars ceaPars = (WebCEAPars) wa.params[0];
		CEAPara ea = ApproveTool.getCeaPara(eq,
				CCliTool.objToString(ceaPars.sbuid));
		eq.beginTrans();
		try {
			ea.vid = ceaPars.sid;
			ea.stfr = ceaPars.statefr;
			ea.stto = ceaPars.stateto;
			String fmu = CCliTool.objToString(eq
					.queryOne("select sflds from insbufield where buid='"
							+ ceaPars.sbuid + "' and rlid=" + ea.stfr));
			if (fmu != null && fmu.length() > 0) {
				fmu = fmu.replace(';', ',');
				ea.fmu = fmu;
			}
			wa.params = new Object[] { ea, "" };
			Object o0 = SWorkMang.tochkupbk(eq, wa, bup);
			WebRabbitMQInvoke.sendRMQTask(eq, wa.orgusr.USRCODE);
			String makeu = WeiWorkMang.getMakeUser(eq, ea);
			if(makeu!=null)
				WebRabbitMQInvoke.sendRMQMSG(eq,makeu);
			if(o0==null)
				return ea.stfr;
			_log.info(CCliTool.objToString(o0));
			return o0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "1";
	}
	/**
	 * @param eq
	 * @param wa
	 * @throws Exception 
	 */
	private Object getWorkStateAndMan(SQLExecQuery eq, WebAppPara wa) throws Exception {
		CWorkInfo info = new CWorkInfo();
		ArrayList<ApprovalFlowObj> list = new ArrayList<ApprovalFlowObj>();
		Object[] o0 = wa.params;
		WebCEAPars ceaPars = (WebCEAPars)o0[0];
		Object[] oo = new Object[2];
		oo[0] = ceaPars.sbuid;
		oo[1] = CCliTool.objToInt(ceaPars.bup, 1) == 1 ? true : false;
		wa.params = oo;
		Object[] ob = (Object[]) SWorkMang.initea(eq, wa);
		CEAPara ea = ApproveTool.getCeaPara(eq, ceaPars.sbuid);
		ea.vid = ceaPars.sid;
		HVector pea = (HVector) ob[0];// 审批节点
		HVector pchk = (HVector) ob[1];// 操作员或者是岗位
		HVector prlex = (HVector) ob[2];// 会审节点
		HVector hh=null;
		info.setState(ceaPars.statefr+"");
		if(ceaPars.statefr==cl.ICL.EANEW ||ceaPars.statefr==cl.ICL.EABACK){
			info.setChecked(true);
		}else if(ceaPars.statefr==6){
			info.setChecked(true);
			String sql="select rlto,tousr,rlfr,frusr,max(cid) cid from "+ea.tbname+"_ea where buno='"+ea.vid+"' and cid>=0 group by rlto,tousr,rlfr,frusr";
			Object[] cks = eq.queryRow(sql, false);
			if(cks!=null){
				int curr = CCliTool.objToInt(cks[0], 0);
				int frst = CCliTool.objToInt(cks[2], 0);
				String frusr = CCliTool.objToString(cks[3]);
				String ckman = CCliTool.objToString(cks[1]);
				if(ckman!=null){
					UserInfo userInfo = new UserInfo();
					userInfo.setUserCode(ckman);
					userInfo.setUserName(userMap.get(ckman));
					if(curr==6){
						ArrayList<UserInfo> users = new ArrayList<UserInfo>();
						users.add(userInfo);
						info.setChkInfos(users);
						info.setUpState(frst+"");
						UserInfo userInfo1 = new UserInfo();
						userInfo1.setUserCode(frusr);
						userInfo1.setUserName(userMap.get(frusr));
						info.setUpUser(userInfo1);
					}else{
						info.setUpState(curr+"");
						info.setUpUser(userInfo);
						ArrayList<UserInfo> users = new ArrayList<UserInfo>();
						users.add(userInfo);
						info.setChkInfos(users);
					}
				}
			}
		}else{
			String sql="select rlfr,tousr,frusr,max(cid) cid from "+ea.tbname+"_ea where buno='"+ea.vid+"' and cid>=0 and rlto="+ceaPars.statefr+" group by rlfr,tousr,frusr";
			Object[] cks = eq.queryRow(sql, false);
			if(cks!=null){
				int curr = CCliTool.objToInt(cks[0], 0);
				info.setUpState(curr+"");
				String userId = CCliTool.objToString(cks[2]);
				UserInfo uu = new UserInfo();
				uu.setUserCode(userId);
				uu.setUserName(userMap.get(userId));
				info.setUpUser(uu);
				String ckman = CCliTool.objToString(cks[1]);
				ArrayList<UserInfo> users = new ArrayList<UserInfo>();
				if(ckman==null){
					info.setChecked(false);
					sql="select tousr from instask where buno='"+ea.vid+"' and rlid="+ceaPars.statefr+" and buid='"+ea.vbu+"'";
					_log.info(sql);
					HVector userids = eq.queryCol(sql);
					if(userids!=null){
						for(int i=0;i<userids.size();i++){
							String id = CCliTool.objToString(userids.elementAt(i));
							if(id!=null){
								UserInfo u = new UserInfo();
								u.setUserCode(id);
								u.setUserName(userMap.get(id));
								if(!users.contains(u))
									users.add(u);
							}
						}
						info.setChkInfos(users);
					}
				}
				else{
					info.setChecked(true);
					UserInfo uuInfo = new UserInfo();
					uuInfo.setUserCode(ckman);
					uuInfo.setUserName(userMap.get(ckman));
					users.add(uuInfo);
					info.setChkInfos(users);
				}
			}
		}
		if(pea!=null){
			hh = checkrole(ceaPars.statefr, ea, wa, pea, prlex, pchk,eq);
			if(hh!=null&&hh.size()>0){
				for (int i = 0; i < hh.size(); i++) {
					Object[] states = (Object[])hh.elementAt(i);
					String stateto = CCliTool.objToString(states[0]);
					HVector usrs = null;
					if(states[1]!=null){
						usrs = (HVector)states[1];
					}
					ApprovalFlowObj apo = makeAPO(stateto,usrs);
					list.add(apo);
				}
			}
		}else {
			ApprovalFlowObj apo = makeAPO("6",null);
			list.add(apo);
		}
		info.setList(list);
		return info;
	}
	
	public static ApprovalFlowObj makeAPO(String state,HVector hh){
		ApprovalFlowObj apo = new ApprovalFlowObj();
		apo.setStateId(state);
		if(stateMap!=null)
			apo.setStateName(stateMap.get(state));
		if(hh!=null){
			ArrayList<UserInfo> usrs = new ArrayList<UserInfo>();
			for(int i=0;i<hh.size();i++){
				UserInfo userInfo = new UserInfo();
				Object[] oo = (Object[])hh.elementAt(i);
				String usrcode = CCliTool.objToString(oo[1]);
				if(usrcode!=null){
					userInfo.setUserCode(usrcode);
					userInfo.setUserName(userMap.get(usrcode));
					usrs.add(userInfo);
				}
				
			}
			apo.setUsers(usrs);
		}
		return apo;
	}
	
	public HVector checkrole(int frrl,CEAPara ea,WebAppPara wb,HVector pea,HVector prlex,HVector pchk,SQLExecQuery eq) {
		int piea = pea != null ? pea.size() : 0;
		int t0 = wb.orgusr.USRATTR, cea = piea;
		HVector o0 = new HVector();
		if (cea < 1)
			return null;
		int chk = pchk==null?0:pchk.size();
		boolean bsh = t0 >= cl.ICL.USR_FULL && t0 <= cl.ICL.USR_ORG;
		Object[] os0 = null, vals = pea.values();// pea代表审核流程数据
		if (frrl == cl.ICL.EABACK)
			frrl = cl.ICL.EANEW;
		String sgs;
		int atr;
		for (int i = 0; i < cea; i++) {
			os0 = (Object[]) vals[i];// ;-开始节点;目标节点;公式;属性
			t0 = ((Number) os0[0]).intValue();
			Object[] oo = new Object[2];
			if (t0 > frrl)
				break;
			if (t0 == frrl) {
				sgs = (String) os0[2];
				if (sgs == null || sgs.length() < 1 || ApproveTool.isLogic(eq,sgs,ea)) {
					t0 = CCliTool.objToInt(os0[1], 0);// --目标角色。
					atr = CCliTool.objToInt(os0[3], 0);// --属性。
					if (t0 < cl.ICL.EAOK || t0 >= cl.ICL.EAUDF){
						HVector hh = ApproveTool.checkusr(t0, atr, bsh, chk,ea,wb,pea,prlex,pchk,eq);
						oo[0] = t0;
						oo[1] = hh;
						o0.addElement(oo);
					}
					else {
						oo[0] = t0;
						o0.addElement(oo);
					}
				}
			}
		}
		return o0;
	}

}
