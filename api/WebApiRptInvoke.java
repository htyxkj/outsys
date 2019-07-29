/**
 * 
 */
package inetbas.web.outsys.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import inet.HVector;
import inet.ReadLine;
import inetbas.cli.cutil.CCliTool;
import inetbas.cli.cutil.CRptItem;
import inetbas.pub.coob.CRecord;
import inetbas.pub.coob.Cells;
import inetbas.pub.cutil.CRefBas;
import inetbas.serv.csys.DBInvoke;
import inetbas.sserv.SQLExecQuery;
import inetbas.sserv.SSTool;
import inetbas.web.outsys.api.uidata.UICData;
import inetbas.web.outsys.api.uidata.UIRecord;
import inetbas.web.outsys.entity.QueryEntity;
import inetbas.web.outsys.tools.DataTools;
import inetbas.web.outsys.tools.SQLInfoE;
import inetbas.web.outsys.tools.SQLUtils;
import inetbas.webserv.WebAppPara;

/**
 * @author www.bip-soft.com
 * 2019-07-25 16:30:14
 */
public class WebApiRptInvoke extends DBInvoke {
	private static Logger _log = LoggerFactory.getLogger(WebApiRptInvoke.class);
	public static final int INIT_RPT = 200;
	@Override
	public Object processOperator(SQLExecQuery eq, WebAppPara wa) throws Exception {
		int id = wa.oprid;
		if(INIT_RPT == id) {
			Object[] ops = wa.params;
			QueryEntity qe = (QueryEntity)ops[0];
			String cellid = qe.getPcell();
			Cells cell = SSTool.readCCell(eq, cellid, true);
			Cells celltj = SSTool.readCCell(eq, qe.getTcell(), false);
			CRecord cRecord = DataTools.makeCRecordByJsonStr(celltj,qe.getCont());
			HVector vd = getRPTInfo(eq,cellid, cell,true);
			UICData data = new UICData(cellid);
			if(vd.size()>0) {
				CRptItem item = (CRptItem)vd.elementAt(0);
				String sql = item.cibds;
				CRefBas cc = new CRefBas(null, celltj,cRecord.getValues());
				WebServeCRef osv = new WebServeCRef(cc,eq);
				sql = SSTool.formatVarMacro(sql, eq, osv);
				_log.info(sql);
				SQLInfoE ss = SQLUtils.makeSqlInfo(sql, qe, eq.db_type);
				String totalSQL = ss.getTotalSql();
				_log.info(totalSQL);
				int total = CCliTool.objToInt(eq.queryOne(totalSQL),0);
				data.setPage(qe.getPage());
				data.getPage().setTotal(total);
				if(total>0) {
					String paging = ss.getPagingSql();
					_log.info(paging);
					HVector v0 = eq.queryVec(paging);
					List<UIRecord> listData = DataTools.valuesToJsonArray2(v0,cell, 0,null,true);
					data.setData(listData);
				}
				
			}
			return data;
		}
		
		return null;
	}
	
	

	public static HVector getRPTInfo(SQLExecQuery eq, String cellid, Cells cell,boolean brpt)
			throws IOException, FileNotFoundException {
		HVector hd = new HVector();
		File f0 = SSTool.getFile(SSTool.dirRPT(true, eq.db_id, 0, 0), getRPTUDF(cellid, brpt), eq.i_co, eq.db_type);
		if(f0.exists()) {
			byte[] rr = CCliTool.readFull(new FileInputStream(f0), (int)f0.length());
			ReadLine rl = new ReadLine(new ByteArrayInputStream(rr));
			CRptItem ri = null;
			String s0;
			while ((s0 = rl.readLineEx(false)) != null) {
			  s0 = s0.trim();
//			  _log.info(s0);
			  if (s0.length() > 0)
			   ri = CRptItem.initialize(hd, ri, s0, cell);
			 }
		}
		return hd;
	}
	
	public static String getRPTUDF(String sobj, boolean brpt) {
		 return cl.ICL.PRE_BUID + sobj.replace('-', '_') + (brpt ? ".rpt" : ".txt");
	}
}
