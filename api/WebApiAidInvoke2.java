package inetbas.web.outsys.api;

import inetbas.cli.cutil.CCliTool;
import inetbas.pub.coob.Cell;
import inetbas.pub.coob.Cells;
import inetbas.serv.csys.DBInvoke;
import inetbas.sserv.SQLExecQuery;
import inetbas.sserv.SSTool;
import inetbas.web.outsys.api.uidata.UICData;
import inetbas.web.outsys.api.uidata.UIRecord;
import inetbas.web.outsys.entity.BipInsAidNew;
import inetbas.web.outsys.entity.BipInsAidType;
import inetbas.web.outsys.entity.QueryEntity;
import inetbas.web.outsys.redis.RedisHelper;
import inetbas.web.outsys.tools.CellsSessionUtil;
import inetbas.web.outsys.tools.CommUtils;
import inetbas.web.outsys.tools.SQLFiledInfo;
import inetbas.web.outsys.tools.SQLInfoE;
import inetbas.web.outsys.tools.SQLUtils;
import inetbas.web.outsys.uiparam.LayCell;
import inetbas.web.outsys.uiparam.LayCells;
import inetbas.webserv.WebAppPara;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import inet.HVector;

/**
 * 辅助、常量、自定义sql、变量查询服务
 * 
 * @author www.bip-soft.com
 *
 */
public class WebApiAidInvoke2 extends DBInvoke {
	private static Logger _log = LoggerFactory.getLogger(WebApiAidInvoke2.class);
	public static final int AID_I = 200;// 获取辅助基本信息
	public static final int AID_DATA = 210;// 获取辅助数据
	public static final int AID_CL = 300;// 获取常量数据
	public static final int AID_GDIC_UNIT = 400;// 获取商品核算单位信息
	private static final String REDISAID = ".BipInsAidNew.";// 储存在redis中的 辅助部分标识
	private static final String REDISAIDCL = ".BipInsAidNew.CL.";// 储存在redis中的 常量部分标识
	private static HashMap<BipInsAidType, BipInsAidType> sqlTypes = new HashMap<BipInsAidType, BipInsAidType>();
	static {
		sqlTypes.put(BipInsAidType.CSelectEditor, BipInsAidType.CSelectEditor);
		sqlTypes.put(BipInsAidType.CDynaEditor, BipInsAidType.CDynaEditor);
		sqlTypes.put(BipInsAidType.CGroupEditor, BipInsAidType.CGroupEditor);
		sqlTypes.put(BipInsAidType.CGDicEditor, BipInsAidType.CGDicEditor);
//		sqlTypes.put(BipInsAidType.CSelectEditor, BipInsAidType.CSelectEditor);
//		sqlTypes.put(BipInsAidType.CSelectEditor, BipInsAidType.CSelectEditor);
	}

	public Object processOperator(SQLExecQuery eq, WebAppPara wa) throws Exception {
		int id = wa.oprid;
		String oid = CCliTool.objToString(wa.params[0]);
		if (oid != null) {
			if (id == AID_I) {
				return getBipInsAidInfoById(eq, oid, true);
			} else if (id == AID_DATA) {
				QueryEntity qe = (QueryEntity) wa.params[1];
				return getBipInsAidDatas(eq, oid, qe);
			} else if (id == AID_GDIC_UNIT) {
				return getGdicUnitInfoByGbm(eq, oid);
			} else if (id == AID_CL) {
				return getCLInfoById(eq, oid);
			}
		}
		return null;
	}

	/**
	 * 根据常量ID获取常量信息
	 * @param eq 数据库链接
	 * @param clid 常量ID
	 * @return
	 * @throws Exception
	 * 2019-07-11 17:39:59
	 */
	public Object getCLInfoById(SQLExecQuery eq, String clid) throws Exception {
		String key = eq.db_id + REDISAIDCL + clid;
		String ass = RedisHelper.get(key);
		BipInsAidNew bipInsAid = null;
		if (ass != null) {
			bipInsAid = JSONObject.parseObject(ass, BipInsAidNew.class);
			return bipInsAid;
		}
		synchronized (clid.intern()) {
			//二次从Redis取值
			if (ass != null) {
				bipInsAid = JSONObject.parseObject(ass, BipInsAidNew.class);
			} else {
				// 从数据库根据常量ID获取信息
				String cl = SSTool.loadConst1(eq, clid);
				bipInsAid = new BipInsAidNew();
				bipInsAid.setSlink(cl);
				bipInsAid.setCl(true);
				HVector hh = null;
				if (cl.startsWith("{")) {
					cl = cl.substring(1, cl.length() - 1);
					hh = CCliTool.divide(cl, ';');
					if (hh != null) {
						ArrayList<JSONObject> list = getFlagsList(hh);
						// 给FlagEditor赋值
						bipInsAid.setValues(list);
						// 设置total值
						bipInsAid.setTotal(list.size());
						bipInsAid = getFlagCells(bipInsAid);
					}
				} else if (cl.startsWith("select")) {
					cl = SSTool.formatVarMacro(cl,eq);
					_log.info(cl);
					hh = eq.queryVec(cl, true, 100);
					if (hh != null) {
						Cell[] cells = (Cell[]) hh.elementAt(0);
						Cells cells2 = new Cells(clid);
						cells2.setCCells(cells);
						LayCells cells3 = new LayCells(cells2);
						bipInsAid.setCells(cells3);
						ArrayList<JSONObject> list = hvectorToArray(hh, cells3.cels, 1);
						bipInsAid.setValues(list);
					}
				}
			}
		}
		return bipInsAid;
	}

	public Object getBipInsAidDatas(SQLExecQuery eq, String oid, QueryEntity qe) throws Exception {
		BipInsAidNew bAidNew = getBipInsAidInfoById(eq, oid, false);
		if (bAidNew != null && qe != null) {
			if (sqlTypes.containsKey(bAidNew.getbType())) {
				// SQL语句辅助查询
				getBipInsAidDatasBySqls(eq, qe, bAidNew);
			} else if (BipInsAidType.CQueryEditor.equals(bAidNew.getbType())) {
				// QueryEditor对象
				return getQueryEditorDatas(eq, qe, bAidNew);
			} else {
				// 其他不做任何操作
				_log.info("非查询类型的辅助:	" + JSON.toJSONString(bAidNew));
			}
		}
		return qe;
	}

	public Object getQueryEditorDatas(SQLExecQuery eq, QueryEntity qe, BipInsAidNew bAidNew) throws Exception {
		Cells[] cells = (Cells[]) CellsSessionUtil.getCellsByCellId(eq.db_id, bAidNew.getSlink());
		if (cells == null) {
			cells = (Cells[]) SSTool.readCCells(eq, bAidNew.getSlink(), false);
			CellsSessionUtil.cacheCells(eq.db_id, bAidNew.getSlink(), cells);
		}
		if (cells == null) {
			return null;
		}
		// 获取主表还是子表数据
		// 获取主表数据
		String st0 = null;
		String sc = "";
		LayCell[] cells2 = null;
		Cells cellm = cells[1];
		if (qe.oprid == 13) {
			// 查询主对象数据
			cells2 = bAidNew.getCells().cels;
			int t0;
			sc = CommUtils.getContStr(cells[0], qe.getCont());
			qe.setCont(null);
			if (sc != null && sc.length() > 0) {
				char c0 = sc.charAt(0);
				if (c0 >= '0' && c0 <= '9') {
					t0 = sc.indexOf('#');// ;-加取行数限制
					if (t0 > 0 && t0 < 5) {
						sc = sc.substring(t0 + 1);
						if (sc.length() > 0)
							c0 = sc.charAt(0);
					}
				}
				if (c0 == ',') {
					t0 = sc.indexOf('&');// ;-检查引用表及关联条件
					st0 = sc.substring(1, t0);
					sc = sc.substring(t0 + 1);
				}
			}
			if (sc != null && sc.length() > 0)
				sc = SSTool.formatVarMacro(sc, eq);
		} else {
			sc = qe.getCont();
			cells2 = bAidNew.getCells().find(qe.getTcell()).cels;
			cellm = cells[1].find(qe.getTcell());
		}
		boolean b0 = false;
		st0 = spelSQL(eq, cellm, b0 ? (Cell.PRIMARY | Cell.LIST) : 0, sc, true, st0, this);
		st0 = SSTool.formatVarMacro(st0, eq);
		SQLInfoE ss = SQLUtils.makeSqlInfo(st0, qe, eq.db_type);
		String totalSQL = ss.getTotalSql();
		String pageSQL = ss.getPagingSql();
		_log.info(totalSQL);
		_log.info(pageSQL);
		int total = CCliTool.objToInt(eq.queryOne(totalSQL), 0);
		UICData data = new UICData(cellm.obj_id);
		data.setPage(qe.getPage());
		data.getPage().setTotal(total);
		if (total > 0) {
			HVector hh = eq.queryVec(pageSQL);
			if (hh != null) {
				for(int i=0;i<hh.size();i++) {
					Object[] o0 = (Object[])hh.elementAt(i);
					UIRecord record = new UIRecord();
					JSONObject jdata = objectsToJsonObj(o0,cells2);
					record.setData(jdata);
					data.add(record, -1);
				}
				ArrayList<JSONObject> list = hvectorToArray(hh, cells2);
				qe.setValues(list);
			} else {
				qe.setValues(new ArrayList<JSONObject>());
			}
		}
		_log.info(totalSQL);
		_log.info(pageSQL);
		return data;
	}

	/***
	 * 获取SQL类型辅助的数据
	 * 
	 * @param eq
	 * @param qe      查询条件
	 * @param bAidNew
	 * @throws Exception 2019-07-11 13:55:38
	 */
	public void getBipInsAidDatasBySqls(SQLExecQuery eq, QueryEntity qe, BipInsAidNew bAidNew) throws Exception {
		// 去掉SQL中的[0+]...
		String s0 = CommUtils.formartSql(bAidNew.getSlink());
		_log.info(s0);
		// 宏格式化SQL
		if (BipInsAidType.CGroupEditor.equals(bAidNew.getbType())) {
			// 如果是分组查询，需要替换
			String gfild = bAidNew.getGroupFld();
			String gv = qe.getGroupV();
			gv = gv == null ? "" : gv.trim();
			if (gfild != null) {
				if (gv.length() > 0) {
					LayCell cell = getLayCellById(bAidNew, gfild);
					if (cell != null) {
						if (cell.type >= 2 && cell.type < 12) {
							gfild = gfild + "=" + gv;
						} else {
							gfild = gfild + "='" + gv + "'";
						}
						s0 = s0.replaceAll("1=1", gfild);
					}
				} else {
					qe.setValues(new ArrayList<JSONObject>());
				}
			}
		}
		s0 = SSTool.formatVarMacro(s0, eq);
		// 获取分页SQL对象信息，包含total，和paging
		SQLInfoE sE = SQLUtils.makeSqlInfo(s0, qe, eq.db_type);
		String total = sE.getTotalSql();
		String page = sE.getPagingSql();
		_log.info(total);
		_log.info(page);
		// 获取数据总数，如果是null则转0
		int num = CCliTool.objToInt(eq.queryOne(total), 0);
		// 设置返回的总条数
		qe.getPage().setTotal(num);
		int size = qe.getPage().getPageSize();// 每页条数
		size = size <= 0 ? 20 : size;
		int _t = (int) Math.ceil((double) num / size);
		if (num > 0 && _t >= qe.getPage().getCurrPage()) {
			HVector hh = eq.queryVec(page);
			if (hh != null) {
				LayCell[] cells = bAidNew.getCells().cels;
				ArrayList<JSONObject> list = hvectorToArray(hh, cells);
				if (qe.getValues() != null) {
					qe.getValues().clear();
				}
				qe.setValues(list);
			}
		} else {
			qe.setValues(new ArrayList<JSONObject>());
		}
	}

	/**
	 * 根据字段ID 获取LayCell字段
	 * 
	 * @param bAidNew
	 * @param gfild
	 * @return 2019-07-11 11:03:33
	 */
	private LayCell getLayCellById(BipInsAidNew bAidNew, String gfild) {
		LayCell[] cells = bAidNew.getCells().cels;
		LayCell c1 = null;
		if (cells != null) {
			for (int i = 0; i < cells.length; i++) {
				LayCell c2 = cells[i];
				if (c2.id.equals(gfild)) {
					c1 = c2;
					break;
				}
			}
		}

		return c1;
	}

	/**
	 * 将数据集转换为ArrayList<JSONObject>
	 * 
	 * @param hh    sql查询数据集
	 * @param cells 界面Cells
	 * @return 返回ArrayList<JSONObject> 2019-07-11 10:45:28
	 */

	public ArrayList<JSONObject> hvectorToArray(HVector hh, LayCell[] cells) {
		return hvectorToArray(hh, cells, 0);
	}

	public ArrayList<JSONObject> hvectorToArray(HVector hh, LayCell[] cells, int start) {
		ArrayList<JSONObject> list = new ArrayList<JSONObject>();
		for (int i = start; i < hh.size(); i++) {
			Object[] oo = (Object[]) hh.elementAt(i);
			JSONObject jo = new JSONObject();
			for (int m = 0; m < cells.length && m < oo.length; m++) {
				LayCell cell = cells[m];
				if (cell.type == 91 || cell.type == 93) {
					jo.put(cell.id, CCliTool.dateToString(oo[m], true, cell.type == 91 ? 1 : 8));
				} else
					jo.put(cell.id, oo[m]);
			}
			list.add(jo);
		}
		return list;
	}
	
	public static JSONObject objectsToJsonObj(Object[] oo, LayCell[] cells) {
		JSONObject jo = new JSONObject();
		for (int m = 0; m < cells.length && m < oo.length; m++) {
			LayCell cell = cells[m];
			if (cell.type == 91 || cell.type == 93) {
				jo.put(cell.id, CCliTool.dateToString(oo[m], true, cell.type == 91 ? 1 : 8));
			} else
				jo.put(cell.id, oo[m]);
		}
		return jo;
	}

	/**
	 * 获取产品换算单位信息
	 * 
	 * @param eq  SQL链接
	 * @param gbm 产品编码
	 * @return
	 * @throws Exception 2019-07-11 17:23:58
	 */
	public Object getGdicUnitInfoByGbm(SQLExecQuery eq, String gbm) throws Exception {
		String sql = "select bzunit,hsgx from gdichs where gbm='" + gbm + "'";
		HVector hh = eq.queryVec(sql);
		if (hh != null) {
			ArrayList<JSONObject> list = new ArrayList<JSONObject>();
			for (int i = 0; i < hh.size(); i++) {
				Object[] oo = (Object[]) hh.elementAt(i);
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("unit", CCliTool.objToString(oo[0]));
				jsonObject.put("hsgx", CCliTool.objToFloat(oo[1], 1f));
				list.add(jsonObject);
			}
			return list;
		}
		return null;
	}

	/***
	 * 根据辅助ID查找辅助信息
	 * 
	 * @param eq 数据库连接信息
	 * @param id 辅助ID
	 * @return 辅助对象 2019-07-08 09:42:55
	 * @throws Exception
	 */
	public static BipInsAidNew getBipInsAidInfoById(SQLExecQuery eq, String id, boolean binit) {
		// 从redis中获取辅助定义
		BipInsAidNew bipInsAid = null;
		// Redis中存储的Key值
		String key = eq.db_id + REDISAID + id;
		// 从redis中获取辅助定义
		String ass = RedisHelper.get(key);
		if (ass != null) {
			bipInsAid = JSONObject.parseObject(ass, BipInsAidNew.class);
			if (binit)
				bipInsAid.setSlink(null);
			return bipInsAid;
		}
		synchronized (key.intern()) {
			// 二次获取
			ass = RedisHelper.get(key);
			if (ass != null) {
				bipInsAid = JSONObject.parseObject(ass, BipInsAidNew.class);
				if (binit)
					bipInsAid.setSlink(null);
				return bipInsAid;
			}
			// 从辅助表中根据辅助ID,查出对应的辅助定义
			String sql = "select slabel,slink,sflag,sclass,sref from insaid where sid='" + id + "'";
			try {
				Object[] o0 = eq.queryRow(sql, false);
				if (o0 != null) {
					bipInsAid = new BipInsAidNew();
					bipInsAid.setId(id);
					bipInsAid.setTitle(CCliTool.objToString(o0[0]));// 标题
					bipInsAid.setSlink(CCliTool.objToString(o0[1]));// 初始SQL
					bipInsAid.setSref(CCliTool.objToString(o0[4]));// sref
					bipInsAid.setSflag(CCliTool.objToString(o0[2]));// sflag
					String sclass = CCliTool.objToString(o0[3]);// 类名
					initBipAidType(bipInsAid, sclass);
					bipInsAid = initAidCells(eq, bipInsAid);
					RedisHelper.setNoTime(key, JSON.toJSONString(bipInsAid));
					if (binit)
						bipInsAid.setSlink(null);
				}
			} catch (Exception e) {
				e.printStackTrace();
				bipInsAid = null;
			}
		}
		return bipInsAid;
	}

	/**
	 * @param bipInsAid 2019-07-08 17:20:54
	 * @throws Exception
	 */
	private static BipInsAidNew initAidCells(SQLExecQuery eq, BipInsAidNew bipInsAid) throws Exception {
		BipInsAidType type = bipInsAid.getbType();
		// 辅助类型是否是SQL查询
		if (sqlTypes.containsKey(type)) {
			bipInsAid.mklables(bipInsAid.getSflag());//// 显示列下标//显示列名称//列标签 ---- 辅助中的标识
			// 初始化SQLEditor
			bipInsAid = initSQLEditor(eq, bipInsAid);
		} else if (BipInsAidType.CQueryEditor.equals(type)) {
			initQueryEditor(eq, bipInsAid);
		} else if (type.equals(BipInsAidType.CFlagEditor)) {
			// 初始化flag标签的Cell组成
			getFlagCells(bipInsAid);
			bipInsAid = initFlagEditor(eq, bipInsAid);
		}
		return bipInsAid;
	}

	/**
	 * 初始化FlagEditor
	 * 
	 * @param eq        数据库连接
	 * @param bipInsAid 辅助对象
	 * @return 返回辅助信息
	 * @throws Exception SQL错误 2019-07-10 10:10:03
	 */
	public static BipInsAidNew initFlagEditor(SQLExecQuery eq, BipInsAidNew bipInsAid) throws Exception {
		// Flag编辑器
		// 获取关联定义
		// 关联有可能是 sql:一条sql语句
		// 关联有可能是 EX:MATTR(长文本定义)
		// 关联有可能是 1:周六;2:周日;3:日期
		// 关联有可能是 QC:期初;QM:期末;FSD:借方发生;FSC:贷方发生;FS:净发生;LJD:累计借方;LJC:累计贷方;LJ:累计净额;JH:计划
		// 关联有可能是 $:CL.CREDFL（常量定义）
		String slink = bipInsAid.getSlink();
		// 判断是否存在连接
		if (slink != null) {
			HVector hh = null;
			if (slink.startsWith("sql:")) {
				// 查表SQL
				slink = slink.substring(4);
				hh = eq.queryVec(slink);
			} else if (slink.startsWith("EX:")) {
				// 数据太长了，定义到长文本
				slink = slink.substring(3);
				// 获取长文本数据
				slink = SSTool.loadMulTxt(eq, slink);
				hh = CCliTool.divide(slink, ';');
			} else if (slink.charAt(0) == '$') {// 常量
				// 去掉常量标记
				slink = slink.substring(2);
				// 有可能是{xx;xxx;xxxx;}
				if (slink.startsWith("{")) {
					slink = slink.substring(1, slink.length() - 1);
				}
				// 获取常量值
				slink = SSTool.loadConst1(eq, slink);
				if (slink.startsWith("{")) {
					slink = slink.substring(1, slink.length() - 1);
				}
				hh = CCliTool.divide(slink, ';');
			} else {
				// 正常再辅助中定义的关联
				hh = CCliTool.divide(slink, ';');
			}
			if (hh != null) {
				ArrayList<JSONObject> list = getFlagsList(hh);
				// 给FlagEditor赋值
				bipInsAid.setValues(list);
				// 设置total值
				bipInsAid.setTotal(list.size());
			}
		}
		return bipInsAid;
	}

	public static ArrayList<JSONObject> getFlagsList(HVector hh) {
		ArrayList<JSONObject> list = new ArrayList<JSONObject>();
		// 循环生成数据（一般flagEditor的数据不是很多）
		for (int i = 0; i < hh.size(); i++) {
			String item = CCliTool.objToString(hh.elementAt(i));
			item = item == null ? "" : item.trim();
			if (item.length() > 0) {
				JSONObject jo = new JSONObject();
				if (item.indexOf(":") > -1) {
					String[] bb = item.split(":");
					jo.put("code", bb[0]);
					jo.put("name", bb[1]);
				} else {
					jo.put("code", (i + 1) + "");
					jo.put("name", item);
				}
				list.add(jo);
			}
		}
		return list;
	}

	/**
	 * 初始化QueryEditor的对象定义
	 * 
	 * @param eq
	 * @param bipInsAid
	 * @return 2019-07-10 10:06:47
	 * @throws Exception
	 */
	public static void initQueryEditor(SQLExecQuery eq, BipInsAidNew bipInsAid) throws Exception {
		String pcell = bipInsAid.getSlink();
		Object o0 = SSTool.readCCells(eq, pcell, false);
		if (o0 != null) {
			CellsSessionUtil.cacheCells(eq.db_id, bipInsAid.getSlink(), o0);
			if (o0 instanceof Cells[]) {
				Cells[] cc = (Cells[]) o0;
				LayCells lay1 = new LayCells(cc[0]);
				LayCells lay2 = new LayCells(cc[1]);
				bipInsAid.setContCells(lay1);
				bipInsAid.setCells(lay2);
			}
		}
	}

	private static BipInsAidNew getFlagCells(BipInsAidNew aidNew) {
		Cells cells = new Cells(aidNew.getId());
		Cell[] cells2 = new Cell[2];
		Cell c1 = new Cell("code", 12);
		c1.labelString = "编码";
		Cell c2 = new Cell("name", 12);
		c2.labelString = "名称";
		cells.widthCell = 4;
		cells2[0] = c1;
		cells2[1] = c2;
		cells.setCCells(cells2);
		LayCells cells3 = new LayCells(cells);
		aidNew.setCells(cells3);
		return aidNew;
	}

	/**
	 * 初始化SQL类型的的辅助 查询字段信息，显示信息
	 * 
	 * @param eq
	 * @param bipInsAid
	 * @param type      2019-07-09 14:58:17
	 */
	public static BipInsAidNew initSQLEditor(SQLExecQuery eq, BipInsAidNew bipInsAid) {
		// 格式化sql 中的【0+】 。。。
		String s0 = getFormatSQL(bipInsAid);
		// 格式化SQL语句 s0的宏定义
		s0 = SSTool.formatVarMacro(s0, eq);
		try {
			// 查询出一行来，带表头即列字段信息，系统已经转化为Cell
			Object[] o0 = eq.queryRow(s0, true);
			SQLInfoE sE = SQLUtils.makeSqlInfo(s0, new QueryEntity(), eq.db_type);
			sE.getFiledInfos();
			// 获取数组返回结果的列信息，在第0项
			Cell[] cels = (Cell[]) o0[0];
			ArrayList<SQLFiledInfo> list = sE.getFiledInfos();
			for(int i=0;i<list.size()&&i<cels.length;i++) {
				SQLFiledInfo sf1 = list.get(i);
				if(!sf1.isBsum()) {
					cels[i].ccName = sf1.getFiledIn();
				}
			}
			// 初始化对象定义
			Cells cell = new Cells(bipInsAid.getId());
			if (BipInsAidType.CGDicEditor.equals(bipInsAid.getbType())) {
				// 如果是货品辅助，最后2个分别为unit（单位）和qtyrt（换算比例）
				cels[cels.length - 2].ccName = "unit";
				cels[cels.length - 1].ccName = "qtyrt";
			}
			cell.setCCells(cels);
			cell.widthCell = 4;
			// 转换成前端对象
			LayCells layCells = new LayCells(cell);
			// 将前端对象展示cell给辅助类
			bipInsAid.setCells(layCells);
			
			// 设置显示列
			int[] sh = bipInsAid.getShowColsIndex();
			if (sh != null) {// 辅助标识（flag）定义了显示列
				// 0,1,2,3,4
				for (int i = 0; i < bipInsAid.getCells().cels.length; i++) {
					LayCell cel = bipInsAid.getCells().cels[i];
					boolean bf = false;
					for (int j = 0; j < sh.length; j++) {
						int k = sh[j];
						if (i == k) {
							bf = true;
							if (bipInsAid.getLabers() != null)
								cel.labelString = bipInsAid.getLabers()[j];
						}
					}
					if (!bf) {
						cel.attr = 0x400;
						cel.isShow = false;
						bf = false;
					}
				}
			} else {
				// 定义了显示标签
				// cbm,cmc,cjm
				String[] lb = bipInsAid.getShowColsName();
				if (lb != null) {
					sh = new int[lb.length];
					for (int i = 0; i < bipInsAid.getCells().cels.length; i++) {
						LayCell cel = bipInsAid.getCells().cels[i];
						boolean bf = false;
						for (int j = 0; j < lb.length; j++) {
							String lb1 = lb[j];
							if (cel.id.equals(lb1)) {
								sh[j] = i;
								bf = true;
							}
						}
						if (!bf) {
							cel.attr |= 0x400;
							cel.isShow = false;
							bf = false;
						}
					}
					bipInsAid.setShowColsIndex(sh);
				} else {
					sh = new int[] { 0, 1 };
					bipInsAid.setShowColsIndex(sh);
					for (int i = 2; i < bipInsAid.getCells().cels.length; i++) {
						LayCell cel = bipInsAid.getCells().cels[i];
						cel.attr |= 0x400;
						cel.isShow = false;
					}
				}
			}
			if (bipInsAid.getAddCellId() != null) {
				Object o1 = SSTool.readCCells(eq, bipInsAid.getAddCellId(), false);
				if (o1 != null) {
					bipInsAid.setAddCells(new LayCells((Cells) (o1)));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			_log.error("辅助查询出错：" + bipInsAid.getId(), e);
			bipInsAid = null;
		}
		return bipInsAid;
	}

	/**
	 * 格式化辅助SQL语句 将GroupEditor中的[cdic]分组字段替换成1=1
	 * 
	 * @param bipInsAid
	 * @return 2019-07-10 11:12:30
	 */
	public static String getFormatSQL(BipInsAidNew bipInsAid) {
		// 1、先格式化SQL语句,将SQL中的#0+，0~，去掉
		String s0 = CommUtils.formartSql(bipInsAid.getSlink());
		// 判断辅助是属于什么类型的
		if (BipInsAidType.CGroupEditor.equals(bipInsAid.getbType())) {
			// 该辅助是分组查询，获取分组查询的字段，并将其替换成 "1=1"
			// 如果是分组查询，查询的最后一个字段是分组字段
			// select bank,bankzh,cdic from cdic_bank where [cdic]
			// select bank,bankzh,cdic from cdic_bank where 1=1
			// 找到from下标
			int i = s0.indexOf("from");
			// 拆出from之前的字段
			String s1 = s0.substring(0, i);
			// 用逗号拆分出最后一个字段
			String[] flds = s1.split(",");
			String s2 = flds[flds.length - 1].trim();
			if (bipInsAid.getGroupFld() == null)
				bipInsAid.setGroupFld(s2);
			_log.info(s2);
			// 组成[cdic],需要转移[]
			String temp = "\\[" + s2 + "\\]";
			// 替换分组字段
			s0 = s0.replaceFirst(temp, "1=1");
		}
		bipInsAid.setSlink(s0);
		return s0;
	}

	/***
	 * 初始化辅助类别
	 * 
	 * @param bipInsAid
	 * @param sclass    2019-07-09 16:27:27
	 */
	public static void initBipAidType(BipInsAidNew bipInsAid, String sclass) {
		if (sclass.endsWith("CSelectEditor")) {
			bipInsAid.setbType(BipInsAidType.CSelectEditor);
		} else if (sclass.endsWith("CGroupEditor")) {
			bipInsAid.setbType(BipInsAidType.CGroupEditor);
		} else if (sclass.endsWith("CFlagEditor")) {
			bipInsAid.setbType(BipInsAidType.CFlagEditor);
		} else if (sclass.endsWith("CDateEditor")) {
			bipInsAid.setbType(BipInsAidType.CDateEditor);
		} else if (sclass.endsWith("CQueryEditor")) {
			bipInsAid.setbType(BipInsAidType.CQueryEditor);
		} else if (sclass.endsWith("CHSMEditor")) {
			bipInsAid.setbType(BipInsAidType.CHSMEditor);
		} else if (sclass.endsWith("CFlowEditor")) {
			bipInsAid.setbType(BipInsAidType.CFlowEditor);
		} else if (sclass.endsWith("CDynaEditor")) {
			bipInsAid.setbType(BipInsAidType.CDynaEditor);
		} else if (sclass.endsWith("CUpDownEditor")) {
			bipInsAid.setbType(BipInsAidType.CUpDownEditor);
		} else if (sclass.endsWith("CYMEditor")) {
			bipInsAid.setbType(BipInsAidType.CYMEditor);
		} else if (sclass.endsWith("CGDicEditor")) {
			bipInsAid.setbType(BipInsAidType.CGDicEditor);
		}
	}

}
