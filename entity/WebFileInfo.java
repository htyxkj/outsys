/**
 * 
 */
package inetbas.web.outsys.entity;

import java.io.Serializable;

/**
 * @author www.bip-soft.com
 *
 */
public class WebFileInfo implements Serializable{
	private String name="";
	private String url="";
	private long size = 0;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}

}
