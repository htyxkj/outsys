package inetbas.web.outsys.api;

import inet.HVector;
import inetbas.cli.cutil.CCliTool;
import inetbas.pub.coob.Cell;
import inetbas.serv.csys.DBInvoke;
import inetbas.sserv.SQLExecQuery;
import inetbas.sserv.SSTool;
import inetbas.web.outsys.entity.AssistObj;
import inetbas.web.outsys.entity.BipInsAid;
import inetbas.web.outsys.entity.BipInsAidNew;
import inetbas.web.outsys.entity.BipInsAidType;
import inetbas.web.outsys.redis.RedisHelper;
import inetbas.web.outsys.uiparam.LayCell;
import inetbas.web.webpage.wxpub.VarObject;
import inetbas.webserv.WAORGUSR;
import inetbas.webserv.WebAppPara;

import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.shade.org.apache.commons.codec.digest.DigestUtils;

/**
 * 辅助、常量、自定义sql、变量查询服务
 * @author www.bip-soft.com
 *
 */
public class WebApiAidInvoke extends DBInvoke {
	private static Logger _log = LoggerFactory.getLogger(WebApiAidInvoke.class);
	
	public static final String ConstL = "常量查询";
	private static final String REDISAID = ".BipInsAid.";//储存在redis中的 辅助部分标识
	
	public static final int AID_A = 200;// 获取辅助基本信息或常量
	public static final int AID_A_D = 210;// 获取辅助数据
	public static final int AID_V = 201;//变量查询
	public static final int AID_AST = 300;//辅助
	public static final int AID_SQL = 400;//辅助
	static WAORGUSR wour=null;
	//辅助执行的是  inetbas.cli.cutil.CGroupEditor 类
	private static final int ASSIST_SELECE=0,ASSIST_GROUP=1,ASSIST_GDIC=2;

	public Object processOperator(SQLExecQuery eq, WebAppPara wa) throws Exception{
		int id = wa.oprid;
		wour = wa.orgusr; 
//		b+=1;
//		_log.info(""+(b));
		if(id==AID_A){//获取辅助信息
			String assist = CCliTool.objToString(wa.params[0]);
			String dbid = CCliTool.objToString(wa.params[1]); 
			return findAssistInfo(eq,assist,dbid);
		}
		if(id == AID_A_D){
			String assist = CCliTool.objToString(wa.params[0]);
			String dbid = CCliTool.objToString(wa.params[1]);
			String cont = CCliTool.objToString(wa.params[2]);
			int pageSize = CCliTool.objToInt(wa.params[3], 20);
			int currentPage = CCliTool.objToInt(wa.params[4], 1);
			String key = assist+cont+pageSize+"_"+currentPage;
			key = DigestUtils.md5Hex(key.getBytes());
			String vals = RedisHelper.get(key);
			if(vals==null) {
				synchronized (key.intern()) {
					_log.info("从DB中获取"+assist);
					vals = RedisHelper.get(key);
					if(vals==null) {
						BipInsAid o0 = findAssistData(eq,assist,dbid,cont,pageSize,currentPage);
						RedisHelper.set(key, JSON.toJSONString(o0),300);
						return o0;
					}else {
						_log.info("从缓存中获取"+assist);
						BipInsAid o0 = JSON.parseObject(vals,BipInsAid.class);
						return o0;
					}

				}
			}else {
				_log.info("从缓存中获取"+assist);
				BipInsAid o0 = JSON.parseObject(vals,BipInsAid.class);
				return o0;
			}
		}
		if(id == AID_V){
			String varid = CCliTool.objToString(wa.params[0]);
			varid = formatBracket(varid);
			String oo= (String)eq.queryOne("select sbds from inssysvar where sname='" + varid + "'", "");
			VarObject vv = new VarObject();
			if(oo.length()>0){
				vv.setCode(1);
				vv.setTitle("变量");
				vv.setMessage("操作成功");
				vv.type = 2;
				vv.varValue = oo;
				return vv;
			}
			_log.info(varid);
			vv.setCode(-1);
			vv.setMessage("没有找到"+varid);
			return vv;
		}
		if(id == AID_AST) {
			String assist = CCliTool.objToString(wa.params[0]);
			String cont = CCliTool.objToString(wa.params[1]);
			int page = CCliTool.objToInt(wa.params[2], 1);
			int pageSize = CCliTool.objToInt(wa.params[3], 20);
			String assistType = CCliTool.objToString(wa.params[4]);
			String script = CCliTool.objToString(wa.params[5]);
			assist = assist.replaceAll("\\{", "").replaceAll("\\}", "");
			return findAidByPage(eq, assist, cont,page,pageSize,assistType,script);
		}
		
		if(id == AID_SQL){
			String sql = CCliTool.objToString(wa.params[0]);
			String type = CCliTool.objToString(wa.params[1]);
			return findSQL(eq,sql,type);
		}
		return null;
	}
	
	/**
	 * 获取辅助详细信息或常量信息和数据
	 * @param eq
	 * @param assist 辅助/常量名称
	 * @return
	 * @throws Exception
	 */
	private Object findAssistInfo(SQLExecQuery eq,String assist,String dbid) throws Exception{
		assist = assist.replaceAll("\\{", "").replaceAll("\\}", "");
		char x0 = assist.charAt(0);
		assist =assist.substring(1);
		//先从redis中获取缓存数据，如果有缓存数据直接返回缓存数据否则查询查询数据返回并放入redis中
		String key = dbid+REDISAID+assist;
		String ass = RedisHelper.get(key);
		if(ass!=null){
			JSONObject jsonObject =JSONObject.parseObject(ass);
			jsonObject.put("slink", null);
			return jsonObject;
		}
		if(x0=='$'){//常量
			return findCLInfo(eq,assist,key);
		} else {//辅助
			return findAidInfo(eq,assist,key); 
		} 
	}
	/**
	 * 获取辅助信息
	 * @param eq
	 * @param assist 辅助名称
	 * @param key    储存在redis中用的key
	 * @return
	 * @throws Exception
	 */
	private Object findAidInfo(SQLExecQuery eq,String assist,String key) throws Exception{
		String sql = "select slabel,slink,sflag,sclass from insaid where (sclass='inetbas.cli.cutil.CSelectEditor' or sclass='inetbas.cli.cutil.CGroupEditor' or sclass='inetbas.cli.cutil.CDynaEditor' or sclass='ebas.cutil.CGDicEditor' ) and sid='"+ assist + "'";
		Object[] o0 = eq.queryRow(sql, false);
		if (o0 != null) {
			BipInsAid aid = new BipInsAid();
			aid.setTitle(CCliTool.objToString(o0[0]));//标题
			aid.setSlink(CCliTool.objToString(o0[1]));//初始SQL
			aid.setSflag(CCliTool.objToString(o0[2]));
			aid.mklables(CCliTool.objToString(o0[2]));////显示列下标//显示列名称//列标签   ---- 辅助中的标识
			String sclass = CCliTool.objToString(o0[3]);//类名
			int type = 0;
			if("inetbas.cli.cutil.CGroupEditor".equals(sclass)){
				type = 1;
			}else if("inetbas.cli.cutil.CSelectEditor".equals(sclass)){
				type = 0;
			}else if("ebas.cutil.CGDicEditor".equals(sclass)){
				type = 2;
			}
			aid.setType(type);
			String selSql = aid.getSlink(); 
			if(selSql!=null && selSql.length()>1){
				String queryStr = "";
				int _idx = selSql.indexOf("where");
				if(_idx>=0){
					queryStr = selSql.substring(0,_idx).trim();
				}else{
					queryStr = selSql;
				}
				queryStr = formartSql(queryStr, null);
				Object[] obj = eq.queryRow(queryStr, true);
				if(obj != null){ 
					Cell[] cells = (Cell[]) obj[0];
					aid.mklayCells(cells);
				}else{
					aid.mklaySQLCells(queryStr);
				}
			}
			String jsonStr = JSONObject.toJSONString(aid);
			RedisHelper.setNoTime(key, jsonStr);
			aid.setSlink(null);
			jsonStr = JSONObject.toJSONString(aid); 
			return JSONObject.parseObject(jsonStr); 
		}
		return null;
	}
	/***
	 * 
	 * @param eq
	 * @param assist 常量名称
	 * @param key    储存在redis中用的key
	 * @return
	 * @throws Exception
	 */
	private Object findCLInfo(SQLExecQuery eq,String assist,String key) throws Exception{
		String sv = SSTool.loadConst1(eq,assist);
		BipInsAid aid = new BipInsAid(); 
		aid.setTitle(ConstL);
		char c0 ;
		if (sv != null) {
			sv = sv.trim();
			if(sv.startsWith(cl.ICL.EX)){
				sv = SSTool.checkMulTxt(sv,eq); 
			}
			boolean isSql = false;
			c0 = sv.charAt(0);
			int lens = sv.length();
			if (c0 == '{') {
				sv = sv.substring(1, lens - 1);
				String[] vals = sv.split(";");
				ArrayList<JSONObject> alist = new ArrayList<JSONObject>();
				for (int i = 0; i < vals.length; i++) {
					String idv = vals[i];
					String[] bb = idv.split(":");
					JSONObject jsob = new JSONObject();
					if(bb.length<=1){
						jsob.put("name", "");
					}else{
						jsob.put("name", bb[1]);
					}
					jsob.put("code",bb[0]);
					alist.add(jsob);
				} 
				aid.setLabers(new String[]{"code","name"});
				LayCell[] layCells = new LayCell[2];
				LayCell l1 = new LayCell();
				l1.id="code";
				LayCell l2 = new LayCell();
				l2.id="name";
				layCells[0] = l1;
				layCells[1] = l2; 
				aid.setLayCells(layCells);
				aid.setShowColsIndex(new int[]{0,1}); 
				aid.setValues(alist); 
			}else if (sv.startsWith("select")) {
				isSql = true;
				sv = sv.replace("{_}", "1=1");
				sv = SSTool.formatVarMacro(sv, eq); 
				HVector hh = eq.queryVec(sv, true, 0); 
				MakeLabersAndData(aid, hh); 
			}else{
				ArrayList<JSONObject> values = new ArrayList<JSONObject>();
				JSONObject job = new JSONObject();
				job.put("values", sv);
				values.add(job);
				aid.setValues(values );
			}
			
			String jsonStr = JSONObject.toJSONString(aid);
			if(!isSql){
				RedisHelper.setNoTime(key, jsonStr);
			}
			return JSONObject.parseObject(jsonStr);
		} 
		return null;
	} 
	
	
	/**
	 * 获取辅助信息
	 * @param eq
	 * @param assist 辅助名称
	 * @param dbid 数据库标识
	 * @param cont 条件
	 * @param pagesize 每页条数
	 * @param currentpage 第几页
	 * @return
	 * @throws Exception
	 */
	private BipInsAid findAssistData(SQLExecQuery eq,String assist1,String dbid,String cont,int pagesize,int currentpage) throws Exception{
		String assist = assist1;
		assist = assist.replaceAll("\\{", "").replaceAll("\\}", "");
		char x = assist.charAt(0);
		assist =assist.substring(1);
		//先从redis中获取缓存数据
		String key = dbid+REDISAID+assist;
		String ass = RedisHelper.get(key);
		if(ass == null){
			findAssistInfo(eq, x+assist, dbid);
			ass = RedisHelper.get(key);
			if(ass == null)
				return null;
		}
		BipInsAid bipAid = JSONObject.parseObject(ass, BipInsAid.class);
		AssistObj sv = new AssistObj();
		sv.mklables(bipAid.getSflag());
		sv.setSlink(bipAid.getSlink()); 
		sv.type = bipAid.getType();
		cont = cont ==null?"":cont;
		String script="";
		if (sv != null) {
			String sql = sv.getSlink();
			//CGroupEditor  辅助
			if(bipAid.getType() == ASSIST_GROUP ){
				String s1="";
				String sld = sql;//;--select ..... where [[#]字段名]...
				int x0 = sld.indexOf('[');
				int x1 = sld.indexOf(']', x0 + 1), t1 = script == null ? 0 : script.length();
				sql = sld.substring(x0 + 1, x1);
				boolean b0 = sql.charAt(0) == '#';//;-数值
//				if (t1 < 1 || (t1 == 1 && script.charAt(0) == '*'))
//					s1 = "1>0";//取所有值。
//				else
					s1 = CCliTool.toBlurCond(b0 ? sql.substring(1) : sql, b0 ? Types.INTEGER : Types.VARCHAR, script);
				if(s1==null) {
					sql = sld.substring(0, x0) + " 1=1 " + sld.substring(x1 + 1);
				}else {
					sql = sld.substring(0, x0) + s1 + sld.substring(x1 + 1);
				}
				sv.setSlink(sql);
			}
			
			sql = SSTool.formatVarMacro(sql, eq);
			sql = sql.replaceAll("\\[!\\]", wour.ORGCODE);
			sql = sql.replaceAll("\\[#\\]", wour.CMCODE);
			sql = sql.replaceAll("\\[$\\]", wour.USRCODE);
			
			sv.setSlink(sql);
			String bForSql = cont.length()>0 ?sv.makeQuery(cont):sv.getSlink();
			bForSql = formartSql(bForSql, cont);
			bForSql = SSTool.formatVarMacro(bForSql, eq);
			_log.info(bForSql);
			String dblink = "";
			if(bForSql.startsWith("@")){
				bForSql = bForSql.substring(1);
				int ii1 = bForSql.indexOf("@");
				if(ii1>0){
					dblink = bForSql.substring(0,ii1);
					_log.info(bForSql);
					bForSql = bForSql.substring(ii1+1);
				}
			}			
			int dateType = eq.db_type;
			String[] sqls = makePageSql(dateType,bForSql,sv,currentpage,pagesize);
			int total=0;
			HVector hh=null;
			if(dblink.length()>0){
				SQLExecQuery eq1 = eq.getDBLNKVAR(
						dblink, false);
				total = CCliTool.objToInt(eq1.queryOne(sqls[0]),0);
				_log.info(sqls[0]);
				_log.info(sqls[1]);
				hh = eq1.queryVec(sqls[1], true, 0);
				eq1.close();
			}else {
				_log.info(sqls[0]);
				total = CCliTool.objToInt(eq.queryOne(sqls[0]),0);
				_log.info(sqls[1]);
				hh = eq.queryVec(sqls[1], true, 0);
			}			 
			bipAid.setTotal(total);
			MakeLabersAndData(bipAid, hh);
			//GDIC  辅助查询 单位信息
			if(bipAid.getType() == ASSIST_GDIC){
				for (int i = 0; i < bipAid.getValues().size(); i++) {
//					Iterator<String> keys = bipAid.getValues().get(i).keySet().iterator();// jsonObject.keys();
//					String key0 ="";
//			        if (keys.hasNext()){ 
//			            key0 = keys.next();
//			        }
			        String sqlUnit = "select g.bzunit,g.hsgx,u.umc from gdichs g inner join udic u on u.ubm = g.bzunit  where g.gbm='" + bipAid.getValues().get(i).get("gbm") + "'";

			        HVector hv = eq.queryVec(sqlUnit);
			        if(hv == null)
			        	hv = new HVector();
			        JSONArray jsonArr = new JSONArray();
			        for (int j = 0; j < hv.size(); j++) {
			        	Object[] object = (Object[]) hv.elementAt(j);
			        	JSONObject jsonObject = new JSONObject();
						jsonObject.put("bzunit", object[0]);
						jsonObject.put("hsgx", object[1]);
						jsonObject.put("name", object[2]);
						jsonArr.add(jsonObject);
					}
			        if(jsonArr != null)
			        	bipAid.getValues().get(i).put("ghsunit", JSON.toJSON(jsonArr));
				}
			}
			bipAid.setSlink(""); 
		} 
		return bipAid; 
	}
	
	 
	/***
	 * 整理数据
	 * @param vv
	 * @param hh
	 */
	private static void  MakeLabersAndData(BipInsAid vv,HVector hh) { 
		Cell[] ccs = (Cell[]) hh.elementAt(0); 
		vv.mklayCells(ccs);  
		int len = ccs.length;
		int[] lb = new int[len];

		String[] vlb = new String[len];
		for (int i = 0; i < ccs.length; i++) {
			lb[i] = i;
			vlb[i] = ccs[i].ccName;
		}
		if(vv.getLabers() == null)
			vv.setLabers(vlb);
		if(vv.getShowColsIndex() == null)
			vv.setShowColsIndex(lb); 
		ArrayList<JSONObject> alist = new ArrayList<JSONObject>();
		if (hh.size() > 1) {
			for (int i = 1; i < hh.size(); i++) {
				JSONObject jsobj = new JSONObject();
				Object[] o0 = (Object[]) hh.elementAt(i);
				for(int j=0;j<o0.length;j++){
					Object object = o0[j] == null?"":o0[j]; 
					Cell cc = ccs[j];
					if(object instanceof Timestamp){
						object = CCliTool.dateToString(object, true, cc.ccType==91?1:8);
					}else if(object instanceof Date){
						object = CCliTool.dateToString(object, true, 1);
					}
					jsobj.put(ccs[j].ccName, object);
				}
				alist.add(jsobj);
			}
		}
		vv.setValues(alist); 
	}
	
	/**
	 * @param eq
	 * @param sql
	 * @param type
	 * @return
	 * @throws Exception 
	 */
	private Object findSQL(SQLExecQuery eq, String sql, String type) throws Exception {
		if(type.equals(cl.ICL.EQ_queryOne)){
			return eq.queryOne(sql);
		}
		if(type.equals(cl.ICL.EQ_queryRow)){
			return eq.queryRow(sql, false);
		}
		if(type.equals(cl.ICL.EQ_queryCol)){
			return eq.queryCol(sql,100);
		}
		return null;
	}
	/**
	 * 去除中括号
	 * @param varid
	 * @return
	 */
	private String formatBracket(String varid) {
		char bracket = varid.charAt(0);
		int leng = varid.length();
		if ( bracket == '[' ) {
			varid = varid.substring(1,leng-1);
			return varid;
		}
		return varid;
	}
	/**
	 * @param eq
	 * @param assist
	 * @throws Exception 
	 */
	public static  VarObject findValues(SQLExecQuery eq, String assist,String cont,String assistType,String script) throws Exception {
		char _idx = assist.charAt(0);
		assist = assist.replaceAll("\\{", "").replaceAll("\\}", "");
		char x0 = assist.charAt(0);
		if(x0=='$'){
			//常量
			return findCL(eq,assist.substring(1));
		} else if(x0=='&') {
			//辅助
//			return findAid(eq,assist.substring(1),cont,assistType,script);
			return findAidByPage(eq,assist.substring(1),cont, 1, 20,assistType,script);
		}else {
			if(_idx == '{'){
				return findAid(eq,assist,cont,assistType,script);
			}
			VarObject vv = new VarObject();
			if(assist.startsWith("sql:")){
				assist = assist.replaceFirst("sql:", "");
				HVector hh = eq.queryVec(assist, true, 0);
				vv.setTitle("自定义sql查询");
//				MakeLabersAndData(vv, hh);
			}else if(assist.indexOf(";")>0 ||assist.indexOf(":")>0){
//				0:现款;1:指定结账日;2:指定账期
				String[] arrs = assist.split(";");
				ArrayList<JSONObject> alist = new ArrayList<JSONObject>();
				for(int i=0;i<arrs.length;i++){
					String arr = arrs[i];
					String[] ar = arr.split(":");
					JSONObject jsob = new JSONObject();
					jsob.put("code",ar[0]);
					if(ar.length>1){
						jsob.put("name", ar[1]);
					}else{
						jsob.put("name", ar[0]);
					}
					alist.add(jsob);
				}
				vv.setAllCols(new String[]{"code","name"});
				vv.setLabers(new String[]{"code","name"});
				vv.setShowCols(new int[]{0,1});
				if(alist.size()>0){
					vv.setValues(alist);
					vv.setCode(1);
				}else{
					vv.setCode(0);
				}
				vv.setTitle(ConstL);
			}else{
				vv.setCode(-1);
				vv.setMessage("错误的参数");
			}
			return vv;
		}

	}
	/**
	 * @param eq
	 * @param substring
	 * @param cont
	 */
	public static VarObject findAid(SQLExecQuery eq, String var, String cont,String assistType,String script) throws Exception {
		AssistObj sv = (AssistObj) getAsidById(eq, var);
		VarObject vv = new VarObject();
		cont = cont ==null?"":cont;
		if(cont.indexOf("delete")>0 || cont.indexOf("update")>0 || cont.indexOf("insert")>0|| cont.indexOf(";")>0){
			vv.setCode(-1);
			vv.setMessage("出错了!!");
			return vv;
		}
		if (sv != null) {
			String sql = sv.getSlink();
			//CGroupEditor  辅助
			if(assistType !=null && assistType.equals(ASSIST_GROUP+"")){
				String s1="";
				String sld = sql;//;--select ..... where [[#]字段名]...
				int x0 = sld.indexOf('[');
				int x1 = sld.indexOf(']', x0 + 1), t1 = script == null ? 0 : script.length();
				sql = sld.substring(x0 + 1, x1);
				boolean b0 = sql.charAt(0) == '#';//;-数值
				if (t1 < 1 || (t1 == 1 && script.charAt(0) == '*'))
					s1 = "1>0";//取所有值。
				else
					s1 = CCliTool.toBlurCond(b0 ? sql.substring(1) : sql, b0 ? Types.INTEGER : Types.VARCHAR, script);
				sql = sld.substring(0, x0) + s1 + sld.substring(x1 + 1);
				sv.setSlink(sql);
			}
			
			
			sql = sql.replaceAll("\\[!\\]", wour.ORGCODE);
			sql = sql.replaceAll("\\[#\\]", wour.CMCODE);
			sql = sql.replaceAll("\\[$\\]", wour.USRCODE);
			sv.setSlink(sql);
			String bForSql = cont.length()>0 ?sv.makeQuery(cont):sql;
			bForSql = SSTool.formatVarMacro(bForSql, eq);
//			bForSql = bForSql.replaceAll("\\r", "");
//			_log.info(bForSql);
			bForSql = formartSql(bForSql, cont);
			bForSql = SSTool.formatVarMacro(bForSql, eq);
			_log.info(bForSql);
			HVector hh = eq.queryVec(bForSql, true, 0);
//			MakeLabersAndData(vv, hh);
			vv.setTitle(sv.getSlable());
			int showSize = vv.getShowCols().length;
			int realShowSize = 0;
			if (sv.getShowCel_idx().length > 1) {
				realShowSize = sv.getShowCel_idx().length;
				int[] _idx = sv.getShowCel_idx();
				if (realShowSize == showSize) {
					if (sv.getShowCel_lable() != null) {
						vv.setLabers(sv.getShowCel_lable());
					}
				} else {
					if (sv.getShowCel_lable() != null) {
						vv.setLabers(sv.getShowCel_lable());
					} else {
						String[] showlabes = new String[realShowSize];
						for (int i = 0; i < realShowSize; i++) {
							showlabes[i] = vv.getAllCols()[_idx[i]];
						}
						vv.setLabers(showlabes);
					}
				}
				vv.setShowCols(_idx);
			} else {
				if (sv.getShowCel() != null) {
					realShowSize = sv.getShowCel().length;
					if (sv.getShowCel_lable() != null) {
						vv.setLabers(sv.getShowCel_lable());
					} else {
						vv.setLabers(sv.getShowCel());
					}
					if (realShowSize != showSize) {
						String[] allcel = vv.getAllCols();
						String[] realshcel = sv
								.getShowCel();
						int[] _idx = new int[realShowSize];
						for (int j = 0; j < realShowSize; j++) {
							for (int i = 0; i < showSize; i++) {
								String ce2 = realshcel[j];
								String ce1 = allcel[i];
								if (ce1.equals(ce2)) {
									_idx[j] = i;
								}
							}
						}
						vv.setShowCols(_idx);
					}
				}
			}
		} else {
			vv.setCode(-1);
			vv.setMessage("不存在辅助：" + var);
		}
		return vv;
	}
	
	/**
	 * @param bForSql
	 * @param sv
	 */
	private static String[] makePageSql(int dateType,String sql, AssistObj sv,int startPage,int pageSize) {
		String orderby = "",groupby = "";
		String totalSql="select count(*) ";
		sv.setSlink(sql);
		int from = sql.indexOf("from");
		String fromStr = sql.substring(from);
		from = fromStr.indexOf("order by");
		if(from>0)
			fromStr = fromStr.substring(0,from);
		totalSql = totalSql+" "+fromStr;
		int _order = sql.indexOf("order by");
		if(_order>0)
			sql = sql.substring(0,_order);
		if(_order>0){
			orderby = sv.getOrderBy();
		}else{
			orderby = "order by "+sv.getShowCel()[0];
		}
		int _group = sql.indexOf("group by");
		if(_group>0){
			groupby = sv.getGroupBy();
		}
		String pageSql="",topPageSql="";
		String[] cols = sv.getCols();
		int len = sv.type==2?cols.length-4:cols.length;
		for(int i=0;i<len;i++){
			String f1 = cols[i];
			int _idx = f1.indexOf(".");
			if(_idx>0){
				f1 = f1.substring(_idx+1);
			}
			pageSql+=" "+f1+",";
		}
		topPageSql = pageSql;
		if(sv.type==2) {
			topPageSql = pageSql;
			pageSql+=" hsunit,hsunit as unit,1.0 as qtyrt,nup,";
			topPageSql +=" hsunit,unit,qtyrt,nup,";
		} 
		String pages="";
		if(dateType == 4){
			pages = sql+" "+sv.getOrderBy() +" limit "+((startPage-1)*pageSize)+","+(pageSize);
		}else{
			pageSql = pageSql.substring(0,pageSql.length()-1); 
			topPageSql = topPageSql.substring(0,topPageSql.length()-1);
			if(sql.indexOf("distinct")!=-1){
				int a = sql.indexOf("from");
				String one = sql.substring(0,a);
				String two = sql.substring(a,sql.length());
				sql = one +",row_number() over("+orderby+") _r "+two;
			}else{
				if(sv.type==2) {
					sql = "select "+pageSql+" from gdic "+sv.getWhereStr();
				}
				orderby = "select row_number() over("+orderby+") _r,";
				sql = sql.replace("select", orderby);
			}
//			sql = sql.replace("select", orderby);
			String fy = "where _r>="+((startPage-1)*pageSize+1)+" and _r<="+(startPage*pageSize);
			pages="select "+topPageSql+" from ("+sql+") b "+fy+" "+groupby+" "+sv.getOrderBy();
		}
		return new String[]{totalSql,pages};
	}
	/**
	 * @param eq
	 * @param substring
	 * @param cont
	 */
	public static VarObject findAidByPage(SQLExecQuery eq, String var, String cont,int startPage,int PageSize,String assistType,String script) throws Exception {
		AssistObj sv = (AssistObj) getAsidById(eq, var);
		VarObject vv = new VarObject();
		cont = cont ==null?"":cont;
		if (sv != null) {
			String sql = sv.getSlink();
			//CGroupEditor  辅助
			if(assistType !=null && assistType.equals(ASSIST_GROUP+"")){
				String s1="";
				String sld = sql;//;--select ..... where [[#]字段名]...
				int x0 = sld.indexOf('[');
				int x1 = sld.indexOf(']', x0 + 1), t1 = script == null ? 0 : script.length();
				sql = sld.substring(x0 + 1, x1);
				boolean b0 = sql.charAt(0) == '#';//;-数值
//				if (t1 < 1 || (t1 == 1 && script.charAt(0) == '*'))
//					s1 = "1>0";//取所有值。
//				else
					s1 = CCliTool.toBlurCond(b0 ? sql.substring(1) : sql, b0 ? Types.INTEGER : Types.VARCHAR, script);
				sql = sld.substring(0, x0) + s1 + sld.substring(x1 + 1);
				sv.setSlink(sql);
			}
			
			
			sql = SSTool.formatVarMacro(sql, eq);
			sql = sql.replaceAll("\\[!\\]", wour.ORGCODE);
			sql = sql.replaceAll("\\[#\\]", wour.CMCODE);
			sql = sql.replaceAll("\\[$\\]", wour.USRCODE);
			
			sv.setSlink(sql);
			String bForSql = cont.length()>0 ?sv.makeQuery(cont):sv.getSlink();
			bForSql = formartSql(bForSql, cont);
			bForSql = SSTool.formatVarMacro(bForSql, eq);
			_log.info(bForSql);
			String dblink = "";
			if(bForSql.startsWith("@")){
				bForSql = bForSql.substring(1);
				int ii1 = bForSql.indexOf("@");
				if(ii1>0){
					dblink = bForSql.substring(0,ii1);
					_log.info(bForSql);
					bForSql = bForSql.substring(ii1+1);
				}
			}			
			int dateType = eq.db_type;
			String[] sqls = makePageSql(dateType,bForSql,sv,startPage,PageSize);
			int total=0;
			HVector hh=null;
			if(dblink.length()>0){
				SQLExecQuery eq1 = eq.getDBLNKVAR(
						dblink, false);
				total = CCliTool.objToInt(eq1.queryOne(sqls[0]),0);
				_log.info(sqls[0]);
				_log.info(sqls[1]);
				hh = eq1.queryVec(sqls[1], true, 0);
				eq1.close();
			}else {
				_log.info(sqls[0],sqls[1]);
				total = CCliTool.objToInt(eq.queryOne(sqls[0]),0);
				hh = eq.queryVec(sqls[1], true, 0);
			}			
			vv.setTotal(total);
			vv.setSize(PageSize);

//			MakeLabersAndData(vv, hh);
			//GDIC  辅助查询 单位信息
			if(assistType !=null && assistType.equals(ASSIST_GDIC+"")){
				for (int i = 0; i < vv.getValues().size(); i++) {
					Iterator<String> keys = vv.getValues().get(i).keySet().iterator();// jsonObject.keys();
					String key ="";
			        if (keys.hasNext()){ 
			            key = keys.next();
			        }
			        String sqlUnit = "select g.bzunit,g.hsgx,u.umc from gdichs g inner join udic u on u.ubm = g.bzunit  where g.gbm='" + vv.getValues().get(i).get(key) + "'";
			        HVector hv = eq.queryVec(sqlUnit);
			        if(hv == null)
			        	hv = new HVector();
			        JSONArray jsonArr = new JSONArray();
			        for (int j = 0; j < hv.size(); j++) {
			        	Object[] object = (Object[]) hv.elementAt(j);
			        	JSONObject jsonObject = new JSONObject();
						jsonObject.put("bzunit", object[0]);
						jsonObject.put("hsgx", object[1]);
						jsonObject.put("name", object[2]);
						jsonArr.add(jsonObject);
					}
			        if(jsonArr != null)
					vv.getValues().get(i).put("ghsunit", JSON.toJSON(jsonArr));
				}
			}
			vv.setTitle(sv.getSlable());
			int showSize = vv.getShowCols().length;
			int realShowSize = 0;
			if (sv.getShowCel_idx().length > 1) {
				realShowSize = sv.getShowCel_idx().length;
				int[] _idx = sv.getShowCel_idx();
				if (realShowSize == showSize) {
					if (sv.getShowCel_lable() != null) {
						vv.setLabers(sv.getShowCel_lable());
					}
				} else {
					if (sv.getShowCel_lable() != null) {
						vv.setLabers(sv.getShowCel_lable());
					} else {
						String[] showlabes = new String[realShowSize];
						for (int i = 0; i < realShowSize; i++) {
							showlabes[i] = vv.getAllCols()[_idx[i]];
						}
						vv.setLabers(showlabes);
					}
				}
				vv.setShowCols(_idx);
			} else {
				if (sv.getShowCel() != null) {
					realShowSize = sv.getShowCel().length;
					if (sv.getShowCel_lable() != null) {
						vv.setLabers(sv.getShowCel_lable());
					} else {
						vv.setLabers(sv.getShowCel());
					}
					if (realShowSize != showSize) {
						String[] allcel = vv.getAllCols();
						String[] realshcel = sv
								.getShowCel();
						int[] _idx = new int[realShowSize];
						for (int j = 0; j < realShowSize; j++) {
							for (int i = 0; i < showSize; i++) {
								String ce2 = realshcel[j];
								String ce1 = allcel[i];
								if (ce1.equals(ce2)) {
									_idx[j] = i;
								}
							}
						}
						vv.setShowCols(_idx);
					}
				}
			}
		} else {
			vv.setCode(-1);
			vv.setMessage("不存在辅助：" + var);
		}
		return vv;
	}
	/**
	 * 根据辅助名称查询辅助信息
	 * @param eq  数据库连接
	 * @param id  辅助名称
	 * @return
	 */
	public static Object getAsidById(SQLExecQuery eq, String id) {
		String sql = "select slabel,slink,sflag,sclass from insaid where (sclass='inetbas.cli.cutil.CSelectEditor' or sclass='inetbas.cli.cutil.CGroupEditor' or sclass='inetbas.cli.cutil.CDynaEditor' or sclass='ebas.cutil.CGDicEditor' ) and sid='"
				+ id + "'";
		try {
			Object[] o0 = eq.queryRow(sql, false);
			if (o0 != null) {
				AssistObj ass = new AssistObj();
				ass.setSid(id);
				ass.setSlable(CCliTool.objToString(o0[0]));
				ass.setSclass(CCliTool.objToString(o0[3]));
				ass.mklables(CCliTool.objToString(o0[2]));
				
				sql=CCliTool.objToString(o0[1]);
				ass.setSlink(sql);
				return ass;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @param eq
	 * @param substring
	 * @throws Exception 
	 */
	public static  VarObject findCL(SQLExecQuery eq, String substring) throws Exception {
		String sv = SSTool.loadConst1(eq,substring);
		VarObject vv = new VarObject();
		char c0 ;
		if (sv != null) {
			c0 = sv.charAt(0);
			int lens = sv.length();
			if (c0 == '{') {
				sv = sv.substring(1, lens - 1);
				String[] vals = sv.split(";");
				ArrayList<JSONObject> alist = new ArrayList<JSONObject>();
				for (int i = 0; i < vals.length; i++) {
					String idv = vals[i];
					String[] bb = idv.split(":");
					JSONObject jsob = new JSONObject();
					if(bb.length<=1){
						jsob.put("name", "");
					}else{
						jsob.put("name", bb[1]);
					}
					jsob.put("code",bb[0]);
					alist.add(jsob);
				}
				vv.setAllCols(new String[]{"code","name"});
				vv.setLabers(new String[]{"code","name"});
				vv.setShowCols(new int[]{0,1});
				vv.setValues(alist);
				vv.setCode(1);
				vv.setTitle(ConstL);
			}
			if (sv.startsWith("select")) {
				sv = sv.replace("{_}", "1=1");
				sv = SSTool.formatVarMacro(sv, eq);
				_log.info("常量查询："+sv);
				HVector hh = eq.queryVec(sv, true, 0);
				vv.setTitle(ConstL);
//				MakeLabersAndData(vv, hh);
				_log.info(vv.getCode()+"");
			}
		} else {
			vv.setCode(-1);
			vv.setMessage("不存在常量：" + substring);
		}
		
		return vv;
		
	}
	
	
	/***
	 * SQL语句格式化
	 * @param bForSql
	 * @param cont
	 * @return
	 */
	public static String formartSql(String bForSql, String cont) {
		if (bForSql.startsWith("[0~]")) {
			bForSql = bForSql.replace("[0~]", "");
		}
		if (bForSql.startsWith("[0+]")) {
			bForSql = bForSql.replace("[0+]", "");
		}
		if (bForSql.startsWith("#[0~]")) {
			bForSql = bForSql.replace("#[0~]", "");
		}
		if (bForSql.startsWith("#[0+]")) {
			bForSql = bForSql.replace("#[0+]", "");
		}
		if (bForSql.startsWith("0~")) {
			bForSql = bForSql.replace("0~", "");
		}
		bForSql = bForSql.replace("{_}", "1=1");
		if (bForSql.indexOf("&select") > 0) {
			int c0 = bForSql.indexOf("&");
			bForSql = bForSql.substring(c0 + 1);
			bForSql = makeFindCont(bForSql, cont);
		}
		bForSql = makeFindCont(bForSql, cont);
		return bForSql;
	}

	private static String makeFindCont(String bForSql, String cont) {
		int c0;
		c0 = bForSql.indexOf("[");
		if (c0 > 0) {
			StringBuffer sb = new StringBuffer();
			String sqlhf = bForSql.substring(0, c0);
			String sqlhf1 = bForSql.substring(c0 + 1);
			int c1 = sqlhf1.indexOf("]");
			sb.append(sqlhf);
			if (!sqlhf1.startsWith("*")) {
				String confld = sqlhf1.substring(0, c1);
				if(cont.length()>0)
					confld += "='" + cont + "' ";
				else {
					confld+="<>''";
				}
				sb.append(confld);
				sqlhf1 = sqlhf1.substring(c1 + 1);
				sb.append(sqlhf1);
				return sb.toString();
			}
		}
		return bForSql;
	}
	
	/***
	 * 根据辅助ID查找辅助信息
	 * @param eq 数据库连接信息
	 * @param id 辅助ID
	 * @return 辅助对象
	 * 2019-07-08 09:42:55
	 * @throws Exception 
	 */
	public static BipInsAidNew getBipInsAidInfoById(SQLExecQuery eq,String id) throws Exception {
		//从辅助表中根据辅助ID,查出对应的辅助定义
		BipInsAidNew bipInsAid = null;
		String sql = "select slabel,slink,sflag,sclass from insaid where sid='"+ id + "'";
		Object[] o0 = eq.queryRow(sql, false);
		if (o0 != null) {
			bipInsAid = new BipInsAidNew();
			bipInsAid.setTitle(CCliTool.objToString(o0[0]));//标题
			bipInsAid.setSlink(CCliTool.objToString(o0[1]));//初始SQL
			bipInsAid.setSflag(CCliTool.objToString(o0[2]));
			bipInsAid.mklables(bipInsAid.getSflag());////显示列下标//显示列名称//列标签   ---- 辅助中的标识
			String sclass = CCliTool.objToString(o0[3]);//类名
			initBipAidType(bipInsAid, sclass);
		}
		return bipInsAid;
	}

	public static void initBipAidType(BipInsAidNew bipInsAid, String sclass) {
		if(sclass.endsWith("CSelectEditor")) {
			bipInsAid.setbType(BipInsAidType.CSelectEditor);
		}else if(sclass.endsWith("CGroupEditor")){
			bipInsAid.setbType(BipInsAidType.CGroupEditor);
		}else if(sclass.endsWith("CFlagEditor")){
			bipInsAid.setbType(BipInsAidType.CFlagEditor);
		}else if(sclass.endsWith("CDateEditor")){
			bipInsAid.setbType(BipInsAidType.CDateEditor);
		}else if(sclass.endsWith("CQueryEditor")){
			bipInsAid.setbType(BipInsAidType.CQueryEditor);
		}else if(sclass.endsWith("CHSMEditor")){
			bipInsAid.setbType(BipInsAidType.CHSMEditor);
		}else if(sclass.endsWith("CFlowEditor")){
			bipInsAid.setbType(BipInsAidType.CFlowEditor);
		}else if(sclass.endsWith("CDynaEditor")){
			bipInsAid.setbType(BipInsAidType.CDynaEditor);
		}else if(sclass.endsWith("CUpDownEditor")){
			bipInsAid.setbType(BipInsAidType.CUpDownEditor);
		}else if(sclass.endsWith("CYMEditor")){
			bipInsAid.setbType(BipInsAidType.CYMEditor);
		}else if(sclass.endsWith("CGDicEditor")){
			bipInsAid.setbType(BipInsAidType.CGDicEditor);
		}
	}

}
