package inetbas.web.outsys.api;

import inet.HVector;
import inetbas.cli.cutil.CCliTool;
import inetbas.pub.coob.Cell;
import inetbas.pub.coob.Cells;
import inetbas.pub.ojc.CExcel;
import inetbas.serv.csys.DBInvoke;
import inetbas.sserv.SGBVar;
import inetbas.sserv.SQLExecQuery;
import inetbas.sserv.SSTool;
import inetbas.web.outsys.entity.PageLayOut;
import inetbas.web.outsys.tools.CellsUtil;
import inetbas.web.outsys.tools.ServScript;
import inetbas.webserv.WAORGUSR;
import inetbas.webserv.WebAppPara;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cl.ICL;

import com.alibaba.fastjson.JSONObject;


/**
 * @author www.bip-soft.com
 *
 */
public class WebApiInvoke extends DBInvoke {
	private static Logger _log = LoggerFactory.getLogger(WebApiInvoke.class);

	public static final int API_FIND_DATA = 200;
	public static final int API_FIND_DATACount = 201; // 统计取数
	public static final int API_Count = 300; // 取统计条件（按照菜单参数ID，常量中定义WB.菜单参数ID）

	public static final int API_ASSISTTYPE = 500;// 获取辅助类型

	public static final int API_CountFLD = 301; // 取统计条件（按照菜单参数ID，常量中定义WB.菜单参数ID）
	public static final int API_CountSbds = 302; //获取常量  根据名称获取常量公式 不做任何业务逻辑处理
	public static final int API_DLGSQLRUN = 304; //获取常量中的DLG 弹出框按钮执行

	public static final int API_InitCelInc = 100;
	public static final int API_exportExcel = 400;
	
	
	private static SQLExecQuery _eq;
	private static WAORGUSR userWaorgusr;
	public Object processOperator(SQLExecQuery eq, WebAppPara wa)
			throws Exception {
		int id = wa.oprid;
		_eq = eq;
		userWaorgusr = wa.orgusr;
		Object[] param = wa.params;
		if (id == API_FIND_DATA) {
			PageLayOut page = (PageLayOut) wa.params[1];
			return find(eq, (Cells) param[0], page);
		}else if (id == API_FIND_DATACount){
			PageLayOut page = (PageLayOut) wa.params[1];
			Cells cells = (Cells)wa.params[0];
			return findCountData(eq,cells,page,(String)wa.params[2],(String)wa.params[3]);
		}else if (id == API_Count) {
			String sbuid = (String)wa.params[0];
			Cells buidCells = (Cells)wa.params[1];
			return getTJCellsAndFlds(eq,sbuid,buidCells);
		}else if (id == API_CountFLD) {
			String sbuid = (String)wa.params[0];
			return getTJFlds(eq,sbuid);
		}else if (id == API_CountSbds) {
			String sname = (String)wa.params[0];
			return getContentSbds(eq,sname);
		}else if (id == API_InitCelInc) {
			Cells cell = (Cells)wa.params[0];
			for (int i = 0; i < cell.db_cels.length; i++) {
				Cell cc = cell.db_cels[i];
				if ((cc.attr & Cell.AUTOINC) != 0) {
					InitCellAutoInc(cell, cc);
				}
				if((cc.ccType == Types.DECIMAL ||cc.ccType == Types.NUMERIC) &&cc.ccPoint<0){
					int poit = -cc.ccPoint;
					poit = CCliTool.objToInt(SGBVar.getGBVar(_eq, "PNT."+poit),2);
					cc.ccPoint = poit;
				}
			}
			initsubCellPoit(cell);
			return cell;
		}else if(id == API_exportExcel){
			PageLayOut page = (PageLayOut) wa.params[1];
			return exportFile(eq, (Cells) param[0], page);
		}else if(id == API_ASSISTTYPE){
			String editName = CCliTool.objToString(wa.params[0]);
			return assistType(eq,editName);
		}else if(id == API_DLGSQLRUN){
			String jsonStr = (String)wa.params[0];
			String btnInfo = (String)wa.params[1];
			return runDLGA(eq,jsonStr,btnInfo);
		}
		return null;
	}


	/**
	 * @param eq
	 * @param cells
	 * @param page
	 * @return
	 * @throws Exception 
	 */
	public  static  String exportFile(SQLExecQuery eq, Cells cells, PageLayOut page) throws Exception {
		HVector hh = findValues(eq, cells, page);
		hh.insertElementAt(getCellTitle(cells), 0);
		String dir = WebUPDFileService.getFileDir(eq.db_id, true);
		String filesString = dir+""+WebUPDFileService.getNows()+".xls";
		OutputStream out = new FileOutputStream(filesString);
		CExcel.expExcel(out,hh,null,cells.all_cels,true,false);
		out.close();
		_log.info(filesString);
		return filesString;
	}
	
	public static Object[] getCellTitle(Cells cells){
		Object[] o0 = new Object[cells.all_cels.length];
		for(int i=0;i<cells.all_cels.length;i++){
			o0[i] = cells.all_cels[i].labelString;
		}
		return o0;
	}


	/**
	 * 获取统计字段
	 * @param eq
	 * @param sbuid
	 * @return
	 */
	private Object getTJFlds(SQLExecQuery eq, String sbuid) {
		try {
			Object clsdbs = eq.queryOne("select sbds,cid from inssyscl where sname='WB."+sbuid+"'");
			//xh.sorg;xha.qty,xha.fcy;bar;order by sid;100|xh.sorg;xha.qty,xha.fcy;bar;order by sid;100
			String[] grpfld = null,sumflds = null;
			String chartType,width="";
			Object[] retuObj = null;
			if(clsdbs!=null){
				String[] strArr = CCliTool.objToString(clsdbs).split("\\|");
				retuObj=new Object[strArr.length];
				for (int j = 0; j < strArr.length; j++) { 
					String one = strArr[j];
					String[] flds = CCliTool.objToString(one).split(";");
					grpfld = flds[0].split(",");
					sumflds = flds[1].split(",");
					chartType = flds[2]; 
					if(flds.length >=4)
						width = flds[3];
					else {
						width="100";
					}
					ArrayList<String> grplist = new ArrayList<String>();
					ArrayList<String> sumArrayList = new ArrayList<String>();
					for (int i=0; i<grpfld.length; i++) {
						String _fld1 = grpfld[i];
						grplist.add(_fld1);
					}
					for (int i=0; i<sumflds.length; i++) {
						String _fld1 = sumflds[i];
						sumArrayList.add(_fld1);
					}
					retuObj[j] = new Object[]{grplist,sumArrayList,chartType,width}; 
				}
			}else{
				return null;
			} 
			return retuObj;
		} catch (Exception e) {
			_log.error("获取初始化统计图表配置出错:",e);
		}
		return null;
	}
	/**
	 * 根据常量名称查询常量公式
	 * @param eq
	 * @param sname
	 * @return
	 */
	private Object getContentSbds(SQLExecQuery eq, String sname){
		try {
			Object clsdbs = eq.queryOne("select sbds from inssyscl where sname='"+sname+"'");
			if(clsdbs!=null){
				return clsdbs;
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	} 

	/**
	 * 执行弹出框按钮
	 * @param eq
	 * @param sname
	 * @return
	 */
	private Object runDLGA(SQLExecQuery eq, String jsonstr,String btnInfo){
		Map<String, String> ret = new HashMap<String, String>();
		try {
			JSONObject str = JSONObject.parseObject(jsonstr);
			JSONObject btn = JSONObject.parseObject(btnInfo); 
			String sqlSel = "select sbds from inssyscl where sname='"+btn.getString("dlgSname")+"' and sbds like 'A:"+btn.getString("name")+"%'";
			String sbds = CCliTool.objToString(eq.queryOne(sqlSel));
			if(sbds.lastIndexOf("&") == sbds.length()-1)
				sbds = sbds.substring(0,sbds.length()-1);
			String[] strArr = sbds.split(";");
			String upsql = strArr[2];
			while (upsql.indexOf("@") !=-1) { 
				String sql_ = upsql.substring(upsql.indexOf("@")+1,upsql.length());
				String[] arr = sql_.split("");
				String key = "";
				for (int i = 0; i < arr.length; i++) {
					if(arr[i] !=""){
						key+=arr[i];
					}else{
						break;
					}
				}
				String val = str.getString(key);
				upsql = upsql.replace("@"+key, "'"+val+"'");
			}
			int num = eq.exec(upsql); 
			
			String msg0 = strArr[3];
			String msg1 = strArr[4];
			String v = num+"";
			if(msg0.startsWith(v)){
				String[] cc = msg0.split(":");
				ret.put("msg", cc[1]);
				ret.put("state", "0");
			}else {
				String[] cc = msg1.split(":");
				ret.put("msg", cc[1]);
				ret.put("state", "1");
			} 
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return ret;
		}
	}
	
	private void initsubCellPoit(Cells cell) throws Exception {
		for(int i = 0;i<cell.getChildCount();i++){
			Cells subc = cell.getChild(i);
			for (int j = 0; j < subc.db_cels.length; j++) {
				Cell cc = subc.db_cels[j];
				if(cc.ccPoint<0){
					int poit = -cc.ccPoint;
					poit = CCliTool.objToInt(SGBVar.getGBVar(_eq, "PNT."+poit),2);
					cc.ccPoint = poit;
				}
			}
			initsubCellPoit(subc);
		}
	}


	private static void InitCellAutoInc(Cells cell, Cell cc) throws Exception {
		cell.autoInc = 0;
		String s0 = CCliTool.trimSpace(cc.initValue, false);
		int t0 = s0 != null ? s0.length() - 1 : -1;
		int t1 = t0 > 2 && s0.charAt(0) == '[' ? s0.indexOf(']') : 0;
		String s1 = "";
		if (t1 > 0) {
			s1 = s0.substring(1, t1);
			if (CCliTool.isCode(s1)) {
				String s2 = SGBVar.getGBVar(_eq, s1);// ;--全局变量只能是合法的编码,不存在时当成宏定义。
				if (s2 != null && s2.length() > 0) {
					s0 = s2 + s0.substring(t1 + 1);// ;--附加内容
					t0 = s0.length() - 1;
				}
			}
//			s0 = s1;
		}
		cc.initValue = s0;
		CCliTool.inc_Init(cell, cc); // 自增关联
		s1 = s0 = cc.initValue;
		t0 = cell.autoInc;
		t1 = cc.index + 1;
		if ((cc.attr & Cell.PRIMARY) != 0)
			cell.autoInc = (t0 & 0xFF00) | t1;// ;--替换主键(低8位)
		else {
			cell.autoInc = (t0 & 0xFF) | (t1 << 8);// ;--替换其它项(8－16)
			cc.attr |= Cell.AUTOREF;
		}
		if(s0 != null){
			if(s0.charAt(0)=='['){
				s0 = s0.substring(1,s0.length()-1);
			}
			if(s0.indexOf("!")>0)
				s0 = s0.replaceAll("\\[!\\]", userWaorgusr.ORGCODE);
			if(s0.indexOf("#")>0)
				s0 = s0.replaceAll("\\[#\\]", userWaorgusr.CMCODE);
			if(s0.indexOf("$")>0)
				s0 = s0.replaceAll("\\[$\\]", userWaorgusr.USRCODE);
		}
		Calendar c = Calendar.getInstance();
		int y = c.get(Calendar.YEAR);
		int m = c.get(Calendar.MONTH)+1;
		int day = c.get(Calendar.DATE);
		if(s0 != null){
			if(s0.indexOf("[Y2M]")>0){
				y = y%100;
				String mon = m<10?("0"+m):m+"";
				s0 = s0.replaceAll("\\[Y2M\\]",y+mon);
			}
			if(s0.indexOf("[YM]")>0){
				String mon = m<10?("0"+m):m+"";
				s0 = s0.replaceAll("\\[Y2M\\]",y+mon);
			}
			if(s0.indexOf("[Y]")>0){ 
				s0 = s0.replaceAll("\\[Y\\]",y+"");
			}
			if(s0.indexOf("[YMD]")>0){
				String mon = m<10?("0"+m):m+"";
				String dSt = day<10?"0"+day:day+"";
				s0 = s0.replaceAll("\\[Y2M\\]",y+mon+dSt);
			}
		}
		cc.psAutoInc = (s0 == null ? "" : s0) + "\r" + s1;
	}
	
	/**
	 * @param eq 数据库连接对象
	 * @param sbuid 菜单参数id
	 * @param buidCells
	 * @return
	 * @throws Exception 
	 */
	private Object getTJCellsAndFlds(SQLExecQuery eq, String sbuid,
			Cells buidCells) throws Exception {
		Object clsdbs = eq.queryOne("select sbds,cid from inssyscl where sname='WB."+sbuid+"'");
		//xh.sorg;xha.qty,xha.fcy;bar
		String[] grpfld = null,sumflds = null;
		String chartType="",grpStr="",sumStr="";
		if(clsdbs!=null){
			String[] flds = CCliTool.objToString(clsdbs).split(";");
			grpStr = flds[0];
			sumStr = flds[1];
			grpfld = flds[0].split(",");
			sumflds = flds[1].split(",");
			chartType = flds[2];
		}else{
			return null;
		}
		ArrayList<String> grplist = new ArrayList<String>();
		ArrayList<String> sumArrayList = new ArrayList<String>();
//		String _grplistIndexStr = "",_sumlistIndexStr="";
		for (int i=0; i<grpfld.length; i++) {
			String _fld1 = grpfld[i];
//			int _index = CellsUtil.getCellIndexByName(_fld1,buidCells);
			grplist.add(_fld1);
//			_grplistIndexStr+=_index+",";
		}
//		_grplistIndexStr = _grplistIndexStr.substring(0,_grplistIndexStr.length()-1);
		for (int i=0; i<sumflds.length; i++) {
			String _fld1 = sumflds[i];
//			int _index = CellsUtil.getCellIndexByName(_fld1,buidCells);
			sumArrayList.add(_fld1);
//			_sumlistIndexStr+=_index+",";
		}
//		_sumlistIndexStr = _sumlistIndexStr.substring(0,_sumlistIndexStr.length()-1);
		Cells countCell = (Cells) buidCells.clone(true);
		Object[] o0 = CellsUtil.makeCellsCell(countCell,grpStr,sumStr,buidCells);
		countCell = (Cells)o0[0];
		return new Object[]{countCell,grplist,sumArrayList,chartType,o0[1],o0[2]};
	}

	/**
	 * @param eq
	 * @param cells
	 * @param page
	 * @param string
	 * @param string2
	 * @return
	 */
	private Object findCountData(SQLExecQuery eq, Cells cells, PageLayOut page, String fild, String sumfilds) throws Exception{
		String sql0 = spelSQL(eq, cells, 0, page.queryCriteria, true, null);
		_log.info(sql0);
		String caseStr = "";
		HVector hh = new HVector(); 
		String s0 = sql0;
		while (s0.indexOf("case")>-1) {
			s0 = s0.substring(s0.indexOf("case "));
			int _when = s0.indexOf("when ");
			caseStr = s0.substring(4,_when).trim();
			if(_when>-1 && !hh.contains(caseStr)){
				hh.addElement(caseStr);
			}
			s0 = s0.substring(_when+5);
			
		}
		String sqlselect = sql0.substring(0, sql0.indexOf(" "));
		String sqlform = sql0.substring(sql0.indexOf("from"));
		String sqlfilds = sql0.substring(sql0.indexOf(" "),sql0.indexOf("from"));
		sqlfilds=makefildesAsF1(sqlfilds);
		if(sqlform.indexOf(" order by ")>-1)
			sqlform = sqlform.substring(0, sqlform.indexOf(" order by "));
		if(sqlform.indexOf(" group by ")>-1){
			caseStr = "";
			for(int i=0;i<hh.size();i++){
				caseStr+=","+hh.elementAt(i).toString();
			}
			sqlform += caseStr;
		}
		if(cells.orderby!=null && !cells.orderby.equals("")){
			sql0 = sqlselect +" "+fild+","+sumfilds+" from ("+ sqlselect+"  "+sqlfilds+","+cells.orderby+" as selob "+sqlform+") b"+" group by "+fild +" order by b.selob";
		}else{
			sql0 = sqlselect +" "+fild+","+sumfilds+" from ("+ sqlselect+" "+sqlfilds+" "+sqlform+") b"+" group by "+fild +" order by " +fild;
		}
		sql0 = SSTool.formatVarMacro(sql0, eq);
		_log.info(sql0);
		HVector queryRes = eq.queryVec(sql0);
		for (int i = 0; queryRes != null&&i < queryRes.size(); i++) {
			Object[] ovsObjects = (Object[]) queryRes.elementAt(i);
			for(int j=0 ;j < ovsObjects.length ; j++){
				Object ov = ovsObjects[j];
				Cell c1 = cells.all_cels[j];
				Object vv =  ov== null ? "": ov;
				if (ov instanceof Timestamp){
					if(c1.ccType == 91){
						vv = CCliTool.dateToString(ov, true, cl.ICL.DF_YMD);
					}else{
						vv = CCliTool.dateToString(ov, true, cl.ICL.DF_YMDHM);
					}
				}
				ovsObjects[j] = vv;
			}
			queryRes.setElementAt(ovsObjects, i);
		}
		ArrayList<JSONObject> resList = new ArrayList<JSONObject>();
		resList = makeJsonArray(cells, queryRes);
		page.celData = resList;
		page.setTotalSize(resList.size());resList.size();
		return page;
	}


	private static ArrayList<JSONObject> makeJsonArray(Cells cells, HVector queryRes) {
		ArrayList<JSONObject> resList = new ArrayList<JSONObject>();
		if(queryRes!=null){
			for (int i = 0; i < queryRes.size(); i++) {
				Object[] res = (Object[]) queryRes.elementAt(i);
				JSONObject jsonObject = new JSONObject();
				for(int j=0;j<res.length;j++){
					Object oo = res[j];
					if (oo instanceof Timestamp || oo instanceof Date){
						if(cells.all_cels[j].ccType == 91){
							oo = CCliTool.dateToString(oo, true, cl.ICL.DF_YMD);
						}else{
							oo = CCliTool.dateToString(oo, true, cl.ICL.DF_YMDHM);
						}
					}
					jsonObject.put(cells.all_cels[j].ccName, oo);
				}
				resList.add(jsonObject);
			}
		}
		return resList;
	}

	public static PageLayOut find(SQLExecQuery eq, Cells cell,
			PageLayOut page) throws Exception {
		String sql0 = spelSQL(eq, cell, 0, page.queryCriteria, true, null);
		String orderByString = "";
		if (sql0.indexOf(" order by") > -1) {
			orderByString = sql0.substring(sql0.indexOf(" order by "));
			orderByString = orderByString.substring(orderByString.indexOf("by") + 3);
			sql0 = sql0.substring(0, sql0.indexOf(" order by "));
		}
		String sqlselect = sql0.substring(0, sql0.indexOf(" "));
		String sqlform = sql0.substring(sql0.indexOf("from"));
		String sqlfilds = sql0.substring(sql0.indexOf(" "), sql0.indexOf("from"));

		int ii = cell.getParent()==null? cell.fkcc:(cell.pkcc-1);
		Cell cc = cell.all_cels[ii>=0?ii:0];
		if (orderByString.length()>0 && orderByString.indexOf(".")<0){
			orderByString = (cell.exTbName==null? orderByString : (cell.exTbName+"."+orderByString));
		}
		String orderByStr = page.orderBy.length() > 0 ? page.orderBy
				: (orderByString.length() > 0 ? orderByString : cc.ccName);
		int startNum = 0, endNum = 0;
		startNum = page.pageSize * (page.currentPage - 1);
		endNum = page.pageSize * page.currentPage;
		String queryCount = sqlselect + " count(*) from ("+sqlselect+" "+makefildesAsF1(sqlfilds)+" "+ sqlform+") b";
		queryCount = SSTool.formatVarMacro(queryCount, eq);
		int count = CCliTool.objToInt(eq.queryOne(queryCount), 0);
		page.setTotalSize(count);
//		ArrayList<JSONObject> resList = new ArrayList<JSONObject>();
		HVector queryRes = null;
		if (eq.db_type == ICL.MSSQL) {
			String querySql = sqlselect + " ROW_NUMBER() over(order by "
					+ orderByStr + ") _r," + makefildesAsF1(sqlfilds) + " "
					+ sqlform;
			querySql = sqlselect + " " + makefildes(sqlfilds) + " from ("
					+ querySql + ") _t where _r>" + startNum + " and _r<="
					+ endNum;
			querySql = SSTool.formatVarMacro(querySql, eq);
			_log.info(querySql);
			queryRes = eq.queryVec(querySql,page.pageSize);
		}
		if (eq.db_type == ICL.MYSQL) {
			String querySql = sql0 + " order by " + orderByStr + " limit "+ startNum + "," + page.pageSize + ";";
			querySql = SSTool.formatVarMacro(querySql, eq);
			_log.info(querySql);
			queryRes = eq.queryVec(querySql,page.pageSize);
		}
		int celleng = cell.all_cels.length;
		ArrayList<Integer> udffildlist = new ArrayList<Integer>();
		for (int i = 0; i < celleng; i++) {
			Cell fldcell = cell.all_cels[i];
			if ((fldcell.attr & Cell.UDFCOL) > 0
					&& (fldcell.attr & Cell.USEBDS) > 0) {
				_log.info(fldcell.ccName + "===" + fldcell.script);
				udffildlist.add(i);
			}
		}
		if (queryRes != null) {
			if (udffildlist.size() > 0) {
				// 有自定义字段
				HVector copyValues = new HVector();
				for (int i = 0; i < queryRes.size(); i++) {
					Object[] newValues = new Object[celleng];
					Object[] oldValues = (Object[]) queryRes.elementAt(i);
					int mm=0,_size=0;
					for (int j = 0; j < udffildlist.size(); j++) {
						int _indx = udffildlist.get(j);
						Object o0 = getUDFValue(_indx,cell,oldValues);
						for(int k=mm;k<_indx;k++){
							Object ov = oldValues[k-_size];
							String vv = ov==null?"":ov.toString();
							if(ov instanceof Timestamp){
								Cell c1 = cell.all_cels[k];
								if(c1.ccType == 91){
									vv = CCliTool.dateToString(ov, true, cl.ICL.DF_YMD);
								}else{
									vv = CCliTool.dateToString(ov, true, cl.ICL.DF_YMDHM);
								}
							}else{
								vv = CCliTool.objToString(ov);
							}
							newValues[k] = vv;
						}
						newValues[_indx] = o0==null?"":o0;
						mm = _indx+1;
						_size++;
					}
					for(int k=mm;k<celleng;k++){
						Object ov = oldValues[k-_size];
						Object vv = ov==null?"":ov;
						if(ov instanceof Timestamp){
							Cell c1 = cell.all_cels[k];
							if(c1.ccType == 91){
								vv = CCliTool.dateToString(ov, true, cl.ICL.DF_YMD);
							}else{
								vv = CCliTool.dateToString(ov, true, cl.ICL.DF_YMDHM);
							}
						}
						newValues[k] = vv;
					}
					copyValues.addElement(newValues);
			}
				queryRes = copyValues;
			}else {
				for (int i = 0; i < queryRes.size(); i++) {
					Object[] ovsObjects = (Object[]) queryRes.elementAt(i);
					for(int j=0 ;j < ovsObjects.length ; j++){
						Object ov = ovsObjects[j];
						Cell c1 = cell.all_cels[j];
						Object vv =  ov== null ? "": ov;
						if (ov instanceof Timestamp){
							if(c1.ccType == 91){
								vv = CCliTool.dateToString(ov, true, cl.ICL.DF_YMD);
							}else{
								vv = CCliTool.dateToString(ov, true, cl.ICL.DF_DYNC);
							} 
						}else{
							vv = CCliTool.objToString(vv);
						}
						ovsObjects[j] = vv;
					}
					queryRes.setElementAt(ovsObjects, i);
				}
			}
			// 没有自定义字段
//			for (int i = 0; i < queryRes.size(); i++) {
//				resList.add((Object[]) queryRes.elementAt(i));
//			}
		}
		page.celData = makeJsonArray(cell,queryRes);
		return page;

	}
	
	public static HVector findValues(SQLExecQuery eq, Cells cell,
			PageLayOut page) throws Exception {
		String sql0 = spelSQL(eq, cell, 0, page.queryCriteria, true, null);
		String orderByString = "";
		if (sql0.indexOf(" order by") > -1) {
			orderByString = sql0.substring(sql0.indexOf(" order by "));
			orderByString = orderByString
					.substring(orderByString.indexOf("by") + 3);
			sql0 = sql0.substring(0, sql0.indexOf(" order by "));
		}
		String sqlselect = sql0.substring(0, sql0.indexOf(" "));
		String sqlform = sql0.substring(sql0.indexOf("from"));
		String sqlfilds = sql0.substring(sql0.indexOf(" "),
				sql0.indexOf("from"));
		
		int ii = cell.getParent()==null? cell.fkcc:(cell.pkcc-1);
		Cell cc = cell.all_cels[ii>=0?ii:0];
		if (orderByString.length()>0 && orderByString.indexOf(".")<0){
			orderByString = (cell.exTbName==null? orderByString : (cell.exTbName+"."+orderByString));
		}
		String orderByStr = page.orderBy.length() > 0 ? page.orderBy
				: (orderByString.length() > 0 ? orderByString : cc.ccName);
		int startNum = 0, endNum = 0;
		startNum = page.pageSize * (page.currentPage - 1);
		endNum = page.pageSize * page.currentPage;
		String queryCount = sqlselect + " count(*) from ("+sqlselect+" "+makefildesAsF1(sqlfilds)+" "+ sqlform+") b";
		queryCount = SSTool.formatVarMacro(queryCount, eq);
		_log.info(queryCount);
		int count = CCliTool.objToInt(eq.queryOne(queryCount), 0);
		page.setTotalSize(count);
//		ArrayList<JSONObject> resList = new ArrayList<JSONObject>();
		HVector queryRes = null;
		if (eq.db_type == ICL.MSSQL) {
			String querySql = sqlselect + " ROW_NUMBER() over(order by "
					+ orderByStr + ") _r," + makefildesAsF1(sqlfilds) + " "
					+ sqlform;
			querySql = sqlselect + " " + makefildes(sqlfilds) + " from ("
					+ querySql + ") _t where _r>" + startNum + " and _r<="
					+ endNum;
			querySql = SSTool.formatVarMacro(querySql, eq);
			_log.info(querySql);
			queryRes = eq.queryVec(querySql,page.pageSize);
		}
		if (eq.db_type == ICL.MYSQL) {
			String querySql = sql0 + " order by " + orderByStr + " limit "
					+ startNum + "," + page.pageSize + ";";
			querySql = SSTool.formatVarMacro(querySql, eq);
			_log.info(querySql);
			queryRes = eq.queryVec(querySql,page.pageSize);
		}
		int celleng = cell.all_cels.length;
		ArrayList<Integer> udffildlist = new ArrayList<Integer>();
		for (int i = 0; i < celleng; i++) {
			Cell fldcell = cell.all_cels[i];
			if ((fldcell.attr & Cell.UDFCOL) > 0
					&& (fldcell.attr & Cell.USEBDS) > 0) {
				_log.info(fldcell.ccName + "===" + fldcell.script);
				udffildlist.add(i);
			}
		}
		if (queryRes != null) {
			if (udffildlist.size() > 0) {
				// 有自定义字段
				HVector copyValues = new HVector();
				for (int i = 0; i < queryRes.size(); i++) {
					Object[] newValues = new Object[celleng];
					Object[] oldValues = (Object[]) queryRes.elementAt(i);
					int mm=0,_size=0;
					for (int j = 0; j < udffildlist.size(); j++) {
						int _indx = udffildlist.get(j);
						Object o0 = getUDFValue(_indx,cell,oldValues);
						for(int k=mm;k<_indx;k++){
							Object ov = oldValues[k-_size];
							String vv = ov==null?"":ov.toString();
							if(ov instanceof Timestamp){
								Cell c1 = cell.all_cels[k];
								if(c1.ccType == 91){
									vv = CCliTool.dateToString(ov, true, cl.ICL.DF_YMD);
								}else{
									vv = CCliTool.dateToString(ov, true, cl.ICL.DF_YMDHM);
								}
							}else{
								vv = CCliTool.objToString(ov);
							}
							newValues[k] = vv;
						}
						newValues[_indx] = o0==null?"":o0;
						mm = _indx+1;
						_size++;
					}
					for(int k=mm;k<celleng;k++){
						Object ov = oldValues[k-_size];
						Object vv = ov==null?"":ov;
						if(ov instanceof Timestamp){
							Cell c1 = cell.all_cels[k];
							if(c1.ccType == 91){
								vv = CCliTool.dateToString(ov, true, cl.ICL.DF_YMD);
							}else{
								vv = CCliTool.dateToString(ov, true, cl.ICL.DF_YMDHM);
							}
						}
						newValues[k] = vv;
					}
					copyValues.addElement(newValues);
			}
				queryRes = copyValues;
			}else {
				for (int i = 0; i < queryRes.size(); i++) {
					Object[] ovsObjects = (Object[]) queryRes.elementAt(i);
					for(int j=0 ;j < ovsObjects.length ; j++){
						Object ov = ovsObjects[j];
						Cell c1 = cell.all_cels[j];
						Object vv =  ov== null ? "": ov;
						if (ov instanceof Timestamp){
							if(c1.ccType == 91){
								vv = CCliTool.dateToString(ov, true, cl.ICL.DF_YMD);
							}else{
								vv = CCliTool.dateToString(ov, true, cl.ICL.DF_DYNC);
							}
						}else{
							vv = CCliTool.objToString(vv);
						}
						ovsObjects[j] = vv;
					}
					queryRes.setElementAt(ovsObjects, i);
				}
			}
			// 没有自定义字段
//			for (int i = 0; i < queryRes.size(); i++) {
//				resList.add((Object[]) queryRes.elementAt(i));
//			}
		}
		return queryRes;

	}

	/**
	 * @return
	 */
	public static Object getUDFValue(int index,Cells cells,Object[] values) {
		Cell cc = cells.all_cels[index];
		String script = cc.script==null?"":cc.script;
		ServScript sp = new ServScript(cells);
		sp.initRows(values);
		sp.setSQLQuery(_eq);
		Object revalue = null;
		if(script.length()>0){
			//=:sql("AM",$(select slkid from htzx where sid={&xh.slkid}))##xh.slkid
			//=:[xha.fcy]-[xha.costrmb]
//			if ()
			String sgs = cc.getScript("=:", false);
			try {
				if(sgs.indexOf("#")>0){
					sgs = sgs.substring(0,sgs.indexOf("#"));
				}
				revalue = sp.execute(sgs, cc.ccName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			revalue="";
		}
		return revalue;
	}

	/**
	 * @param sqlfilds
	 * @return
	 */
	private static String makefildes(String sqlfilds) {
		String[] flds = sqlfilds.split(",");
		String fld = "";
		for(int i=0;i<flds.length;i++){
			fld+="f"+(i+1)+",";
		}
		fld = fld.substring(0,fld.length()-1);
		_log.info(fld);
		return fld;
	}
	
	/**
	 * @param sqlfilds
	 * @return
	 */
	private static String makefildesAsF1(String sqlfilds) {
		String[] flds = sqlfilds.split(",");
		String fld = "";
		for(int i=0;i<flds.length;i++){
			fld+=flds[i]+" as f"+(i+1)+",";
		}
		fld = fld.substring(0,fld.length()-1);
		_log.info(fld);
		return fld;
	}

	private static Object assistType(SQLExecQuery eq,String editName){
		String[] str = new String[2]; 
		String type = "C_SELECT";
		String script = null;
		if(editName == null || editName.equals("")){
			str[0] = type;
			return str;
		}
		try {
			String sql = "select slink,sclass from insaid where sid = '"+editName+"'";
			Object[] objects = eq.queryRow(sql, false);
			if(objects ==null){
				return type;
			}
			String slink  = CCliTool.objToString(objects[0]);
			
			String aa = CCliTool.objToString(objects[1]);
			if(aa.equals("inetbas.cli.cutil.CGroupEditor")){
				type = "C_GROUP";
				if(slink.indexOf("&") !=-1){
					script = slink.substring(0,slink.indexOf("&"));
				}
			}else if(aa.equals("ebas.cutil.CGDicEditor")){
				type = "C_GDIC";
			}else if(aa.equals("inetbas.cli.cutil.CDateEditor")){
				type = "C_DATE";
			}else{
				type = "C_SELECT";
			}
		} catch (Exception e) {
			e.printStackTrace();
			type = "C_SELECT";
			script = null;
		} finally{
			str[0] = type;
			str[1] = script; 
		} 
		return str;
	} 
}