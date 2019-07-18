/**
 * 
 */
package inetbas.web.outsys.tools;

import inet.CFPS;
import inet.HVector;
import inetbas.cli.cutil.CCliTool;
import inetbas.pub.cbds.CVar;
import inetbas.pub.cbds.ScriptProc;
import inetbas.pub.coob.Cells;
import inetbas.pub.cutil.CRefBas;
import inetbas.sserv.SQLExecQuery;

import java.util.Hashtable;

/**
 * @author www.bip-soft.com
 *
 */
public class ServScript extends ScriptProc {
	
	private CFPS _sqlexec;
	private Cells _cell;
	private SQLExecQuery eq;
	private static Hashtable<String,String> _sqls = new Hashtable<String,String>();//;--SQL语句。
	
	public ServScript(Cells cell){
		super();
		initthis();
		_cell = cell;
	}
	
	public void initRows(Object[] rows){
		setCurRows(rows);
	}
	
	public void initthis() {
		 super.initthis();
		 _sqlexec = null;
		}
	
	/**
	 * 参数为整个SQL语句或长文本引用,返回值只有一个 
	 */
	public Object f_sql(CFPS fps) throws Exception {
	 int cc = fps.size();
	 String sql, vid, sf;
	 if (cc == 1) {
	  sql = (String) fps.elementAt(0);
	  vid = null;
	  sf = cl.ICL.EQ_queryOne;
	 } else {
	  vid = (String) fps.elementAt(0);
	  sql = (String) fps.elementAt(1);
	  char c0 = vid.charAt(0);
	  //;--R=行,C=列,其它字符=单个。
	  sf = c0 == cl.ICL.EQ_queryRow_CH ? cl.ICL.EQ_queryRow : (c0 == cl.ICL.EQ_queryCol_CH ? cl.ICL.EQ_queryCol : cl.ICL.EQ_queryOne);
	  vid = vid.length() < 2 ? null : vid.substring(1);
	 }
	 if (sql.length() < 15 && sql.indexOf(' ') < 0) {
	  Hashtable<String,String> ht0 = _sqls;//;-在长文本中直接定义,此处用引用
	  String s0 = (String) ht0.get(sql);
	  if (s0 == null) {
	   s0 = CCliTool.loadmultxt(sql);
	   if (s0 == null || s0.length() < 1)
	    s0 = "0";
	   ht0.put(sql, s0);
	  }
	  if (s0.length() < 2)
	   return null;
	  sql = s0;
	 }
	 sql = CCliTool.formatVarMacro(sql, getCRefEnvir(_cell, getRows()));
	 if (sql == null || sql.length() < 2)
	  return null;
	 Object ov=null, ov2[] = getSQLExec(sql);//--起用缓存(相同的SQL只执行一次)
	 if (ov2 == null) {
		if(sf.equals(cl.ICL.EQ_queryOne)){
			ov = eq.queryOne(sql);
		}else if(sf.equals(cl.ICL.EQ_queryRow)){
			ov = eq.queryRow(sql, false);
		}else if(sf.equals(cl.ICL.EQ_queryVec)){
			ov = eq.queryVec(sql);
		}else if(sf.equals(cl.ICL.EQ_queryCol)){
			ov = eq.queryCol(sql);
		}
	  addSQLExec(sql, ov);
	 } else
	  ov = ov2[1];
	 if (vid != null && vid.length() > 0)
	  setVar(new CVar(vid, -1, null), ov);//;--设变量缓存。
	 if (ov instanceof HVector)
	  return ((HVector) ov).elementAt(0);//;--返回行0。
	 if (ov instanceof Object[])
	  return ((Object[]) ov)[0];
	 return ov;
	}
	
	/**
	 * @param _cell2
	 * @param rows
	 * @return
	 */
	private CRefBas getCRefEnvir(Cells _cell2, Object[] rows) {
		CRefBas cRefBas = new CRefBas(null, _cell2, rows);
		return cRefBas;
	}

	private Object[] getSQLExec(String sql) {
		 CFPS ose = _sqlexec;
		 int cc = ose == null ? 0 : ose.size();
		 if (cc < 1)
		  return null;
		 Object[] vals = ose.values(), itms;
		 for (int i = 0;i < cc;i++) {
		  itms = (Object[]) vals[i];
		  if (sql.equals(itms[0]))
		   return itms;
		 }
		 return null;
		}
	
	private void addSQLExec(String sql, Object ov) {
		 if (_sqlexec == null)
		  _sqlexec = new CFPS();
		 _sqlexec.addElement(new Object[]{sql, ov });
	}

	/**
	 * @param _eq
	 */
	public void setSQLQuery(SQLExecQuery _eq) {
		eq = _eq;
	}
	
	protected int nameToIndex(String sname) {
		return _cell.nameToIndex(sname);
	}
	
	

}
