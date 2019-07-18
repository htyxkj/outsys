/**
 * 
 */
package inetbas.web.outsys.tools;

import inetbas.cli.cutil.CCliTool;
import inetbas.pub.coob.Cell;
import inetbas.pub.coob.Cells;

/**
 * @author www.bip-soft.com
 *
 */
public class CellsUtil {
	
	public static void initCells(Cells cell) {
		initlink(cell);
		int child = cell.getChildCount();
		for (int i = 0; i < child; i++) {
			initlink(cell.getChild(i));
		}
	}
	
	public static void initCells(Cells[] cells) {
		if (cells != null && cells.length > 0) {
			for (int i = 0; i < cells.length; i++) {
				Cells cells2 = cells[i];
				initlink(cells2);
			}
		}
	}
	
	public static void initlink(Cells cell) {
		 Cells cellp = cell.c_par;
		 if (cellp == null || cell.fimp != null)
		  return;
		 String smp = cell.fkmaps;
		 int xx = smp != null ? smp.length() : 0;
		 if (xx > 0 && smp.charAt(0) == '(') {
		  initlinkudf(cellp, smp.substring(1, xx - 1),cell);//;-单独指定。
		  return;
		 }
		 int cc = 0, imp = 0, i;
		 String s0 = "", st = "", s1;
		 if (cellp.fkcc > 0) {
		  s0 = cellp.fimp;
		  xx = s0.indexOf('|');
		  st = s0.substring(xx + 1);
		  s0 = s0.substring(0, xx);
		  xx = st.indexOf('*');//;-去掉索引号
		  if (xx > 0)
		   st = st.substring(0, xx);
		 }
		 int t0 = cellp.pkcc, xs[] = cellp.pkIndexs();
		 Cell cel, cels[] = cellp.db_cels;
		 for (i = 0;i < t0;i++) {
		  cel = cels[xs[i]];
		  s0 += "," + cel.ccName;
		  st += CCliTool.sqlTypeToChar(cel.ccType);
		 }
		 if (s0.length() < 1)
		  throw new RuntimeException(cl.IERR.ERR_CELL + "<NO PRIMARY KEY>" + cellp.obj_id);
		 else if (s0.charAt(0) == ',')
		  s0 = s0.substring(1);//;-没有外键.
		 String[] sfs = new String[cl.ICL.MAXPKLEN];
		 while (true) {
		  xx = s0.indexOf(',');
		  sfs[cc++] = xx < 0 ? s0 : s0.substring(0, xx);
		  if (xx < 0)
		   break;
		  s0 = s0.substring(xx + 1);
		 }
		 if (smp != null && smp.length() > 0) {
		  smp = smp.replace(':', '=');//;-主键(当前表)[=|:]外键(父表)
		  while (true) {
		   xx = smp.indexOf(',');//;-多项用","分隔
		   s0 = xx < 0 ? smp : smp.substring(0, xx);
		   t0 = s0.indexOf('=');
		   if (t0 > 0) {
		    s1 = s0.substring(t0 + 1);
		    s0 = s0.substring(0, t0);
		    for (t0 = 1, i = 0;i < cc;i++, t0 <<= 1) {
		     if ((imp & t0) == 0 && Cell.cpname(sfs[i], s1) >= 0) {
		      imp |= t0;
		      sfs[i] = s0;
		      break;
		     }
		    }
		   }
		   if (xx < 0)
		    break;
		   smp = smp.substring(xx + 1);
		  }
		 }
		 smp = "";//;-字段
		 s0 = cell.tableName() + ".";
		 for (t0 = 1, i = 0;i < cc;i++, t0 <<= 1) {
		  s1 = sfs[i];
		  xx = s1.indexOf('.');
		  if (xx < 0)
		   s1 = s0 + s1;//;-没有表名,直接用当前的表名
		  else if ((imp & t0) == 0)
		   s1 = s0 + s1.substring(xx + 1);//;-非对照方式换成当前的表名
		  smp += "," + s1;
		 }
		 cell.fimp = smp.substring(1) + "|" + st;
		 cell.fkcc = st.length();
		}
	
	
	private static void initlinkudf(Cells cellp, String smp,Cells cell) {
		 //;-导入的索引号采用2位表示法.
		 //;-顺序必需和单无顺序一至,否则系统取数不对。
		 int xx, x0, cx, cx0 = -1;
		 Cell cel, cels[] = cellp.db_cels;
		 int cpk = cellp.pkcc, pxs[] = cellp.pkIndexs();
		 String s0, sx = String.valueOf(CCliTool.intToChar(cellp.fkcc)), st = "", smpn = "", smp0 = smp, sexp = cellp.exp_sx;
		 smp = smp.replace(':', '=');//;-主键(当前表)[=|:]外键(父表)
		 while (true) {
		  xx = smp.indexOf(',');//;-多项用","分隔
		  s0 = xx < 0 ? smp : smp.substring(0, xx);
		  x0 = s0.indexOf('=');
		  cx = cellp.nameToIndex(s0.substring(x0 + 1), false, true);
		  if (cx <= cx0)
		   throw new RuntimeException(cl.IERR.ERR_CELL + "<ORDER>" + smp0);//;-顺序不一至
		  cel = cels[cx];
		  smpn += "," + s0.substring(0, x0);
		  if ((cel.attr & Cell.PRIMARY) != 0)
		   x0 = CCliTool.indexOf(pxs, cx);//;-主键相对索引号
		  else {
		   s0 = String.valueOf(cx);
		   if (sexp == null || sexp.length() < 1) {
		    sexp = s0;
		    x0 = cpk;
		   } else {
		    x0 = CCliTool.toIndex(sexp, s0, ',', true);
		    if (x0 < 0) {
		     x0 = CCliTool.calccount(sexp, ',', false);
		     sexp += "," + s0;
		    }
		    x0 += cpk;
		   }
		  }
		  sx += String.valueOf(x0);//;-相对索引号位置(取导出的第几项值)。
		  st += CCliTool.sqlTypeToChar(cel.ccType);//;-类型
		  cel.attr |= Cell.LIST;//;-强行加上列表属性。
		  if (xx < 0)
		   break;
		  cx0 = cx;
		  smp = smp.substring(xx + 1);
		 }
		 cellp.exp_sx = sexp;//;-绝对索引号(父表取引用值)。
		 cell.fimp = smpn.substring(1) + "|" + st + "*" + sx;
		 cell.attrci |= cl.ICL.ociUDFLNK;//;-自定义关联。
		 if ((cell.attr & cl.ICL.ocCtrl) == 0)
		  cell.attr |= cl.ICL.ocReadonly;//;-非主控表时只能只读。
		 cell.fkcc = st.length();
		}
	
	/**
	 * @param countCell // 统计cells
	 * @param pgrpfld //分组统计字段下标
	 * @param pgrpdatafld // 分组统计数据字段下标
	 * @param oldCells //原始Cell
	 */
	public static Object[] makeCellsCell(Cells countCell, String pgrpfld,
			String pgrpdatafld,Cells oldCells) {
		String[] flds = pgrpfld.split(",");
		String[] flddatas = pgrpdatafld.split(",");
		Cell[] cels = new Cell[flds.length+flddatas.length];
		int num=0;
		String fild1 = "",fild2="";
		for(int i=0;i<flds.length;i++) {
			String celId = flds[i];
			for(int j=0;j<countCell.db_cels.length;j++){
				Cell cell1 = countCell.db_cels[j];
				String id1 = "\""+cell1.ccName+"\"";
				if(id1.equals(celId) || cell1.ccName.equals(celId)){
					System.out.println(cell1.ccName);
					cels[num] = cell1;
					break;
				}
			}
			fild1+="f"+(num+1)+",";
			num++;
		}
		for(int i=0;i<flddatas.length;i++) {
			String celId = flddatas[i];
			for(int j=0;j<countCell.db_cels.length;j++){
				Cell cell1 = countCell.db_cels[j];
				String id1 = "\""+cell1.ccName+"\"";
				if(id1.equals(celId)||cell1.ccName.equals(celId)){
					cels[num] = cell1;
					break;
				}
			}
			fild2+="sum(isnull(f"+(num+1)+",0)) as f"+(num+1)+",";
			num++;
		}
		countCell.setCCells(cels);
		return new Object[]{countCell,fild1.substring(0,fild1.length()-1),fild2.substring(0,fild2.length()-1)};
	}
	
	public static int getCellIndexByName(String celName,Cells countC){
		int _index = -1;
		for(int i=0;i<countC.all_cels.length;i++){
			Cell cc = countC.all_cels[i];
			if(cc.ccName.equals(celName)){
				_index = i;
				return _index;
			}
		}
		return _index;
	}

}
