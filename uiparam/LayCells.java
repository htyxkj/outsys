/**
 * 
 */
package inetbas.web.outsys.uiparam;

import inetbas.cli.cutil.CCliTool;
import inetbas.pub.coob.Cell;
import inetbas.pub.coob.Cells;

import java.io.Serializable;

import cl.ICL;


/**
 * @author www.bip-soft.com
 *
 */
public class LayCells implements Serializable {
	public String obj_id;//对象Id 
	public String desc;//对象描素
	private long attr;//属性
	public LayCell[] cels;//元素集合
	public LayCells[] subLayCells;//子项
	public String parentId="";//父对象Id
	public boolean editble = true;//对象可否编辑
	public boolean readOnly = false; //对象只读
	public boolean unNull = false;//对象可空,保存时如果该项是true，没有数据时跳过
	public boolean canAppend = true;
	public int autoInc,x_co;
	public boolean haveChild = false;
	
	public int index;
	public int pkid,pkcc;//外键数量，主键数量
	public int[] pkindex;//主键下标
	public int widthCell;
	public String condiction;
	public int x_pk;
	
	public String clientUI;
	public LayCells () {}
	public LayCells (Cells cells) {
		obj_id = cells.obj_id;
		desc = cells.desc;
		attr = cells.attr;
		parentId = (cells.c_par == null ? "" : cells.c_par.obj_id);
		pkid = cells.fkcc;
		autoInc = cells.autoInc;
		x_co = cells.x_co;
		pkcc = cells.pkcc;
		pkindex = cells.pkIndexs();
		widthCell = cells.widthCell &0x3FF;
		condiction = cells.condiction;
		initAttr();
		int celLength = cells.all_cels.length;
		cels = new LayCell[celLength];
		for(int i = 0;i < celLength;i++){
			LayCell layCell = new LayCell(cells.all_cels[i]);
			cels[i] = layCell;
		}
		initRefs();
		int childLayCelsCount = cells.getChildCount();
		if(childLayCelsCount>0)
			haveChild = true;
		for(int i = 0;i < childLayCelsCount; i++){
			LayCells cells2 = new LayCells(cells.getChild(i));
			cells2.initRefs();
			addChild(cells2);
		}
		x_pk = CCliTool.indexPKID(cells,true,true);
		this.clientUI = cells.clientUI;
	}
	
	
	/**
	 * 
	 */
	private void initAttr() {
		if((attr&ICL.ocReadonly)>0){
			readOnly = true;
			editble = false;
			canAppend = false;
		}
		if((attr&ICL.ocCanNull)>0) {
			unNull = true;
		}
		if((attr&ICL.ocNotAppend)>0){
			canAppend = true;
		}
		
	}
	/**
	 * @return the attr
	 */
	public long getAttr() {
		return attr;
	}
	/**
	 * @param attr the attr to set
	 */
	public void setAttr(long attr) {
		this.attr = attr;
	}
	
	/***
	 * 添加子项Cells
	 * @param cell
	 */
	public void addChild(LayCells cell) {
		 int cc = subLayCells != null ? subLayCells.length : 0;
		 LayCells[] old = subLayCells;
		 subLayCells = new LayCells[cc + 1];
		 if (cc > 0)
		  System.arraycopy(old, 0, subLayCells, 0, cc);
		 subLayCells[cc] = cell;
		 cell.index = cc;
		 cell.parentId = this.obj_id;
	}
	
	public int getChildCount(){
		return subLayCells==null?0:subLayCells.length;
	}
	
	public LayCells find(String objid) {
		 return find(objid, false);
	}
	
	public LayCells find(String sName, boolean isTB) {
		 if (sName.equals(obj_id)||sName==null)
			 return this;
		 int t0 = getChildCount();
		 if (t0 > 0) {
			LayCells cell;
		  for (int i = 0;i < t0;i++) {
		   cell = subLayCells[i].find(sName, isTB);
		   if (cell != null)
		    return cell;
		  }
		 }
		 return null;
	}
	
	/**
	 * 初始化公式引用
	 * 2019-08-12 16:51:26
	 */
	public void initRefs() {
		for(int i=0;i<cels.length;i++) {
			LayCell cell = cels[i];
			String script = cell.script;
			if((cell.attr&Cell.USEBDS)>0||script!=null) {
				if(script!=null) {
					if(script.startsWith("=:")) {
						script = script.substring(2);
						//sql("AM",$(select sum(b.qty) from hta b inner join ht a 
						//on a.sid=b.sid where a.sbuid='2111' and {b.barcode=barcode} and {b.cxh=cxh} and {b.gdic=gdic}))
						if(script.startsWith("sql(")) {
							int _q = script.indexOf("{");
							while (_q>-1) {
								int _qn = CCliTool.nextBarcket(script.toCharArray(), _q, script.length(), '{');
								String s1 = script.substring(_q+1,_qn);
								String[] ss = s1.split("=");
								String s0 = ss[1];
								if(!cell.refCellIds.contains(s0))
									cell.refCellIds.add(s0);
								script = script.substring(_qn+1);
								_q = script.indexOf("{");
							}
						}
					}
					if(script.indexOf("[")>-1) {
						int _q = script.indexOf("[");
						while (_q>-1) {
							int _qn = CCliTool.nextBarcket(script.toCharArray(), _q, script.length(), '[');
							String s0 = script.substring(_q+1,_qn);
							if(s0.charAt(0)=='^'){
								s0 = s0.substring(1);
								if(!cell.pRefIds.contains(s0))
									cell.pRefIds.add(s0);
							}else {
								if(!cell.refCellIds.contains(s0))
									cell.refCellIds.add(s0);
							}

							script = script.substring(_qn+1);
							_q = script.indexOf("[");
						}
					}
					//自增
					if((cell.attr&0x80)>0&&cell.type==12) {
						int _q = script.indexOf(",");
						while (_q>0) {
							String s0 = script.substring(0, _q);
							if(!cell.refCellIds.contains(s0))
								cell.refCellIds.add(s0);
							script = script.substring(_q+1);
							_q = script.indexOf(",");
						}
						if(script.length()>0) {
							String s0 = script;
							if(!cell.refCellIds.contains(s0))
								cell.refCellIds.add(s0);
						}
					}
				}
			}
		}
	}
	
	public LayCell findById(String cellId) {
		LayCell c0 = null;
		for(int i=0;i<cels.length;i++) {
			LayCell cell = cels[i];
			if(cell.id.equals(cellId)) {
				c0 = cell;
				break;
			}
		}
		return c0;
	}
	

}
