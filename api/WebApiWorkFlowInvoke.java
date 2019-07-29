/**
 * 
 */
package inetbas.web.outsys.api;

import inet.HVector;
import inetbas.CMain;
import inetbas.cli.cutil.CCliTool;
import inetbas.pub.coob.Cells;
import inetbas.pub.exi.CExPubTool;
import inetbas.serv.csys.DBInvoke;
import inetbas.sserv.SQLExecQuery;
import inetbas.sserv.SSTool;
import inetbas.web.outsys.api.uidata.UICData;
import inetbas.web.outsys.api.uidata.UIRecord;
import inetbas.web.outsys.entity.QueryEntity;
import inetbas.web.outsys.redis.RedisHelper;
import inetbas.web.outsys.tools.CellsSessionUtil;
import inetbas.web.outsys.tools.CellsUtil;
import inetbas.web.outsys.tools.CommUtils;
import inetbas.web.outsys.tools.DataTools;
import inetbas.web.outsys.tools.SQLInfoE;
import inetbas.web.outsys.tools.SQLUtils;
import inetbas.web.outsys.uiparam.CWorkCopy;
import inetbas.web.outsys.uiparam.CWorkFlow;
import inetbas.web.outsys.uiparam.LayCells;
import inetbas.webserv.WebAppPara;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @author www.bip-soft.com
 * 2018-10-26 14:33:31
 */
public class WebApiWorkFlowInvoke extends DBInvoke {
	private static Logger _log = LoggerFactory.getLogger(WebApiWorkFlowInvoke.class);
	public final static String WFSTR="WORKFLOW";
	private final static int WORKLIST = 200;
	private final static int WORKDATA_M = 205;
	private final static int WORKDATA_S = 210;
	public Object processOperator(SQLExecQuery eq, WebAppPara wa) throws Exception {
		if(wa.params==null)
			return null;
		if(wa.oprid == WORKLIST) {
			String buidto = CCliTool.objToString(wa.params[0]);
			return getWorlkFlowInf(eq, buidto);
		}else if(wa.oprid == WORKDATA_M) {
			String buidto = CCliTool.objToString(wa.params[0]);
			String buidfr = CCliTool.objToString(wa.params[1]);
			QueryEntity qe = (QueryEntity)wa.params[2];
			return getWorkFlowDatas(eq, buidto, buidfr, qe);
		}else if(wa.oprid == WORKDATA_S) {
			String buidto = CCliTool.objToString(wa.params[0]);
			String buidfr = CCliTool.objToString(wa.params[1]);
			QueryEntity qe = (QueryEntity)wa.params[2];
			return getWorkFlowDatasSub(eq, buidto, buidfr, qe);
		}
		return null;
	}

	/**
	 * @param eq     //获取拷贝定义子表数据
	 * @param buidto 目标业务
	 * @param buidfr 来源业务
	 * @param qe     查询条件
	 * @return 2019-06-27 16:30:54
	 */
	private Object getWorkFlowDatasSub(SQLExecQuery eq, String buidto, String buidfr, QueryEntity qe) throws Exception {
		CWorkFlow cWorkFlow = workFlowByBuidto(eq, buidto, buidfr);
		UICData data = new UICData();
		if (cWorkFlow != null) {
			String pcell = cWorkFlow.getPcell();
			data.setObj_id(data.getObj_id());
			Object oo = CellsSessionUtil.getCellsByCellId(eq.db_id, pcell);
			Cells cells = null;
			if (oo == null) {
				Object o1 = SSTool.readCCells(eq, pcell, false);
				if (o1 instanceof Cells) {
					cells = (Cells) o1;
				}
			} else {
				Cells[] cc = (Cells[]) oo;
				cells = cc[1];
			}
			cells = cells.find(qe.getPcell());
			String sc = qe.getCont();
			String st0 = spelSQL(eq, cells, 0, sc, true, null, this);
			st0 = SSTool.formatVarMacro(st0, eq);
			HVector hv = eq.queryVec(st0);
			if(hv==null) {
				if(qe.getValues()!=null)
					qe.getValues().clear();
			}else {
				System.out.println((cells.attr & cl.ICL.ocNotDelete));
				if((cells.attr & cl.ICL.ocNotDelete)==0) {
					checkLoadData(eq, cWorkFlow, cells, sc, hv);
				}
				
				ArrayList<UIRecord> values = DataTools.valuesToJsonArray2(hv, cells, 0, null, true);
				data.setData(values);
//				ArrayList<JSONObject> values = WebApiPageInvoke.valuesToJsonArray2(hv, cells, 0, null, true);
//				qe.setValues(values);
			}
		}
		return data;
	}

	/**
	 * 检查数据是否已经被引用，用于消减原始数据
	 * @throws Exception
	 * 2019-07-03 10:22:57
	 */
	public void checkLoadData(SQLExecQuery eq, CWorkFlow cWorkFlow, Cells cells, String sc, HVector hv)
			throws Exception {
		String _sid = getPKValue(sc);
		CWorkCopy copy = getCworkCopyByObjId(cells.obj_id, cWorkFlow.getScopys());
		if (copy != null) {
			String s0 = copy.getSfrom();
			String sload = copy.getSload();
			if(sload==null) {
				return ;
			}
			int ccnum = 1, ibv = 0, x0 = -1, x1 = -1;
			String scp = "";
			HVector sto = new HVector();
			if (s0 != null) {
				// ;--数值项修正放在最前面。
				char c0 = s0.charAt(0);
				if (c0 == '#') {
					ccnum = CCliTool.charToInt(s0.charAt(1)); // ;--定义对照项数。
					s0 = s0.substring(2);
				} else if (c0 >= '0' && c0 <= '9') {
					ccnum = c0 - '0';
					s0 = s0.substring(1);
				}
				x0 = s0.indexOf(':');
				if (x0 > 0) {
					scp = s0.substring(0, x0);// ;--单独定义对比。
					s0 = s0.substring(x0 + 1);
				} else {
					x1 = s0.indexOf('/');
					scp = x1 < 0 ? s0 : s0.substring(0, x1);// --直接取。
				}
				if (x0 < 1)
					scp = CommUtils.fieldMap(scp, copy.getSmap());// ;--对照转化
				scp = CommUtils.checkcomp(scp);
				if (_sid != null)
					ibv = CommUtils.calcSQL(sto, sload, 1, _sid, true);
			}
			HVector h1 = new HVector();
			for (int i = 0; i < sto.size() / 2; i++) {
				String s1 = sto.elementAt((i * 2)).toString();
				_log.info(s1);
				HVector hh = eq.queryVec(s1);
				h1.addElement(hh);
			}
			if (h1.size() > 0) {
				x0 = h1.size();
				int[] xs = CCliTool.toIndexs(cells, scp, ',', 0, true, true);
				if (hv != null) {
					int iBIT = 1;
					for (int i = 0;i < x0;i++, iBIT *= 2)
						CExPubTool.calcVector(hv, 0, (HVector) h1.elementAt(i), xs, ccnum, (ibv & iBIT) == 0 ? '-' : '+', CMain.IgnoreCase);
					CExPubTool.filter0(hv, 0, xs, ccnum);
					}
				
			}
		}
	}

	/**
	 * @param sc
	 * @return
	 * 2019-07-02 10:18:39
	 */
	private String getPKValue(String sc) {
		String[] ii = sc.split("=");
		String sv = ii[1];
		if(sv.startsWith("'")) {
			sv = sv.substring(1,sv.length()-1);
		}
		return sv;
	}
	

	/**
	 * @param obj_id
	 * @param scopys
	 * @return
	 * 2019-07-01 19:59:52
	 */
	private CWorkCopy getCworkCopyByObjId(String obj_id, CWorkCopy[] scopys) {
		CWorkCopy cp = null;
		if(scopys!=null) {
			for(int i=0;i<scopys.length;i++) {
				CWorkCopy copy = scopys[i];
				if(copy.getObjId().equals(obj_id)) {
					cp = copy;
					break;
				}
			}
		}
		return cp;
	}

	public Object getWorkFlowDatas(SQLExecQuery eq, String buidto, String buidfr, QueryEntity qe) throws Exception {
		CWorkFlow cWorkFlow = workFlowByBuidto(eq, buidto,buidfr);
		UICData cData = new UICData();
		if(cWorkFlow != null) {
			String pcell = cWorkFlow.getPcell();
			Object oo = CellsSessionUtil.getCellsByCellId(eq.db_id, pcell);
			Cells cells = null;
			Cells tcells = null;
			if(oo==null) {
				Object o1 = SSTool.readCCells(eq, pcell, false);
				if(o1 instanceof Cells) {
					cells = (Cells)o1;
				}
				tcells = (Cells)SSTool.readCCells(eq, qe.getTcell(), false);
				CellsUtil.initCells(tcells);
			}else {
				Cells[] cc = (Cells[])oo;
				cells = cc[1];
				tcells = cc[0];
				tcells.condiction = SSTool.formatVarMacro(tcells.condiction, eq);
			}
			cData.setObj_id(cells.obj_id);
			cData.setPage(qe.getPage());
			if(cells!=null) {
				String flag = cWorkFlow.getFlag();
				String sc = "("+flag+"=0 or "+flag+" is null)";
				String sc1 = CommUtils.getContStr(tcells, qe.getCont());
				qe.setCont(null);
				String st0 = spelSQL(eq, cells, 0, sc+" and ("+sc1+")", true, null, this);
				st0 = SSTool.formatVarMacro(st0, eq);
				_log.info(st0);
				SQLInfoE ss = SQLUtils.makeSqlInfo(st0,qe,eq.db_type);
				String total = ss.getTotalSql();
				_log.info(total);
				int totalSize = CCliTool.objToInt(eq.queryOne(total),0);
				if(totalSize>0) {
					qe.getPage().setTotal(totalSize);
					HVector v0 = eq.queryVec(ss.getPagingSql());
					
					
					ArrayList<UIRecord> values = DataTools.valuesToJsonArray2(v0,cells,0,null,true);
					cData.setData(values);
					cData.setPage(qe.getPage());
//					ArrayList<JSONObject> values = WebApiPageInvoke.valuesToJsonArray(v0,cells,0,null,true);
//					qe.setValues(values);
					_log.info(ss.getPagingSql());
				}
			}
		}
		return cData;
	}

	@SuppressWarnings("unchecked")
	public CWorkFlow workFlowByBuidto(SQLExecQuery eq, String buidto,String buidfr) throws Exception {
		String buidListString = RedisHelper.get(eq.db_id+"."+WFSTR+"."+buidto);
		List<CWorkFlow> wFlows = null;
		CWorkFlow cWorkFlow = null;
		if(buidListString==null) {
			Object o0 = getWorlkFlowInf(eq, buidto);
			if(o0!=null)
				wFlows = (List<CWorkFlow>) o0;
		}else {
			wFlows = JSONObject.parseArray(buidListString, CWorkFlow.class);
		}
		if(wFlows!=null) {
			for (CWorkFlow cWorkFlow1 : wFlows) {
				if(cWorkFlow1.getBuidfr().equals(buidfr)) {
					cWorkFlow = cWorkFlow1;
					break;
				}
			}
		}
		return cWorkFlow;
	}

	/***
	 * 通过目标业务号，获取工作流定义中的来源业务
	 * 
	 * @param eq     数据库连接
	 * @param buidto 目标业务号
	 * @return 返回相应的业务信息 2018-10-26 14:36:41
	 * @throws Exception
	 */
	public Object getWorlkFlowInf(SQLExecQuery eq, String buidto) throws Exception {
		String infos = RedisHelper.get(eq.db_id+"."+WFSTR+"."+buidto);
		List<CWorkFlow> wFlows = null;
		if(infos==null) {
			synchronized (buidto.intern()) {
				StringBuilder sb = new StringBuilder();
				sb.append(
						"select a.buidfr,b.pname,a.pcell,a.playout,a.sflag from insworkfl a inner join insbu b on b.buid=a.buidfr where buidto='");
				sb.append(buidto).append("'");
				_log.info("getWorkFlow:", sb.toString());
				HVector flows = eq.queryVec(sb.toString());
				if (flows != null) {
					wFlows = new ArrayList<CWorkFlow>();
					for (int i = 0; i < flows.size(); i++) {
						Object[] o0 = (Object[]) flows.elementAt(i);
						String buidfr = CCliTool.objToString(o0[0]);
						String name = CCliTool.objToString(o0[1]);
						String pcell = CCliTool.objToString(o0[2]);
						String playout = CCliTool.objToString(o0[3]);
						String sflag = CCliTool.objToString(o0[4]);
						int key = pcell.indexOf(";");
						String contC = pcell.substring(0,key);
						CWorkFlow cWorkFlow = new CWorkFlow(buidfr, name, buidto);
						Object o1 = SSTool.readCCells(eq, pcell, false);
						pcell = pcell.substring(key+1);
						CellsSessionUtil.cacheCells(eq.db_id, pcell, o1);
						if(o1 instanceof Cells[]) {
							Cells[] cells = (Cells[]) o1;
							LayCells[] layCells = new LayCells[cells.length];
							for(int m=0;m<cells.length;m++) {
								LayCells layCell = new LayCells(cells[m]);
								layCells[m] = layCell;
							}
							cWorkFlow.setCells(layCells);
						}
						cWorkFlow.setPcell(pcell);
						cWorkFlow.setContCell(contC);
						cWorkFlow.setFlag(sflag);
						cWorkFlow.setPlayout(playout);
						sb.setLength(0);
						sb.append("select buidfr,stable,stablefr,smap,sload,sfrom from inscopy where buid ='").append(buidto).append("' and buidfr='").append(buidfr).append("'");
						HVector hh = eq.queryVec(sb.toString());
						if(hh!=null) {
							CWorkCopy[] copys = new CWorkCopy[hh.size()];
							for(int k = 0; k < hh.size(); k++) {
								Object[] oo = (Object[]) hh.elementAt(k);
								CWorkCopy cp = new CWorkCopy(buidto,CCliTool.objToString(oo[1]),CCliTool.objToString(oo[0]),CCliTool.objToString(oo[2]),CCliTool.objToString(oo[3]),CCliTool.objToString(oo[4]),CCliTool.objToString(oo[5]));
								if(o1 instanceof Cells[]) {
									Cells[] cells = (Cells[]) o1;
									for(int m=1;m<cells.length;m++) {
										Cells c1 = cells[m];
										c1 = c1.find(cp.getStablefr(), true);
										if(c1!=null) {
											cp.setObjId(c1.obj_id);
											cp.makeConFigFld(c1);
										}
									}
								}
								
								copys[k] = cp;
							}
							cWorkFlow.setScopys(copys);
						}
//						RedisHelper.setNoTime(cWorkFlow.getBuidfr()+"_"+cWorkFlow.getBuidto(),JSON.toJSONString(cWorkFlow));
						wFlows.add(cWorkFlow);
					}
					RedisHelper.set(eq.db_id+"."+WFSTR+"."+buidto,JSON.toJSONString(wFlows),600);
				}
			}
		}else {
			wFlows = JSON.parseArray(infos, CWorkFlow.class);
		}	
		return wFlows;
	}
}
