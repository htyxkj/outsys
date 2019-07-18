/**
 * 
 */
package inetbas.web.outsys.entity;

import java.util.List;

/**
 * 审批流节点信息
 * @author www.bip-soft.com
 *
 */
public class ApprovalFlowObj {
	private String stateId;
	private String stateName;
	
	private List<UserInfo> users;
	
	public String getStateId() {
		return stateId;
	}

	public void setStateId(String stateId) {
		this.stateId = stateId;
	}

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public List<UserInfo> getUsers() {
		return users;
	}

	public void setUsers(List<UserInfo> users) {
		this.users = users;
	}
	
	

}
