/**
 * 
 */
package inetbas.web.outsys.entity;

import java.io.Serializable;

/**
 * @author www.bip-soft.com
 * 2019-03-21 14:19:24
 */
public class PageInfo implements Serializable{
	private int total;//总条数
	private int currPage = 1;//当前页
	private int index = 0;//当前页第几条
	private int pageSize = 20;//每页条数
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public int getCurrPage() {
		return currPage;
	}
	public void setCurrPage(int currPage) {
		this.currPage = currPage;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	
	
}
