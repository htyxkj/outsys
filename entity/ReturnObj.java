/**
 * 
 */
package inetbas.web.outsys.entity;

import java.io.Serializable;
import java.util.Map;

/**
 * @author www.bip-soft.com
 *
 */
public class ReturnObj implements Serializable{
	private int id=-1; //0,成功，-1：失败
	private String message="失败";
	private  Map<String, Object> data;
	
	public ReturnObj(){}
	public ReturnObj(int successOrError,String info)
	{
		this.id = successOrError;
		this.message = info;
	}
	public Map<String, Object> getData() {
		return data;
	}
	public void setData(Map<String, Object> data) {
		this.data = data;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public void makeSuccess(){	
		makeSuccess("操作成功！");
	}
	public void makeSuccess(String info){
		this.id=0;
		this.message = info;
	}
	
	public void makeFaile() {
		makeFaile("操作失败！");
	}
	
	public void makeFaile(String error) {
		this.id=-1;
		this.message = error;
	}
	
	
}
