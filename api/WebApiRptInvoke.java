/**
 * 
 */
package inetbas.web.outsys.api;

import inet.HVector;
import inet.ReadLine;
import inetbas.cli.cutil.CCliTool;
import inetbas.cli.cutil.CDBLINK;
import inetbas.cli.cutil.CRptItem;
import inetbas.pub.coob.CBasTool;
import inetbas.pub.coob.CRecord;
import inetbas.pub.coob.Cell;
import inetbas.pub.coob.Cells;
import inetbas.pub.cutil.CPubTool;
import inetbas.pub.cutil.CRefBas;
import inetbas.pub.cutil.GOException;
import inetbas.serv.csys.DBInvoke;
import inetbas.sserv.SQLExecQuery;
import inetbas.sserv.SSTool;
import inetbas.web.outsys.api.uidata.UICData;
import inetbas.web.outsys.api.uidata.UIRecord;
import inetbas.web.outsys.entity.PageInfo;
import inetbas.web.outsys.entity.QueryEntity;
import inetbas.web.outsys.tools.DataTools;
import inetbas.web.outsys.tools.SQLInfoE;
import inetbas.web.outsys.tools.SQLUtils;
import inetbas.web.webpage.api.tools.ServScript;
import inetbas.webserv.WebAppPara;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;

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
			//Web端查询对象
			QueryEntity qe = (QueryEntity)ops[0];
			//要查询的cellID
			String cellid = qe.getPcell();
			//获取展示对象cell定义
			Cells cell = SSTool.readCCell(eq, cellid, false);
			//获取查询条件cell定义
			Cells celltj = SSTool.readCCell(eq, qe.getTcell(), false);
			//获取查询条件，并将json格式的数据转CRecord记录
			CRecord cRecord = DataTools.makeCRecordByJsonStr(celltj,qe.getCont());
			if(qe.getType() == 2){ //RPT
				return rpt(eq, cell, qe, cellid, celltj, cRecord);
			}else if (qe.getType() == 3){	//外部SQL
				return externalSQL(eq, cell, qe, cellid, celltj);
			}
		}
		return null;
	}
	/**
	 * 执行RPT查询
	 * @param eq		数据库连接
	 * @param cell		获取展示对象cell定义
	 * @param qe		Web端查询对象
	 * @param cellid	要查询的cellID
	 * @param celltj	获取查询条件cell定义
	 * @param cRecord	获取查询条件，并将json格式的数据转CRecord记录
	 * @return
	 * @throws Exception
	 */
	private Object rpt(SQLExecQuery eq,Cells cell,QueryEntity qe,String cellid,Cells celltj,CRecord cRecord) throws Exception{
		HVector vd = getRPTInfo(eq,cell,qe.getType() ==2);//根据对象标识获取RPT定义
		UICData data = new UICData(cellid);//返回的数据集
		if(vd.size()>0) {
			CRptItem item = (CRptItem)vd.elementAt(0);//RPT报表中的第一个定义，目前只处理第一个，其他的不处理
			String sql = item.cibds;//SQL语句
			sql = sql.replaceAll("\r", " ");
			sql = sql.replaceAll("\n", " ");
			int x0 = sql.indexOf(";");//一个定义里面可能有多条sql，用分号分割
			if(x0==0) {//去掉开头是;
				sql = sql.substring(1);
			}else if(x0>0) {
				sql = sql.substring(0,x0);//多个sql语句，sql;sql...,现在只取第一个
			}
			CRefBas cc = new CRefBas(null, celltj,cRecord.getValues());//查询条件
			WebServeCRef osv = new WebServeCRef(cc,eq);//SQL变量对象
			//格式化SQL条件{xx=xx} and {xx=xx}
			sql = SSTool.formatVarMacro(sql, eq, osv);
			//将SQL语句按照page分页
			SQLInfoE ss = SQLUtils.makeSqlInfo(sql, qe, eq.db_type);
//			ExecutorService executor = Executors.newCachedThreadPool();//开启线程
//			// CompletionService异步获取并行任务执行结果
//			CompletionService<HVector> completionService = new ExecutorCompletionService<HVector>(executor);
			//查询总数的SQL
			String totalSQL = ss.getTotalSql();
			_log.info(totalSQL);
			//执行查询总数sql，并将结果转化为int
			int total = CCliTool.objToInt(eq.queryOne(totalSQL),0);
			//给返回结果CData设置页码信息
			data.setPage(qe.getPage());
			//设置总条数
			data.getPage().setTotal(total);
			long l1 = System.currentTimeMillis();
			if(total>0) {//如果总条数大于0，则获取分页数据
				Cells cell0 = getUICells(cell,item);
//				RPTCallablePaging callable = new RPTCallablePaging(eq,ss.getPagingSql());
//				completionService.submit(callable);
//				executor.shutdown();
//				HVector v0 = completionService.take().get();
				//查询分页数据，返回Hvector集合
				HVector v0 = eq.queryVec(ss.getPagingSql());
				//将Hvector集合转换为前端UIRecord的List集合
				List<UIRecord> listData = DataTools.valuesToJsonArray2(v0,cell0, 0,null,true);
				//将转换的List集合给CData
				data.setData(listData);
				long l2 = System.currentTimeMillis();
				_log.info("执行总时间："+(l2-l1));
			}
			
		}
		return data;
	}
	
	/**
	 * 执行外部SQL查询
	 * @param eq		数据库连接
	 * @param cell		获取展示对象cell定义
	 * @param qe		Web端查询对象
	 * @param cellid	要查询的cellID
	 * @param celltj	获取查询条件cell定义
	 * @param cRecord	获取查询条件，并将json格式的数据转CRecord记录
	 * @return
	 * @throws Exception
	 */
	private Object externalSQL(SQLExecQuery eq,Cells cell,QueryEntity qe,String cellid,Cells celltj) throws Exception{
		long l1 = System.currentTimeMillis();
		String vgs = loadUDF(eq,cell,eq.i_co);//根据对象标识获取RPT定义
		UICData data = new UICData(cellid);//返回的数据集
		PageInfo page = qe.getPage();
		JSONObject json = JSONObject.parseObject(qe.getCont());
		json = json.getJSONObject("data");
		if(json == null){
			json = new JSONObject();
		}
		if (vgs instanceof String) {
			CDBLINK dbo = CDBLINK.toDBLink((String) vgs, null);
			String s0 = dbo.bds, xmap = null;
			int x0 = s0.charAt(0) == '{' ? s0.indexOf('}') : -1;
			if (x0 > 0) {
				xmap = s0.substring(1, x0);// ;-输出对照表:f0=x0,f1=x1,f2=x2,....;中间用","或";"隔开
				s0 = s0.substring(x0 + 1).trim();
			}
			json.put("currentpage", page.getCurrPage());
			json.put("pagesize", page.getPageSize());
			String sql = externalFormatSQL(s0, json, s0.indexOf("{"));
			HVector vd = eq.queryVec(sql);
			data.setPage(qe.getPage());
			data.getPage().setTotal(0);
			if(vd !=null && vd.size()>0){
				Cells cell0 = getUICells(cell,xmap);
				Object[] obj = (Object[]) vd.elementAt(0);
				int total = CCliTool.objToInt(obj[obj.length-1],0); 
				//设置总条数
				data.getPage().setTotal(total);
				List<UIRecord> listData = DataTools.valuesToJsonArray2(vd,cell0, 0,null,true);
				//将转换的List集合给CData
				data.setData(listData);
				
			}
			long l2 = System.currentTimeMillis();
			_log.info("执行总时间："+(l2-l1));
			return data; 
		} 
		return null;
	}

	/**
	 * 根据CRptItem的maps、s_chk获取对照Cells对象的db_cells，all_cels
	 * @param cell 界面的Cells对象
	 * @param item 查询的当前行CRptItem
	 * @return 返回对照Cells
	 * 2019-07-29 18:01:14
	 */
	private Cells getUICells(Cells cell, CRptItem item) {
		Cells cell0 = new Cells(cell.tableName);
		cell0.obj_id = cell.obj_id;
		String s1 = item.s_chk;
		HVector h1 = CCliTool.divide(s1, ',');
		String s2 = item.x_maps;
		HVector h2  = CCliTool.divide(s2, ';');
		Cell[] cc = new Cell[h1.size()+h2.size()];
		for(int i=0;i<h1.size();i++) {
			String s = h1.elementAt(i).toString();
			String[] st = s.split(":");
			int u1 = CCliTool.objToInt(st[0], 0);
			int t1 = CCliTool.objToInt(st[1], 0);
			Cell c1 = cell.all_cels[u1];
			cc[t1] = c1;
		}
		for(int i=0;i<h2.size();i++) {
			String s = h2.elementAt(i).toString();
			String[] st = s.split(":");
			String u1 = st[0];
			int t1 = CCliTool.objToInt(st[1], 0);
			int n1 = cell.nameToIndex(u1);
			if(n1>-1) {
				Cell c1 = cell.all_cels[n1];
				cc[t1] = c1;
			}
		}
		cell0.db_cels = cc;
		cell0.all_cels = cc;
		return cell0;
	}

	/**
	 * 根据CRptItem的maps、s_chk获取对照Cells对象的db_cells，all_cels
	 * @param cell 界面的Cells对象
	 * @param item 查询的当前行CRptItem
	 * @return 返回对照Cells
	 * 2019-07-29 18:01:14
	 */
	private Cells getUICells(Cells cell, String item) {
		Cells cell0 = new Cells(cell.tableName);
		cell0.obj_id = cell.obj_id;  
		String s2 = item;
		HVector h2  = CCliTool.divide(s2, ';');
		Cell[] cc = new Cell[h2.size()]; 
		for(int i=0;i<h2.size();i++) {
			String s = h2.elementAt(i).toString();
			String[] st = s.split(":");
			String u1 = st[0];
			int t1 = CCliTool.objToInt(st[1], 0);
			int n1 = cell.nameToIndex(u1);
			if(n1>-1) {
				Cell c1 = cell.all_cels[n1];
				cc[t1] = c1;
			}
		}
		cell0.db_cels = cc;
		cell0.all_cels = cc;
		return cell0;
	}
	/**
	 * 根据CEllID获取rpt文件或者是自定义txt文件
	 * @param eq 数据库连接对象
	 * @param cell cell对象
	 * @param brpt 是否是RPT报表
	 * @return 返回文件定义内容CRptItem 集合
	 * @throws IOException
	 * @throws FileNotFoundException
	 * 2019-07-30 18:22:00
	 */
	public static HVector getRPTInfo(SQLExecQuery eq, Cells cell,boolean brpt)
			throws IOException, FileNotFoundException {
		HVector hd = new HVector();//新建返回集合
		//读取RPT或者自定义的txt文件
		File f0 = SSTool.getFile(SSTool.dirRPT(true, eq.db_id, 0, 0), getRPTUDF(cell.obj_id, brpt), eq.i_co, eq.db_type);
		if(f0.exists()) {
			//读取文件
			byte[] rr = CCliTool.readFull(new FileInputStream(f0), (int)f0.length());
			//从内存中读取
			ReadLine rl = new ReadLine(new ByteArrayInputStream(rr));
			CRptItem ri = null;
			String s0;
			while ((s0 = rl.readLineEx(false)) != null) {
			  s0 = s0.trim();
			//初始化CRptItem,并加入集合hd
			  if (s0.length() > 0)
			   ri = CRptItem.initialize(hd, ri, s0, cell);
			 }
		}
		return hd;
	}
	/**
	 * 加载自定义外部SQL文本
	 */
	public String loadUDF(SQLExecQuery eq,Cells cell, int i_co) throws Exception { 
		File f0 = SSTool.getFile(SSTool.dirUDF(true, eq.db_id, 0, 0), getRPTUDF((String) cell.obj_id, false), i_co, CPubTool.objToInt(eq.db_type, 0));
		byte[] bs0 = f0.exists() ? CPubTool.readFull(new FileInputStream(f0), (int) f0.length()) : null;
		int cb = bs0 == null ? 0 : bs0.length;
		String s0 = cb < 3 ? null : CCliTool.byteToString(bs0, cb,
				cl.INN.ENC_GBK, true, false);// ;--SQL不要转化。
		return s0 == null || s0.length() < 3 ? "" : s0;// ;-可以单独指定一个字符关闭该功能。
	}
	/**
	 * 获取RPT或者是自定义文件名
	 * @param sobj 对象ID
	 * @param brpt 是否是RPT
	 * @return 返回文件名
	 * 2019-07-31 09:08:33
	 */
	public static String getRPTUDF(String sobj, boolean brpt) {
		 return cl.ICL.PRE_BUID + sobj.replace('-', '_') + (brpt ? ".rpt" : ".txt");
	}
	
	/**
	 * 格式化SQL语句中可变引用条件,调用者需要实现格式化项的转化
	 * 注意标识是否允许每一项不为空。 
	 * xf:第一个"{"位置。
	 */
	public static String externalFormatSQL(String sql,JSONObject json, int xf) {
	 String s0, s1, st0, sk;
	 int x00, xt, cc, t0;
	 boolean byy; 
	 while (xf > -1) {
	  xt = CBasTool.indexOf(sql, '}', xf, true, false);
	  if (xt < 0)
	   break;
	  st0 = sql.substring(xf + 1, xt);
	  byy = st0.charAt(0) == cl.INN.CH_SQLVAR_REF;//;--非SQL格式
	  if (byy)
	   st0 = st0.substring(1);
	  x00 = -1;
	  st0 = json.getString(st0);
	  st0 = st0 ==null?"":st0;
	  st0 = st0.equals("")?"''":"'"+st0+"'";
//	   st0 = osv.proc_SqlVar(st0);

	  if (x00 == GOException.EXIT)
	   return null;//放弃本次SQL
	  if (x00 == GOException.SKIP)
	   x00 = xt + 1;//跳到下一项
	  else if (byy) {
	   if (st0 == null)
	    st0 = "null";//单值为空时传入null
	   st0 = sql.substring(0, xf) + st0;
	   x00 = st0.length();
	   sql = st0 + sql.substring(xt + 1);//引用。
	  } else {
	   for (xf--;xf > -1 && sql.charAt(xf) <= ' ';xf--);
	   if (xf < 0) {
	    s0 = "";
	    x00 = 0;
	   } else {
	    x00 = xf + 1;
	    s0 = sql.substring(0, x00);
	   }
	   cc = sql.length();
	   for (xt++;xt < cc && sql.charAt(xt) <= ' ';xt++);
	   s1 = xt < cc ? sql.substring(xt) : "";
	   xt = s1.length();
	   if (st0 == null || st0.length() < 1) {
	    t0 = -1;
	    if (xt > 0) {
	     sk = nexttoken(s1, true);
	     if (sk.charAt(0) == ')')
	      t0 = 0;//--检查 "or"的组合项。
	     else {
	      sk = sk.toLowerCase();
	      if ("and".equals(sk) || "or".equals(sk)) {
	       s1 = s1.substring(sk.length());
	       xt = s1.length();
	       t0 = 1;
	      }
	     }
	    }
	    if (t0 < 1 && x00 > 0) {
	     sk = nexttoken(s0, false).toLowerCase();//--检查删除全面的关键字。
	     if (t0 == 0 && sk.charAt(0) == '(') {
	      s0 = s0.trim();//---...( )...
	      x00 = s0.length() - 1;
	      while (x00 >= 0 && xt > 0 && s0.charAt(x00) == '(' && s1.charAt(0) == ')') {
	       s0 = s0.substring(0, xf).trim();//--去括号
	       s1 = s1.substring(1).trim();
	       x00 = s0.length() - 1;
	       xt = s1.length();
	      }
	      sk = nexttoken(s0, false).toLowerCase();
	      if (xt > 2 && "where".equals(sk)) {
	       sk = nexttoken(s1, true).toLowerCase();
	       if ("and".equals(sk) || "or".equals(sk)) {
	        s1 = s1.substring(sk.length());//--过滤后面的关键字。
	        xt = s1.length();
	        sk = "";//--where在后面还有其它条件计算。
	       }
	      }
	      x00 = s0.length();//--新的终止位置。
	     }
	     if (t0 == 0 || "and".equals(sk) || "or".equals(sk) || "where".equals(sk)) {
	      t0 = sk.length();
	      if (t0 > 0) {
	       x00 -= t0;
	       s0 = s0.substring(0, x00);
	      }
	     }
	    }
	    if (x00 > 0) {
	     sql = s0;
	     if (xt > 0)
	      sql += s1;
	    } else
	     sql = s1;
	   } else {
	    sql = x00 > 0 ? s0 + " " + st0 : st0;
	    x00 = sql.length();
	    if (xt > 0)
	     sql += " " + s1;
	   }
	  }
	  xf = CBasTool.indexOf(sql, '{', x00, true, false);
	 }
	 return sql;
	}
	
	/**
	 * 按空格取项。
	 */
	public static String nexttoken(String sv0, boolean bnext) {
	 int x1 = sv0.length() - 1;
	 char[] cs0 = sv0.toCharArray();
	 if (bnext) {
	  if (cs0[0] == ')')
	   return ")";
	  for (int i = 1;i < x1;i++) {
	   if (cs0[i] <= ' ')
	    return sv0.substring(0, i);
	  }
	 } else {
	  if (cs0[x1] == '(')
	   return "(";
	  for (int i = x1--;i >= 0;i--) {
	   if (cs0[i] <= ' ')
	    return sv0.substring(i + 1);
	  }
	 }
	 return sv0;
	}
	/**
	 * @return
	 */
	public static Object getUDFValue(int index,Cells cells,Object[] values,SQLExecQuery eq) {
		Cell cc = cells.all_cels[index];
		String script = cc.script==null?"":cc.script;
		ServScript sp = new ServScript(cells);
		sp.initRows(values);
		
		sp.setSQLQuery(eq);
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
}
