/**
 * 
 */
package inetbas.web.outsys.entity;

import java.io.Serializable;

import cl.ICL;

/**
 * @author www.bip-soft.com
 *
 */
public class UserInfo implements Serializable{
	private String userCode;
	private String userName;
	private DempInfo deptInfo;
	private String gwCode;
	private int attr = ICL.USR_ONE;
	public String getUserCode() {
		return userCode;
	}
	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public DempInfo getDeptInfo() {
		return deptInfo;
	}
	public void setDeptInfo(DempInfo deptInfo) {
		this.deptInfo = deptInfo;
	}
	public String getGwCode() {
		return gwCode;
	}
	public void setGwCode(String gwCode) {
		this.gwCode = gwCode;
	}
	public int getAttr() {
		return attr;
	}
	public void setAttr(int attr) {
		this.attr = attr;
	}
	
	

}
