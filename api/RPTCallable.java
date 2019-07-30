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
import inetbas.sserv.SSTool;
import inetbas.web.outsys.api.uidata.UICData;

/**
 * RPT数据获取分析
 * @author www.bip-soft.com
 * 2019-07-30 10:46:59
 */
public class RPTCallable implements Callable<UICData> {
	private Logger _log = LoggerFactory.getLogger(WebApiRptInvoke.class);
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
		UICData data = new UICData(objId);
		for(int i=0;i<rptItems.size();i++) {
			CRptItem item = (CRptItem)rptItems.elementAt(i);
			String sql = item.cibds;
			sql = SSTool.formatVarMacro(sql, eq, _osv);
			_log.info(sql);
			Object o0 = eq.query(sql, false, null, 0, 0, true);
			if(o0 instanceof CKeyVector) {
				_log.info("CKeyVector");
//				eq.nextQuery(0);
			}else if(o0 instanceof CNextItem) {
				_log.info("CNextItem");
			}else {
				_log.info(o0.toString());
			}
		}
		return data;
	}

}
