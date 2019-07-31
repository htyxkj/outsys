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
import inetbas.pub.coob.Cell;
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
			HVector vd = getRPTInfo(eq,cell,true);//根据对象标识获取RPT定义
			UICData data = new UICData(cellid);//返回的数据集
			if(vd.size()>0) {
				CRptItem item = (CRptItem)vd.elementAt(0);//RPT报表中的第一个定义，目前只处理第一个，其他的不处理
				String sql = item.cibds;//SQL语句
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
//				ExecutorService executor = Executors.newCachedThreadPool();//开启线程
//				// CompletionService异步获取并行任务执行结果
//				CompletionService<HVector> completionService = new ExecutorCompletionService<HVector>(executor);
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
//					RPTCallablePaging callable = new RPTCallablePaging(eq,ss.getPagingSql());
//					completionService.submit(callable);
//					executor.shutdown();
//					HVector v0 = completionService.take().get();
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
		//获取到文件，判断文件是否存在
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
	 * 获取RPT或者是自定义文件名
	 * @param sobj 对象ID
	 * @param brpt 是否是RPT
	 * @return 返回文件名
	 * 2019-07-31 09:08:33
	 */
	public static String getRPTUDF(String sobj, boolean brpt) {
		 return cl.ICL.PRE_BUID + sobj.replace('-', '_') + (brpt ? ".rpt" : ".txt");
	}
}
