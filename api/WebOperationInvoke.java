package inetbas.web.outsys.api;

import inetbas.cli.cutil.CCliTool;
import inetbas.serv.csys.DBInvoke;
import inetbas.sserv.SQLExecQuery;
import inetbas.web.outsys.entity.Operation;
import inetbas.webserv.WebAppPara;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author www.bip-soft.com
 *
 */
public class WebOperationInvoke extends DBInvoke {
	private static Logger _log = LoggerFactory.getLogger(WebOperationInvoke.class);
	
	public static final int OPERATIONID = 200;
	
	public static final int us_mkvou = 1;//提交生成凭证
	public static final int us_delvou = 2;//提交删除凭证
	public static final int us_gmts = 4;//超量提示
	public static final int us_gmerr = 8;//超量报错
	public Object processOperator(SQLExecQuery eq, WebAppPara wa) throws Exception {
		int id = wa.oprid;
		if(id == OPERATIONID){
			String buid = CCliTool.objToString(wa.params[0]);
			if(buid!=null){
				return getOperationInfoById(eq, buid);
			}else{
				return null;
			}
		}
		return null;
	}
	
	public static Operation getOperationInfoById(SQLExecQuery eq,String buid) throws Exception {
		String sql="select buid, pname, bmain, bnew, pmenuid, maintb, pkfld,buidfld, reffld, bulnk, "
				+ "lkbuidfld, lknofld,"+
	"statefld, iymfld, hpdatefld, sorgfld, smakefld,qid,purl,docfld,docfmt,us_atr,sublnk,procs ";
		sql+=" from insbu where buid='"+buid+"'";
		_log.info(sql);
		_log.info("getBUID");
		Object infos = eq.queryRow(sql, false);
		if(infos!=null){
			Object[] o0 = (Object[])infos;
			Operation opt = new Operation();
			opt.setBuid(CCliTool.objToString(o0[0]));
			opt.setPname(CCliTool.objToString(o0[1]));
			opt.setBmain(CCliTool.objToInt(o0[2], 0)==1);
			opt.setBnew(CCliTool.objToInt(o0[3], 0)==1);
			opt.setPmenuid(CCliTool.objToString(o0[4]));
			opt.setMaintb(CCliTool.objToString(o0[5]));
			opt.setPkfld(CCliTool.objToString(o0[6]));
			opt.setBuidfld(CCliTool.objToString(o0[7]));
			opt.setReffld(CCliTool.objToString(o0[8]));
			opt.setBulnk(CCliTool.objToString(o0[9]));
			opt.setLkbuidfld(CCliTool.objToString(o0[10]));
			opt.setLknofld(CCliTool.objToString(o0[11]));
			opt.setStatefld(CCliTool.objToString(o0[12]));
			opt.setIymfld(CCliTool.objToString(o0[13]));
			opt.setHpdatefld(CCliTool.objToString(o0[14]));
			opt.setSorgfld(CCliTool.objToString(o0[15]));
			opt.setSmakefld(CCliTool.objToString(o0[16]));
			opt.setQid(CCliTool.objToString(o0[17]));
			opt.setPurl(CCliTool.objToString(o0[18]));
			opt.setDocfld(CCliTool.objToString(o0[19]));
			opt.setDocfmt(CCliTool.objToString(o0[20]));
			int attr = CCliTool.objToInt(o0[21], 0);
			if(attr>0){
				if((attr&us_mkvou)>0){
					opt.setUs_mkvou(true);
				}
				if((attr&us_delvou)>0){
					opt.setUs_delvou(true);
				}
				if((attr&us_gmts)>0){
					opt.setUs_gmts(true);
				}
				if((attr&us_gmerr)>0){
					opt.setUs_gmerr(true);
				}
			}
			opt.setSublnk(CCliTool.objToString(o0[22]));
			opt.setProcs(CCliTool.objToString(o0[23]));
			return opt;
		}
		
		return null;
	}
}
