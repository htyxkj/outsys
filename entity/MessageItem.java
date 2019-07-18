/**
 * 
 */
package inetbas.web.outsys.entity;

import java.io.Serializable;

/**
 * @author www.bip-soft.com
 *
 */
public class MessageItem implements Serializable{
	//select a.iid,a.smake,a.dmake,a.dkeep,a.stitle,a.sdsc,sfile,fj_root,brd from insmsg a 
	//inner join insmsga b on b.iid=a.iid where b.touser='admin' and b.brd in (0,1)
	private int iid;//主键项次
	private UserInfo smake;//发送人
	private String dmake;//时间
	private String title;//标题
	private String content;//内容
	private int brd;//状态0:公共;1:个人;2:已读
	public int getIid() {
		return iid;
	}
	public void setIid(int iid) {
		this.iid = iid;
	}
	public UserInfo getSmake() {
		return smake;
	}
	public void setSmake(UserInfo smake) {
		this.smake = smake;
	}
	public String getDmake() {
		return dmake;
	}
	public void setDmake(String dmake) {
		this.dmake = dmake;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getBrd() {
		return brd;
	}
	public void setBrd(int brd) {
		this.brd = brd;
	}
	
	
	

}
