/**
 * 
 */
package inetbas.web.outsys.entity;

import java.io.Serializable;

/**
 * @author www.bip-soft.com
 *
 */
public class DempInfo implements Serializable {
	private String cmcCode;
	private String cmcName;
	private String deptCode;
	private String deptName;
	public String getCmcCode() {
		return cmcCode;
	}
	public void setCmcCode(String cmcCode) {
		this.cmcCode = cmcCode;
	}
	public String getCmcName() {
		return cmcName;
	}
	public void setCmcName(String cmcName) {
		this.cmcName = cmcName;
	}
	public String getDeptCode() {
		return deptCode;
	}
	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}
	public String getDeptName() {
		return deptName;
	}
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
	
	

}
