/**
 * 
 */
package inetbas.web.outsys.api.uidata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import inetbas.web.outsys.entity.PageInfo;

/**
 * web端数据集合
 * @author www.bip-soft.com
 * 2019-07-19 15:08:28
 */
public class UICData implements Serializable {
	private String obj_id;//对象ID
	private List<UIRecord> _data, _rmdata;//数据对象集合，移除对象的集合
	private int index = -1, attr;
	private boolean _bnull = true;
	
	private PageInfo page = new PageInfo();
	public UICData() {
		_data = new ArrayList<UIRecord>();
		_rmdata = new ArrayList<UIRecord>();
	}
	
	public UICData(String obj_id) {
		_data = new ArrayList<UIRecord>();
		_rmdata = new ArrayList<UIRecord>();
		this.obj_id = obj_id;
	}
	
	/**
	 * 增加一条记当。 
	 */
	public int add(UIRecord crd, int idx) {
	 int imx = _data.size();
	 if (idx < 0 || idx >= imx) {
	  _data.add(crd);
	  index = imx;
	 } else {
	  _data.add(idx, crd);
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

	public List<UIRecord> get_data() {
		return _data;
	}

	public void set_data(List<UIRecord> _data) {
		this._data = _data;
	}

	public List<UIRecord> get_rmdata() {
		return _rmdata;
	}

	public void set_rmdata(List<UIRecord> _rmdata) {
		this._rmdata = _rmdata;
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
	
	
	
}
