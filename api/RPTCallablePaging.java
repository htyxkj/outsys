/**
 * 
 */
package inetbas.web.outsys.api;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import inet.HVector;
import inetbas.sserv.SQLExecQuery;


/**
 * RPT分页数据获取分析
 * @author www.bip-soft.com
 * 2019-07-30 10:46:59
 */
public class RPTCallablePaging implements Callable<HVector> {
	private Logger _log = LoggerFactory.getLogger(RPTCallablePaging.class);
	private SQLExecQuery _eq;
	private String _sql;
	
	public RPTCallablePaging(){}
	public RPTCallablePaging(SQLExecQuery eq,String sql){
		_eq = eq;
		_sql = sql;
	}
	
	@Override
	public HVector call() throws Exception {
		_log.info(_sql);
		HVector hh = _eq.queryVec(_sql);
		return hh;
	}
	
}
