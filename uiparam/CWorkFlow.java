/**
 * 
 */
package inetbas.web.outsys.uiparam;

import java.io.Serializable;

/**
 * 工作流实体类
 * @author www.bip-soft.com
 * 2018-10-26 15:10:02
 */
public class CWorkFlow implements Serializable{

	private String buidfr = null;//来源业务号
	private String buidto = null;//目标业务号
	private String buidfrName = null;//来源业务名称
	private String pcell = null;//拷贝定义对象组成
	private String contCell = null;//拷贝定义对象组成
	private String playout = null;//拷贝定义布局
	private String flag = null;//回写标志
	private LayCells[] cells;//对象组成
	private CWorkCopy[] scopys;//数据对照
	
	public CWorkFlow() {}
	
	/***
	 * 工作流构造器
	 * @param buidfr 来源业务号
	 * @param name 来源名称
	 * @param buidto 目标业务号
	 */
	public CWorkFlow(String buidfr,String name,String buidto) {
		this.buidfr = buidfr;
		this.buidfrName = name;
		this.buidto = buidto;
	}
	public String getBuidfr() {
		return buidfr;
	}
	public void setBuidfr(String buidfr) {
		this.buidfr = buidfr;
	}
	public String getBuidto() {
		return buidto;
	}
	public void setBuidto(String buidto) {
		this.buidto = buidto;
	}
	public String getBuidfrName() {
		return buidfrName;
	}
	public void setBuidfrName(String buidfrName) {
		this.buidfrName = buidfrName;
	}
	public String getPcell() {
		return pcell;
	}
	public void setPcell(String pcell) {
		this.pcell = pcell;
	}
	public String getPlayout() {
		return playout;
	}
	public void setPlayout(String playout) {
		this.playout = playout;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}

	public LayCells[] getCells() {
		return cells;
	}

	public void setCells(LayCells[] cells) {
		this.cells = cells;
	}
	
	public CWorkCopy[] getScopys() {
		return scopys;
	}

	public void setScopys(CWorkCopy[] scopys) {
		this.scopys = scopys;
	}

	public String getContCell() {
		return contCell;
	}

	public void setContCell(String contCell) {
		this.contCell = contCell;
	}
}
