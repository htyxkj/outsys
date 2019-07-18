/**
 * 
 */
package inetbas.web.outsys.entity;

import java.io.Serializable;

/**
 * 状态对象
 * @author www.bip-soft.com
 *
 */
public class StatusInfo implements Serializable{
	private String id;
	private String name;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
