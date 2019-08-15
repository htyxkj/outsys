/**
 * 
 */
package inetbas.web.outsys.api;

import inet.CRuntimeException;
import inet.HVector;
import inetbas.cli.cutil.CCliTool;
import inetbas.pub.coob.CRecord;
import inetbas.pub.coob.CRef;
import inetbas.pub.coob.Cell;
import inetbas.pub.coob.Cells;
import inetbas.pub.ojc.CExcel;
import inetbas.serv.csys.DBInvoke;
import inetbas.sserv.SQLExecQuery;
import inetbas.sserv.SSTool;
import inetbas.web.outsys.api.uidata.UICData;
import inetbas.web.outsys.api.uidata.UIRecord;
import inetbas.web.outsys.entity.QueryEntity;
import inetbas.web.outsys.tools.CellsUtil;
import inetbas.web.outsys.tools.CommUtils;
import inetbas.web.outsys.tools.DataTools;
import inetbas.web.outsys.tools.SQLInfoE;
import inetbas.web.outsys.tools.SQLUtils;
import inetbas.web.webpage.api.WebApiAidInvoke;
import inetbas.web.webpage.wxpub.VarObject;
import inetbas.webserv.SErvVars;
import inetbas.webserv.WebAppPara;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;

/**
 * 单据界面数据查询
 * @author www.bip-soft.com
 * 2019-03-21 10:13:09
 */
public class WebApiPageInvoke extends DBInvoke {
	private static Logger _log = LoggerFactory.getLogger(WebApiPageInvoke.class);
	private static Map<String,Object> exRef = new HashMap<String, Object>();//导出excel是的参照
	private static Map<Integer,String[]> exCid = new HashMap<Integer,String[]>();//结果集中第几个是需要转参照
	
	public Object processOperator(SQLExecQuery eq, WebAppPara wa) throws Exception {
		if (SErvVars.__$L || SErvVars.__$3)
			throw new CRuntimeException("[%0AUTHEND]");
		Object[] ops = wa.params;
		int orid = wa.oprid;
		if(orid>=213&&orid<=216) {
			return findPages(eq, ops);
		}else if(orid == 400){
			return exportFile(eq, ops);
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
				ArrayList<UIRecord> arr = DataTools.valuesToJsonArray2(hh, cell, 0, null,qEntity.getType()==1);
				data.setPage(qEntity.getPage());
				data.setData(arr);
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
		String sc = null;
		if(qe.oprid == 14){
			String cont = qe.getCont(); 
			if(cont != null && cont.length()>1){
				sc = cont;
			}
		}else{
			sc = CommUtils.getContStr(querCell, qe.getCont());
		}
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
		st0 = spelSQL(eq, cell, 0, sc, true, st0, dbi);
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
				List<UIRecord> listData = DataTools.valuesToJsonArray2(v0,cell, 0,extb,qe.getType()>0);
				data.setData(listData);
			}
		}
		return data;
	}
	
	
	public String exportFile(SQLExecQuery eq, Object[] ops) throws Exception {

		String st0 =null;
		String sc = null;
		
		QueryEntity qe = (QueryEntity) ops[0];
		String pcell = qe.getPcell();
		Cells cell = (Cells)SSTool.readCCells(eq, pcell, false);
		CellsUtil.initCells(cell);
		cell.condiction = SSTool.formatVarMacro(cell.condiction, eq);
		String tcellId = qe.getTcell();
		Cells tcell = null;
		if(!pcell.equals(tcellId)) {
			tcell = (Cells)SSTool.readCCells(eq, tcellId, false);
			CellsUtil.initCells(tcell);
			tcell.condiction = SSTool.formatVarMacro(tcell.condiction, eq);
		}else {
			tcell = cell;
		} 
		if(qe.oprid == 14){
			String cont = qe.getCont(); 
			if(cont != null && cont.length()>1){
				sc = cont;
			}
		}else{
			sc = CommUtils.getContStr(tcell, qe.getCont());
		}
		st0 = spelSQL(eq, cell, 0, sc, true, st0, this);
		HVector hh = eq.queryVec(st0);
		hh = getReference(hh, cell,eq);
		hh.insertElementAt(getCellTitle(cell), 0);
		String dir = WebUPDFileService.getFileDir(eq.db_id, true);
		String filesString = dir+""+WebUPDFileService.getNows()+".xls";
		OutputStream out = new FileOutputStream(filesString);
		CExcel.expExcel(out,hh,null,cell.all_cels,true,false);
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
	private static HVector getReference(HVector hh,Cells cell,SQLExecQuery eq) throws Exception{
		exRef = new HashMap<String, Object>();//导出excel是的参照
		exCid = new HashMap<Integer, String[]>();//结果集中第几个是需要转参照
		Cell[] cel =cell.all_cels;
		for (int i = 0; i < cel.length; i++) {
			Cell ce = cel[i];			
			if(ce.refValue !=null && !ce.refValue.equals("")){
				String[] refArr = new String[2];
				refArr[1] = "n";
				if((ce.attr & 0x200000)>0){//多项
					refArr[1] = "y";
				}				
				String ref = CCliTool.objToString(ce.refValue);
				refArr[0] = ref;
				exCid.put(i, refArr);
			}
		} 
		if(exCid.size() ==0)
			return hh;
		for (int i = 0; i < hh.size(); i++) {
			Object[] obj = (Object[]) hh.elementAt(i);
			for(Map.Entry<Integer, String[]> entry:exCid.entrySet()){
				String vl = CCliTool.objToString(obj[entry.getKey()]);//值
				String[] refs = entry.getValue();//参照名称,是否是多项
				if(vl!=null){
					String[] cc=null;
					if(refs[1].equals("y")){
						cc = vl.split(";");
					}else {
						cc = new String[]{vl};
					}
					String newVlStr = "";
					for (int j = 0; j < cc.length; j++) {
						if(exRef.containsKey(refs[0]+"_"+cc[j])){
							newVlStr+=exRef.get(refs[0]+"_"+cc[j])+";";
						}else{
							VarObject vobj = WebApiAidInvoke.findValues(eq, refs[0], cc[j], "", null);
							if(vobj.getValues()!=null){
								List<JSONObject> listJ = vobj.getValues();
								for (int k = 0; k < listJ.size(); k++) {
									JSONObject json = listJ.get(k);
									if(json.containsKey(vobj.getAllCols()[vobj.getShowCols()[0]])){
										String refKey = CCliTool.objToString(json.get(vobj.getAllCols()[vobj.getShowCols()[0]]));
										String refVal = CCliTool.objToString(json.get(vobj.getAllCols()[vobj.getShowCols()[1]]));
										if(refKey.equals(cc[j])){											
											newVlStr+=refVal+";"; 
										}
										exRef.put(refs[0]+"_"+refKey, refVal);
									} 
								} 
							} 
						}
					} 
					if(!newVlStr.equals(""))
						newVlStr = newVlStr.substring(0,newVlStr.length()-1);
					obj[entry.getKey()] = newVlStr;
				} 
			}
			hh.setElementAt(obj, i);
		}
		exRef = null;//导出excel是的参照
		exCid = null;//结果集中第几个是需要转参照
		return hh;
	}
}
