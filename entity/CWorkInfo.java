/**
 * 
 */
package inetbas.web.outsys.entity;

import java.util.ArrayList;

/**
 * @author www.bip-soft.com
 *
 */
public class CWorkInfo {
	private String state="";//当前节点
	private String upState="0";
	private UserInfo upUser;
	private boolean checked = false;//是否已经审核
	private ArrayList<UserInfo> chkInfos;//待审核人列表
	ArrayList<ApprovalFlowObj> list;//下一节点信息
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getUpState() {
		return upState;
	}
	public void setUpState(String upState) {
		this.upState = upState;
	}
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	public ArrayList<UserInfo> getChkInfos() {
		return chkInfos;
	}
	public void setChkInfos(ArrayList<UserInfo> chkInfos) {
		this.chkInfos = chkInfos;
	}
	public ArrayList<ApprovalFlowObj> getList() {
		return list;
	}
	public void setList(ArrayList<ApprovalFlowObj> list) {
		this.list = list;
	}
	public UserInfo getUpUser() {
		return upUser;
	}
	public void setUpUser(UserInfo upUser) {
		this.upUser = upUser;
	}
	
	

}
