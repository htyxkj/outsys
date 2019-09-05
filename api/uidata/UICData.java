/**
 * 
 */
package inetbas.web.outsys.api.uidata;

import inetbas.web.outsys.entity.PageInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

/**
 * web端数据集合
 * @author www.bip-soft.com
 * 2019-07-19 15:08:28
 */
public class UICData implements Serializable {
	private String obj_id;//对象ID
	private List<UIRecord> data, rmdata;//数据对象集合，移除对象的集合
	private int index = -1, attr;
	private boolean _bnull = true;
	private List<JSONObject> sumData;
	
	private PageInfo page = new PageInfo();
	public UICData() {
		data = new ArrayList<UIRecord>();
		rmdata = new ArrayList<UIRecord>();
	}
	
	public UICData(String obj_id) {
		data = new ArrayList<UIRecord>();
		rmdata = new ArrayList<UIRecord>();
		this.obj_id = obj_id;
	}
	
	/**
	 * 增加一条记当。 
	 */
	public int add(UIRecord crd, int idx) {
	 int imx = data.size();
	 if (idx < 0 || idx >= imx) {
	  data.add(crd);
	  index = imx;
	 } else {
	  data.add(idx, crd);
	  index = idx;
	 }
	 _bnull = false;
	 return index;
	}
	public String getObj_id() {
		return obj_id;
	}
	public void setObj_id(String obj_id) {
		this.obj_id = obj_id;
	}


	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getAttr() {
		return attr;
	}

	public void setAttr(int attr) {
		this.attr = attr;
	}

	public boolean is_bnull() {
		return _bnull;
	}

	public void set_bnull(boolean _bnull) {
		this._bnull = _bnull;
	}

	public PageInfo getPage() {
		return page;
	}

	public void setPage(PageInfo page) {
		this.page = page;
	}

	public List<UIRecord> getData() {
		return data;
	}

	public void setData(List<UIRecord> data) {
		this.data = data;
	}

	public List<UIRecord> getRmdata() {
		return rmdata;
	}

	public void setRmdata(List<UIRecord> rmdata) {
		this.rmdata = rmdata;
	}

	public List<JSONObject> getSumData() {
		return sumData;
	}

	public void setSumData(List<JSONObject> sumData) {
		this.sumData = sumData;
	}
	
	
	
}
