/**
 * 
 */
package inetbas.web.outsys.api;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import inet.CRuntimeException;
import inet.HVector;
import inetbas.cli.cutil.CCliTool;
import inetbas.pub.coob.CData;
import inetbas.pub.coob.CRecord;
import inetbas.pub.coob.CRef;
import inetbas.pub.coob.Cell;
import inetbas.pub.coob.Cells;
import inetbas.serv.csys.DBInvoke;
import inetbas.sserv.SQLExecQuery;
import inetbas.sserv.SSTool;
import inetbas.web.outsys.api.uidata.UICData;
import inetbas.web.outsys.api.uidata.UIRecord;
import inetbas.web.outsys.entity.QueryEntity;
import inetbas.web.outsys.tools.CellsUtil;
import inetbas.web.outsys.tools.CommUtils;
import inetbas.web.outsys.tools.SQLInfoE;
import inetbas.web.outsys.tools.SQLUtils;
import inetbas.webserv.SErvVars;
import inetbas.webserv.WebAppPara;

/**
 * 单据界面数据查询
 * @author www.bip-soft.com
 * 2019-03-21 10:13:09
 */
public class WebApiPageInvoke extends DBInvoke {
	private static Logger _log = LoggerFactory.getLogger(WebApiPageInvoke.class);

	public Object processOperator(SQLExecQuery eq, WebAppPara wa) throws Exception {
		if (SErvVars.__$L || SErvVars.__$3)
			throw new CRuntimeException("[%0AUTHEND]");
		Object[] ops = wa.params;
		int orid = wa.oprid;
		if(orid>=213&&orid<=216) {
			return findPages(eq, ops);
		}
		return null;

	}

	public Object findPages(SQLExecQuery eq, Object[] ops) throws Exception {
		QueryEntity qEntity = (QueryEntity) ops[0];
		int orid = qEntity.oprid;
		String pcell = qEntity.getPcell();
		Cells cell = (Cells)SSTool.readCCells(eq, pcell, false);
		CellsUtil.initCells(cell);
		cell.condiction = SSTool.formatVarMacro(cell.condiction, eq);
		String tcellId = qEntity.getTcell();
		Cells tcell = null;
		if(!pcell.equals(tcellId)) {
			tcell = (Cells)SSTool.readCCells(eq, tcellId, false);
			CellsUtil.initCells(tcell);
			tcell.condiction = SSTool.formatVarMacro(tcell.condiction, eq);
		}else {
			tcell = cell;
		}
		if(qEntity.getValues()!=null)
			qEntity.getValues().clear();
		boolean bm = orid == cl.ICL.RQ_FMAIN;
		if (bm || orid == cl.ICL.RQ_FULL) {			
			_log.info(cell.desc);
//			return null;
			
			return find(eq,cell,tcell,qEntity,this, bm ? 0 : cl.ICL.FLAGMAX);
		}else if(orid == cl.ICL.RQ_FREC || orid == cl.ICL.RQ_FCHILD){
			CRef[] rfs = new CRef[cl.ICL.MAXPKLEN];
			cell.fkcc = refAddExp(rfs, 0, cell.fimp, cell.vimp, true);// ;-注意外键值
			int rid = orid;// ;-请求。
			Object[] os = CommUtils.getFldData(tcell, qEntity.getCont());
			CRecord cr0 = new CRecord(0, os);
//			cr0.setValues(os);
			Object osc = CCliTool.getPKValues(cell, cr0, false, true);
			CRecord cc =  findRecord(eq, cell, addExport(cell, rfs, osc, true, false),
					rid == cl.ICL.RQ_FREC ? 2 : (rid == cl.ICL.RQ_FCHILD ? 1 : 0), false);
			if(cc!=null) {
				HVector hh = new HVector();
				hh.addElement(cc);
				UICData data = new UICData(cell.obj_id);
				ArrayList<UIRecord> arr = valuesToJsonArray2(hh, cell, 0, null,qEntity.getType()==1);
//				JSONObject crd = makeValuesToJSON(cc.getValues(), cell.all_cels);
//				crd.put("sys_stated", cc.c_state);
				data.setPage(qEntity.getPage());
				data.set_data(arr);
//				qEntity.getValues().clear();
//				qEntity.setValues(arr);
				return data;
			}	
		}
		return null;

	}
	
	
	/**
	 * 按条件查找数据。
	 */
	public static Object find(SQLExecQuery eq, Cells cell,Cells querCell, QueryEntity qe, DBInvoke dbi, int maxrows) throws Exception {
		String st0 = null;
		int t0;
		long attr = cell.attr;
		String sc = CommUtils.getContStr(querCell, qe.getCont());
		if (sc != null && sc.length() > 0) {
			char c0 = sc.charAt(0);
			if (c0 >= '0' && c0 <= '9') {
				t0 = sc.indexOf('#');// ;-加取行数限制
				if (t0 > 0 && t0 < 5) {
//					topn = Integer.parseInt(sc.substring(0, t0), 10);
					sc = sc.substring(t0 + 1);
					if (sc.length() > 0)
						c0 = sc.charAt(0);
				}
			}
			if (c0 == ',') {
				t0 = sc.indexOf('&');// ;-检查引用表及关联条件
				st0 = sc.substring(1, t0);
				sc = sc.substring(t0 + 1);
			}
		}
		if (sc != null && sc.length() > 0)
			sc = SSTool.formatVarMacro(sc, eq);
		boolean b0 = (attr & cl.ICL.ocAllField) == 0;
		if(qe.getType()>0) {
			b0 = false;
		}
		String extb = st0;
		st0 = spelSQL(eq, cell, b0 ? (Cell.PRIMARY | Cell.LIST) : 0, sc, true, st0, dbi);
		st0 = SSTool.formatVarMacro(st0, eq);
		if(!CCliTool.isNull(qe.getCont(), true)) {
			String cont = qe.getCont().trim();
			if("{}".equals(cont)) {
				qe.setCont("");
			}
		}
		SQLInfoE ss = SQLUtils.makeSqlInfo(st0,qe,eq.db_type);
		String totalSQL =  ss.getTotalSql();
		String pageSQL = ss.getPagingSql();
		int total = 0;
////		if(qe.getType()==0) {
//			total = getBillData(eq, cell, qe, dbi, attr, b0, extb, totalSQL, pageSQL);
////		}else {
////			// 报表
////			_log.info(totalSQL);
////			_log.info(pageSQL);
////		}
//		qe.getPage().setTotal(total);
//		return qe;
		return getBillData(eq, cell, qe, dbi, attr, b0, extb, totalSQL, pageSQL);
	}

	public static UICData getBillData(SQLExecQuery eq, Cells cell, QueryEntity qe, DBInvoke dbi, long attr, boolean b0,
			String extb, String totalSQL, String pageSQL) throws Exception {
		int t0;
		int total;
		_log.info(totalSQL);
		UICData data = new UICData(cell.obj_id);
		total = CCliTool.objToInt(eq.queryOne(totalSQL), 0);
		data.setPage(qe.getPage());
		data.getPage().setTotal(total);
		if (total > 0) {
			_log.info("查询页面数据：" + pageSQL);
			HVector v0 = eq.queryVec(pageSQL);
			int rr = v0 != null ? v0.size() : 0;
			if (rr > 0) {
				t0 = cell.getChildCount();
				if (t0 > 0 && (attr & cl.ICL.ocChild) != 0) {
//					if (t0 > 0) {
					CRecord crd0;
					CRef[] rfs = new CRef[cl.ICL.MAXPKLEN];
					cell.fkcc = refAddExp(rfs, 0, cell.fimp, cell.vimp, true);// ;-注意外键值
					int i, j;
					if (dbi == null) {
						for (i = 0; i < rr; i++) {
							crd0 = new CRecord(0, v0.elementAt(i));
							v0.setElementAt(crd0, i);
							rfs = addExport(cell, rfs, crd0.getValues(), true, true);
							for (j = 0; j < t0; j++)
								crd0.addChild(findChilds(eq, cell.getChild(j), rfs, null));
						}
					} else {
						for (i = 0; i < rr; i++) {
							crd0 = new CRecord(0, v0.elementAt(i));
							v0.setElementAt(crd0, i);
							rfs = addExport(cell, rfs, crd0.getValues(), true, true);
							for (j = 0; j < t0; j++)
								crd0.addChild(dbi.findChilds(eq, cell.getChild(j), rfs));
						}
					}
				}
				else if (b0) {
					CRef[] rfs = addExport(cell, null, v0.elementAt(0), true, true);
					Object o1 = dbi == null ? findRecord(eq, cell, rfs, 3, null, true) : dbi.findRecord(eq, cell, rfs, 3, true);
					v0.setElementAt(o1, 0);
				}
//				ArrayList<JSONObject> arrayList = valuesToJsonArray(v0,cell,b0 ? (Cell.PRIMARY | Cell.LIST) : 0,extb,qe.getType()>0);
//				qe.setValues(arrayList);
				List<UIRecord> listData = valuesToJsonArray2(v0,cell,b0 ? (Cell.PRIMARY | Cell.LIST) : 0,extb,qe.getType()>0);
				data.set_data(listData);
			}
		}
		return data;
	}
	
	/**
	 * @param v0
	 * @param cell
	 * @param qe
	 * 2019-03-22 14:55:34
	 */
	public static ArrayList<JSONObject> valuesToJsonArray(HVector v0, Cells cell,long attr,String extb,boolean report) {
		if(report) {
			ArrayList<JSONObject> arrayList = new ArrayList<JSONObject>();
			for(int i=0;i<v0.size();i++) {
				Object[] v1 = (Object[]) v0.elementAt(i);
				JSONObject jsonObject = makeValuesToJSON(v1, cell.db_cels);
				arrayList.add(jsonObject);
			}
			return arrayList;
		}
		boolean b2 = extb != null && extb.length() > 0;
		String s0 = cell.toSQLString(false, attr, false, false, true, b2);
		String[] flds = s0.split(",");
		//##自定义列
//		int celleng = cell.all_cels.length;
//		ArrayList<Integer> udffildlist = new ArrayList<Integer>();
//		for (int i = 0; i < celleng; i++) {
//			Cell fldcell = cell.all_cels[i];
//			if ((fldcell.attr & Cell.UDFCOL) > 0
//					&& (fldcell.attr & Cell.USEBDS) > 0) {
//				_log.info(fldcell.ccName + "===" + fldcell.script);
//				udffildlist.add(i);
//			}
//		}
		//##自定义列
		Cell[] cc = cell.getCCells(CCliTool.toIndexs(cell, flds, 0));
		ArrayList<JSONObject> arrayList = new ArrayList<JSONObject>();
		for(int i=0;i<v0.size();i++) {
			Object o1 = v0.elementAt(i);
			if(o1==null) {
				continue;
			}
			JSONObject jsonObject = null;
			if(o1 instanceof CRecord) {
				CRecord c1 = (CRecord)o1;
				Object[] v1 = c1.getValues();
				jsonObject = makeValuesToJSON(v1, cell.db_cels);
				jsonObject.put("sys_stated", c1.c_state);
				int t0 = cell.getChildCount();
				if(t0>0) {
					for(int k=0;k<t0;k++) {
						Cells scel = cell.getChild(k);
						CData data = c1.getChild(k);
						if(data!=null) {
							HVector hh = data.elements();
							ArrayList<JSONObject> arr1 = valuesToJsonArray(hh, scel, 0, null,report);
							jsonObject.put(scel.obj_id, arr1);							
						}
					}
				}
			}else {
				Object[] v1 = null;
				if(cc.length==1) {
					v1 = new Object[] {o1};
				}else {
					v1 = (Object[])o1;
				}
				jsonObject = makeValuesToJSON(v1, cc);
			}
			arrayList.add(jsonObject);
		}
		return arrayList;
	}
	
	/**
	 * @param v0
	 * @param cell
	 * @param qe
	 * 2019-03-22 14:55:34
	 */
	public static ArrayList<UIRecord> valuesToJsonArray2(HVector v0, Cells cell,long attr,String extb,boolean report) {
		ArrayList<UIRecord> arrayList = new ArrayList<UIRecord>();
		if(report) {
			
			for(int i=0;i<v0.size();i++) {
				Object[] v1 = (Object[]) v0.elementAt(i);
				UIRecord jsonObject = makeValuesToUIRecord(v1, cell.db_cels);
				arrayList.add(jsonObject);
			}
			return arrayList;
		}
		boolean b2 = extb != null && extb.length() > 0;
		String s0 = cell.toSQLString(false, attr, false, false, true, b2);
		String[] flds = s0.split(",");
		Cell[] cc = cell.getCCells(CCliTool.toIndexs(cell, flds, 0));
		for(int i=0;i<v0.size();i++) {
			Object o1 = v0.elementAt(i);
			if(o1==null) {
				continue;
			}
			UIRecord uiRecord = null;
			if(o1 instanceof CRecord) {
				CRecord c1 = (CRecord)o1;
				Object[] v1 = c1.getValues();
				uiRecord = makeValuesToUIRecord(v1, cell.db_cels);
				uiRecord.setC_state(c1.c_state);
//				jsonObject.put("sys_stated", c1.c_state);
				int t0 = cell.getChildCount();
				List<UICData> subs = new ArrayList<UICData>();
				if(t0>0) {
					for(int k=0;k<t0;k++) {
						Cells scel = cell.getChild(k);
						CData data = c1.getChild(k);
						
						if(data!=null) {
							UICData uData = new UICData(data.obj_id);
							HVector hh = data.elements();
							ArrayList<UIRecord> arr1 = valuesToJsonArray2(hh, scel, 0, null,report);
							uData.set_data(arr1);
							subs.add(uData);
						}
					}
					uiRecord.setSubs(subs);
				}
//				arrayList.add(uiRecord);
			} else {
				Object[] v1 = null;
				if(cc.length==1) {
					v1 = new Object[] {o1};
				}else {
					v1 = (Object[])o1;
				}
				uiRecord = makeValuesToUIRecord(v1, cc);
			}
			arrayList.add(uiRecord);
		}
		return arrayList;
	}
	
	public static JSONObject makeValuesToJSON(Object[] vl,Cell[] cells){
		JSONObject json = new JSONObject();
		for(int i=0;i<cells.length;i++) {
			Cell cell = cells[i];
			int type = cell.ccType;
			Object ov = vl[i];
			if(type==91||type ==93 || ov instanceof Timestamp ) {
				String vv = CCliTool.dateToString(ov, true, type==93?8:cl.ICL.DF_YMD);
				json.put(cell.ccName, vv);
			} else {
				json.put(cell.ccName, ov);
//				json.put(cell.ccName, CCliTool.objToString(ov));
			}	
		}
		return json;
	}
	
	public static UIRecord makeValuesToUIRecord(Object[] vl,Cell[] cells){
		UIRecord cRecord = new UIRecord();
		JSONObject json = makeValuesToJSON(vl, cells);
		cRecord.setData(json);
		return cRecord;
	}
}
