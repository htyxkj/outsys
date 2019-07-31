/**
 * 
 */
package inetbas.web.outsys.api;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import inet.CNextItem;
import inet.HVector;
import inetbas.cli.cutil.CRptItem;
import inetbas.pub.coob.CKeyVector;
import inetbas.pub.coob.CODEOBJ;
import inetbas.sserv.SQLConnection;
import inetbas.sserv.SQLExecQuery;
import inetbas.sserv.SQLTOOL;
import inetbas.sserv.SSTool;
import inetbas.web.outsys.api.uidata.UICData;

/**
 * RPT数据获取分析
 * @author www.bip-soft.com
 * 2019-07-30 10:46:59
 */
public class RPTCallable implements Callable<UICData> {
	private Logger _log = LoggerFactory.getLogger(RPTCallable.class);
	private SQLExecQuery eq;
	private WebServeCRef _osv;
	private String objId;
	private HVector rptItems;
	
	public RPTCallable(){}
	public RPTCallable(String dbid,HVector items,CODEOBJ vars,WebServeCRef osv,String obj_id){
		try {
			rptItems = items;
			eq = SQLConnection.getExecQuery(dbid, 0,(byte)0);
			eq.macro_init(vars);
			_osv = osv;
			objId = obj_id;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public UICData call() throws Exception {
		long l1 = System.currentTimeMillis();
		_log.info(l1+"");
		UICData data = new UICData(objId);
		for(int i=0;i<rptItems.size();i++) {
			CRptItem item = (CRptItem)rptItems.elementAt(i);
			String sql0 = item.cibds;
			_log.info(sql0);
			int x0 = sql0.indexOf(";");
			String sql = "";
			if(x0<=-1) {
				sql = sql0;
				sql0 = "";
			}else if(x0==0){
				sql = sql0.substring(1);
				sql0="";
			}else {
				sql = sql0.substring(0,x0);
				sql0 = sql0.substring(x0+1);
			}
			HVector h0 = getValues(sql);
			System.out.println(h0.size());
			char cysf;
			while (true&&sql0.length()>0) {
				x0 = sql0.indexOf(";");
				if(x0==0) {
					sql0 = sql0.substring(1);
					sql = sql0;
					sql0="";
				}else if(x0>0){
					sql = sql0.substring(0, x0);
					sql0 = sql0.substring(x0+1);
					
				}else {
					sql = sql0;
					sql0 = "";	
				}
				 cysf = sql.length() > 0 ? sql.charAt(0) : '0';
				 if ("+-*/".indexOf(cysf) >= 0)
					 sql = sql.substring(1);
				 else
					  cysf = '+';//;-运算符,两个数据集间的运算
				 
				 HVector h2 = getValues(sql);
				 System.out.println(h2);
				 if(x0<0) {
					 break;
				 }
				 
			}
			
		}
		long l2 = System.currentTimeMillis();
		_log.info(l2+"");
		_log.info("执行时间"+(l2-l1)+"");
		return data;
	}
	
	public HVector getValues(String sql) throws Exception {
		sql = SSTool.formatVarMacro(sql, eq, _osv);
		_log.info(sql);
		Object o0 = eq.query(sql, false, null, 0, 0, true);
		HVector hh = new HVector();
		if(o0==null) {
			return hh;
		}
		if(o0 instanceof CKeyVector) {
			_log.info("CKeyVector");
			CKeyVector keyVector = (CKeyVector)o0;
			hh = SQLTOOL.toHVector(keyVector);
//			System.out.println(hh.size());
			while (keyVector.keyID>0) {
//				System.out.println(keyVector.keyID);
				Object o0s = eq.nextQuery(512);
				if(o0s==null) {
					break;
				}
				
				hh = SQLTOOL.toHVector(o0s);
//				System.out.println(hh.size());
			}
		}else if(o0 instanceof CNextItem) {
			CNextItem c0 = (CNextItem)o0;
			hh = SQLTOOL.toHVector(c0);
			_log.info("CNextItem");
		}else {
			hh = (HVector)o0;
			_log.info(o0.toString());
		}
		return hh;
	}

}
