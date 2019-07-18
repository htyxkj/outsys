/**
 * 
 */
package inetbas.web.outsys.uiparam;

import inetbas.cli.cutil.CCliTool;
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
		int childLayCelsCount = cells.getChildCount();
		if(childLayCelsCount>0)
			haveChild = true;
		for(int i = 0;i < childLayCelsCount; i++){
			LayCells cells2 = new LayCells(cells.getChild(i));
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
	

}
