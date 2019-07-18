package inetbas.web.outsys.api;

import inet.HVector;
import inetbas.cli.cutil.CCliTool;
import inetbas.rabbitmq.util.ConnectionUtil;
import inetbas.rabbitmq.util.PublisherInfo;
import inetbas.serv.csys.DBInvoke;
import inetbas.serv.systool.SMSGTool;
import inetbas.sserv.SQLExecQuery;
import inetbas.web.outsys.entity.MessageItem;
import inetbas.web.outsys.entity.MsgAndTaskCont;
import inetbas.web.outsys.entity.PageLayOut;
import inetbas.web.outsys.entity.UserInfo;
import inetbas.webserv.WAORGUSR;
import inetbas.webserv.WebAppPara;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cl.ICL;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;

/**
 * @author www.bip-soft.com
 *RabbitMQ消息处理
 */
public class WebRabbitMQInvoke extends DBInvoke {
	private static Logger _log = LoggerFactory.getLogger(WebRabbitMQInvoke.class);
	
	public static final int Msg = 200;
	public static final int RQ_RT = 205;
	private WAORGUSR userWaorgusr;
	public Object processOperator(SQLExecQuery eq, WebAppPara wa) throws Exception {
		int id = wa.oprid;
		userWaorgusr = wa.orgusr;
		if(id == APIConst.APIID_TM_ALL){
			sendRMQMSG(eq, userWaorgusr.USRCODE);
			sendRMQTask(eq, userWaorgusr.USRCODE);
		}
		if(id == APIConst.APIID_TM_MSG){
			sendRMQMSG(eq, userWaorgusr.USRCODE);
		}
		if(id == APIConst.APIID_TM_TASK){
			sendRMQTask(eq, userWaorgusr.USRCODE);
		}
		if(id == APIConst.APIID_TM_RL){
			_log.info("RELoad RabbitMQ Properties files");
			ConnectionUtil.reLoad();
		}
		if(id == APIConst.APIID_TM_MSG_DTL){
			PageLayOut page = (PageLayOut) wa.params[0];
			return getIMList(eq,page);
		}
		if(id == APIConst.APIID_TM_MSG_UPD){
			int iid = CCliTool.objToInt(wa.params[0],0);
			int std = CCliTool.objToInt(wa.params[1],2);
			updateState(eq,iid,std,userWaorgusr.USRCODE);
		}
		return null;
	}
	
	/**
	 * @param eq
	 * @param iid
	 * @param std
	 * @return
	 */
	public static void updateState(SQLExecQuery eq, int iid, int std,String usrCode) {
		String sql = "update insmsga set brd="+std+" where iid="+iid+" and touser='"+usrCode+"'";
		eq.exec(sql);
		sendRMQMSG(eq, usrCode);
	}

	/**
	 * @param eq
	 * @param uSRCODE
	 */
	private PageLayOut getIMList(SQLExecQuery eq, PageLayOut page) {
		//select a.iid,a.smake,a.dmake,a.dkeep,a.stitle,a.sdsc,sfile,fj_root,brd from insmsg a inner join insmsga b 
		//on b.iid=a.iid where b.touser='admin' and b.brd in (0,1)
		// brd:0:公共;1:个人;2:已读
		//select iid,smake,dmake,stitle,sdsc,brd from (select row_number() over (order by dmake) _r,iid,smake,dmake,stitle,sdsc,brd from v_msg where brd in (0,1) and touser='admin') b where _r>10
		int st = (page.currentPage-1)*page.pageSize;
		int end = st+page.pageSize+1;
		String countSQL = "select count(*) from v_sysmsg where brd in (0,1) and touser='"+userWaorgusr.USRCODE+"'";
		String sql = "";
		if(eq.db_type == ICL.MSSQL)
			sql="select iid,smake,dmake,stitle,sdsc,brd from (select row_number() over (order by dmake) _r,iid,smake,dmake,stitle,sdsc,brd from v_sysmsg where brd in (0,1) and touser='"+userWaorgusr.USRCODE+"') b where _r>"+st+" and _r<"+end;
		else {
			sql="select iid,smake,dmake,stitle,sdsc,brd from v_sysmsg brd in (0,1) and touser='"+userWaorgusr.USRCODE+"' limit "+st+","+page.pageSize;
		}
		try {
			int count = CCliTool.objToInt(eq.queryOne(countSQL, -1),-1);
			page.setTotalSize(count);
			if(count>0){
				HVector hh = eq.queryVec(sql);
				ArrayList<JSONObject> data = makeIM(eq, hh);
				page.celData = data;
			}
		} catch (Exception e) {
			createVMSG(eq);
			int count = 0;
			try {
				count = CCliTool.objToInt(eq.queryOne(countSQL, -1),-1);
				page.setTotalSize(count);
				HVector hh = eq.queryVec(sql);
				ArrayList<JSONObject> data = makeIM(eq, hh);
				page.celData = data;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return page;
	}
	
	public static ArrayList<JSONObject> makeIM(SQLExecQuery eq,HVector list) {
		ArrayList<JSONObject> datas = new ArrayList<JSONObject>();
		Map<String, String> users = getUsers(eq);
		for (int i = 0; i < list.size(); i++) {
			Object[] o0 = (Object[]) list.elementAt(i);
			MessageItem msItem = new MessageItem();
			msItem.setIid(CCliTool.objToInt(o0[0], 0));
			String userId = CCliTool.objToString(o0[1]);
			UserInfo u = new UserInfo();
			u.setUserCode(userId);
			if(users.containsKey(userId)){
				u.setUserName(users.get(userId));
			}else{
				u.setUserName(userId);
			}
			msItem.setSmake(u);
			String dd = CCliTool.dateToString(CCliTool.objToDate(o0[2]), true, 6);
			msItem.setDmake(dd);
			String title = CCliTool.objToString(o0[3]);
			msItem.setTitle(title);
			String content = CCliTool.objToString(o0[4]);
			msItem.setContent(content);
			int brd = CCliTool.objToInt(o0[5],0);
			msItem.setBrd(brd);
			datas.add((JSONObject) JSONObject.toJSON(msItem));
		}
		return datas;
	}
	
	public static Map<String, String> getUsers(SQLExecQuery eq){
		String sql="select usrcode,usrname from insuser";
		HashMap<String, String> users = new HashMap<String, String>();
		try {
			HVector hh = eq.queryVec(sql);
			for(int i=0;i<hh.size();i++){
				Object[] o0 = (Object[]) hh.elementAt(i);
				String code = CCliTool.objToString(o0[0]);
				String name = CCliTool.objToString(o0[1]);
				name = name==null?code:name;
				users.put(code, name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return users;
	}

//	/**
//	 * @param eq
//	 * @param uSRCODE
//	 */
//	private ArrayList<TaskEntity> getTaskList(SQLExecQuery eq, String userCode) {
//		//select buno,buid,rlid,frusr,ddate,brd from instask where tousr='admin'
//		String sql = "select buno,buid,rlid,frusr,ddate,brd from instask where tousr='"+userCode+"'";
//		return null;
//	}

	/**
	 * @param eq
	 */
	private synchronized void createVMSG(SQLExecQuery eq) {
		String sql = "drop view v_sysmsg";
//		eq.exec(sql);
		sql="create view v_sysmsg as (select a.iid,a.smake,a.dmake,a.stitle,a.sdsc,brd,b.touser from insmsg a inner join insmsga b on b.iid=a.iid)";
		eq.exec(sql);
	}

	public static void sendRMQTask(SQLExecQuery eq,String userCode){
		try {
			String sql = "select count(*) from instask where tousr = '"+userCode+"'";
			_log.info(sql);
			Object o0 = eq.queryOne(sql, 0);
			int count = CCliTool.objToInt(o0, 0);
			MsgAndTaskCont inf = new MsgAndTaskCont();
			inf.type = 1;
			inf.count = count;
			String msg="";
			msg = JSON.toJSON(inf).toString();
			PublisherInfo.setTaskORMSG(msg, userCode, eq.db_id, inf.type);
			_log.info(msg);
		} catch (Exception e) {
			e.printStackTrace();
			_log.error("task",e);
		}
	}
	
	public static void sendRMQMSG(SQLExecQuery eq,String userCode){
		try {
			WebAppPara wa = new WebAppPara();
			WAORGUSR waorgusr = new WAORGUSR();
			waorgusr.USRCODE = userCode;
			String sql1 = "select orgcode,gwcode from insuser where usrcode='"+userCode+"'";
			Object[] oo = eq.queryRow(sql1, false);
			waorgusr.GWCODE = CCliTool.objToString(oo[1]);
			waorgusr.ORGCODE = CCliTool.objToString(oo[0]);
			wa.orgusr = waorgusr;
			SMSGTool.recMSGNew(eq, wa);
			String sql = "select count(*) from insmsga where touser = '"+userCode+"' and brd in (0,1)";
			Object o0 = eq.queryOne(sql, 0);
			_log.info(sql);
			int count = CCliTool.objToInt(o0, 0);
			MsgAndTaskCont inf = new MsgAndTaskCont();
			inf.type = 2;
			inf.count = count;
			String msg="";
			msg = JSON.toJSON(inf).toString();
			PublisherInfo.setTaskORMSG(msg, userCode, eq.db_id, inf.type);
			_log.info(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
