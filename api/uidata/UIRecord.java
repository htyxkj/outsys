/**
 * 
 */
package inetbas.web.outsys.api.uidata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.alibaba.fastjson.JSONObject;

/**
 * 单条数据记录
 * @author www.bip-soft.com
 * 2019-07-19 15:08:03
 */
public class UIRecord implements Serializable {
	private int c_state;//数据记录状态
	private JSONObject data = new JSONObject(); //实际数据
	private List<UICData> subs = new ArrayList<UICData>();//子项数据
	
	public String id = UUID.randomUUID().toString().toLowerCase();//主键ID
	public UIRecord(){
	}
	public UIRecord(int state){
		c_state = state;
	}
	public int getC_state() {
		return c_state;
	}
	public void setC_state(int c_state) {
		this.c_state = c_state;
	}
	public JSONObject getData() {
		return data;
	}
	public void setData(JSONObject data) {
		this.data = data;
	}
	public List<UICData> getSubs() {
		return subs;
	}
	public void setSubs(List<UICData> subs) {
		this.subs = subs;
	}

	
	
}
