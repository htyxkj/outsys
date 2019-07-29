/**
 * 
 */
package inetbas.web.outsys.tools;

import java.util.ArrayList;

import inet.HVector;
import inetbas.cli.cutil.CCliTool;
import inetbas.pub.coob.Cell;
import inetbas.pub.coob.Cells;
import inetbas.sserv.SQLExecQuery;
import inetbas.web.outsys.entity.QueryEntity;

/**
 * @author www.bip-soft.com
 * 2019-03-21 18:25:49
 */
public class SQLUtils {	
	public static SQLInfoE makeSqlInfo(String sql0,QueryEntity qe,int dbType) {
		SQLInfoE sqlInfoE = new SQLInfoE(sql0,dbType);
		String sqlform = sql0.substring(sql0.indexOf("from"));
		sqlInfoE.setSqlfrom(sqlform);
		String sqlfilds = sql0.substring(sql0.indexOf(" "),sql0.indexOf("from")).trim();
		HVector hh = getSqlSelectFled(sqlfilds);
		if(hh!=null) {
			ArrayList<SQLFiledInfo> arrayList = new ArrayList<SQLFiledInfo>();
			for(int i=0;i<hh.size();i++) {
				SQLFiledInfo filedInfo = new SQLFiledInfo(hh.elementAt(i).toString(), i);
				arrayList.add(filedInfo);
			}
			sqlInfoE.setFiledInfos(arrayList);
			makeOrderByAndGroupBy(sqlInfoE, qe);
			sqlInfoE.makeTotal();
			int startNum = 0;
			startNum = qe.getPage().getPageSize() * (qe.getPage().getCurrPage() - 1);
			sqlInfoE.makeSelectPage(startNum,qe.getPage().getPageSize());
		}
		return sqlInfoE;
	}
	
	public static void makeOrderByAndGroupBy(SQLInfoE sqlInfoE,QueryEntity qe) {
		String sqlform = sqlInfoE.getSqlfrom();
		String orderBy = "";
		int index = sqlform.indexOf("order by");
		if(index>-1) {
			orderBy = sqlform.substring(index);
			sqlform = sqlform.substring(0,index);
			
		}
		if(qe.getOrderBy()!=null&&qe.getOrderBy().length()>0) {
			orderBy = "order by "+qe.getOrderBy();
		}
		orderBy = orderBy==null?"":orderBy;
		if(orderBy.length()==0) {
			orderBy = " order by "+ sqlInfoE.getCommOrderByFiled();
		}
		String groupBy = "";
		index = sqlform.indexOf("group by");
		if(index>-1) {
			groupBy = sqlform.substring(index);
			sqlform = sqlform.substring(0,index);
		}
		if(qe.getCont()!=null&&qe.getType()==3) {
			String cont = qe.getCont().trim();
			int _i = sqlform.indexOf(" where");
			if(cont.length()>0) {
				if(cont.startsWith("~"))
					cont = cont.substring(1);
				if(_i>0) {
					sqlform+=" and ("+cont+")";
				}else {
					sqlform+=" where "+cont;
				}
			}
		}
		sqlInfoE.setSqlfrom(sqlform);
		sqlInfoE.setOrderBy(orderBy);
		sqlInfoE.setGroupBy(groupBy);
	} 
	
	/**
	 * @return
	 */
	public static Object getUDFValue(int index,Cells cells,Object[] values,SQLExecQuery _eq) {
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
	 * @param sql
	 * @return
	 * 2019-07-29 14:45:25
	 */
	public static HVector getSqlSelectFled(String sql) {
		return CCliTool.divide(sql, ',', true);
	}

}
