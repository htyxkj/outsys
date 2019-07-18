/**
 * 
 */
package inetbas.web.outsys.entity;

import java.io.Serializable;
import java.util.ArrayList;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;

/**
 * 查询对象及返回数据
 * @author www.bip-soft.com
 * 2019-03-21 14:17:29
 */
public class QueryEntity implements Serializable {
	private String pcell;//主对象cellID
	private String tcell;//查询cellID
	private String cont;//查询条件
	private String orderBy;//排序
	private String groupV;//分组字段的值
	private PageInfo page = new PageInfo();//页码
	private int type=1;//类型，是单据还是报表
	public int oprid = 13;//13查询数据，14，根据主键查询,查询一条记录（包含主子）,
	private ArrayList<JSONObject> values;//返回数据（有可能只是列表数据）
	public String getPcell() {
		return pcell;
	}
	public void setPcell(String pcell) {
		this.pcell = pcell;
	}
	public String getCont() {
		return cont;
	}
	public void setCont(String cont) {
		this.cont = cont;
	}
	public String getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	public PageInfo getPage() {
		return page;
	}
	public void setPage(PageInfo page) {
		this.page = page;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public ArrayList<JSONObject> getValues() {
		return values;
	}
	public void setValues(ArrayList<JSONObject> values) {
		this.values = values;
	}
	public String getTcell() {
		return tcell;
	}
	public void setTcell(String tcell) {
		this.tcell = tcell;
	}
	public String getGroupV() {
		return groupV;
	}
	public void setGroupV(String groupV) {
		this.groupV = groupV;
	}
	
}
