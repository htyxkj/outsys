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
//		int _where = sqlform.indexOf(" where");
//		if(_where>0) {
//			String where = sqlform.substring(_where).trim();
//			sqlInfoE.setWhereCont(where);
//			sqlform = sqlform.substring(0,_where).trim();
//		}
		sqlInfoE.setSqlfrom(sqlform);
		String sqlfilds = sql0.substring(sql0.indexOf(" "),sql0.indexOf("from")).trim();
		HVector hh = spliteSqlFiled(sqlfilds);
		ArrayList<SQLFiledInfo> arrayList = new ArrayList<SQLFiledInfo>();
		for(int i=0;i<hh.size();i++) {
			SQLFiledInfo filedInfo = new SQLFiledInfo(hh.elementAt(i).toString(), i);
			arrayList.add(filedInfo);
		}
//		String cont = qe.getCont();
//		cont = cont ==null?"":cont.trim();
		sqlInfoE.setFiledInfos(arrayList);
		makeOrderByAndGroupBy(sqlInfoE, qe);
		sqlInfoE.makeTotal();
		int startNum = 0;
		startNum = qe.getPage().getPageSize() * (qe.getPage().getCurrPage() - 1);
		sqlInfoE.makeSelectPage(startNum,qe.getPage().getPageSize());
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
	
	public static HVector spliteSqlFiled(String str) {
		HVector hh = new HVector();
		String sqlfilds = str;
		int i = sqlfilds.indexOf(",");
		while (i>=0) {
			String s1 = sqlfilds.substring(0,i);
			int k = s1.indexOf("(");
			if(k>=0) {
				int n = CCliTool.nextBarcket(sqlfilds.toCharArray(), k, sqlfilds.length(), '(');//查找匹配的括号
				if(n>i) {
					i = sqlfilds.indexOf(',', i+1);
					if(i>=0) {
						s1 = sqlfilds.substring(0,i);
					}
				}
			}
			sqlfilds = sqlfilds.substring(i+1);
			hh.addElement(s1.trim());
			i = sqlfilds.indexOf(",");	
		}
		hh.addElement(sqlfilds.trim());
		return hh;
	}
	
//	public static void main(String[] args) {
////		String bb = "ht.cdic,'1111',123,ht.sorg,ht.sopr,ht.htlx,hta.gdic," + 
////				" sum(hta.usd),sum(hta.qtyhs),sum(hta.qty),sum(hta.qtybk),sum(hta.fcy),sum(hta.addtax),sum(hta.rmbhs))" + 
////				" ,ht.cdic,ht.sorg,ht.sopr,ht.htlx,hta.gdic,sum(hta.usd),sum(hta.qtyhs),sum(hta.qty),sum(hta.qtybk)," + 
////				"  sum(hta.fcy),sum(hta.addtax),sum(hta.rmbhs) ";
////		HVector hh = spliteSqlFiled(bb);
////		for(int i=0;i<hh.size();i++) {
////			System.out.println(hh.elementAt(i));
////		}
//		String sql="select cdic,sbuid,sopr,sum(fcy) from ht where sbuid='2111' group by cdic,sbuid,sopr order by cdic";
//		String sql0 = "select ht.cdic,ht.sorg,ht.sopr,ht.htlx,hta.gdic,sum(hta.usd),sum(hta.qtyhs),sum(hta.qty),sum(hta.qtybk),sum(hta.fcy),sum(hta.addtax),sum(hta.rmbhs) from hta,ht where ht.sid>='0' and ht.hpdate>='2018-05-07' and ht.hpdate<'2019-05-08' and ht.sid=hta.sid and ht.c_corp=hta.c_corp and ht.sbuid='2111' and ht.sorg like '0%' group by ht.cdic,ht.sorg,ht.sopr,ht.htlx,hta.gdic order by ht.cdic";
//		QueryEntity qEntity = new QueryEntity();
//		PageInfo pg = new PageInfo();
//		pg.setCurrPage(1);
//		pg.setPageSize(10);
//		qEntity.setPage(pg);
//		SQLInfoE ss = makeSqlInfo(sql0,qEntity,ICL.MSSQL);
//		System.out.println(ss.getTotalSql());
//		System.out.println(ss.getPagingSql());
//		
//	}
}
