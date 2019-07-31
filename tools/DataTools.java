/**
 * 
 */
package inetbas.web.outsys.tools;

import inet.HVector;
import inetbas.cli.cutil.CCliTool;
import inetbas.pub.coob.CData;
import inetbas.pub.coob.CRecord;
import inetbas.pub.coob.Cell;
import inetbas.pub.coob.Cells;
import inetbas.web.outsys.api.uidata.UICData;
import inetbas.web.outsys.api.uidata.UIRecord;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @author www.bip-soft.com
 * 2019-07-29 17:13:58
 */
public class DataTools {
	
	 /**
	 * 组成CRecord数据，来源数据是JSON格式数据
	 * @param cells
	 * @param jsonData
	 * @return
	 */
	public static CRecord makeCRecordByJsonStr(Cells cells, String jsonData) {
		UIRecord uir = JSON.parseObject(jsonData, UIRecord.class);
		CRecord cr = makeSysRecord(cells, uir);
		return cr;
	}
	
	public static  CRecord makeSysRecord(Cells cells, UIRecord uir) {
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
	 * @param v0
	 * @param cell
	 * @param qe
	 * 2019-03-22 14:55:34
	 */
	public static ArrayList<UIRecord> valuesToJsonArray2(HVector v0, Cells cell,long attr,String extb,boolean report) {
		ArrayList<UIRecord> arrayList = new ArrayList<UIRecord>();
//		if(report) {
//			for(int i=0;i<v0.size();i++) {
//				Object[] v1 = (Object[]) v0.elementAt(i);
//				UIRecord jsonObject = makeValuesToUIRecord(v1, cell.db_cels);
//				arrayList.add(jsonObject);
//			}
//			return arrayList;
//		}
		boolean b2 = extb != null && extb.length() > 0;
		String s0 = cell.toSQLString(false, attr, false, false, true, b2);
		HVector hv = SQLUtils.getSqlSelectFled(s0);
		Cell[] cc = cell.db_cels;
		if(!report) {
			String[] flds = new String[hv.size()];
			for (int i = 0; i < hv.size(); i++) {
				flds[i] = CCliTool.objToString(hv.elementAt(i));
			}
			cc = cell.getCCells(CCliTool.toIndexs(cell, flds, 0));
		}
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
							uData.setData(arr1);
							subs.add(uData);
						}
					}
					uiRecord.setSubs(subs);
				}
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
	
	public static UIRecord makeValuesToUIRecord(Object[] vl,Cell[] cells){
		UIRecord cRecord = new UIRecord();
		JSONObject json = makeValuesToJSON(vl, cells);
		cRecord.setData(json);
		return cRecord;
	}
	
	public static JSONObject makeValuesToJSON(Object[] vl,Cell[] cells){
		JSONObject json = new JSONObject();
		for(int i=0;i<cells.length;i++) {
			Cell cell = cells[i];
			if(cell == null) 
				continue;
			int type = cell.ccType;
			Object ov = null;
			if(i<vl.length) {
				ov = vl[i];
			}else {
				ov = "";
			}
			if(type==91||type ==93 || ov instanceof Timestamp ) {
				String vv = CCliTool.dateToString(ov, true, type==93?8:cl.ICL.DF_YMD);
				json.put(cell.ccName, vv);
			} else {
				json.put(cell.ccName, ov);
			}	
		}
		return json;
	}
	

}
