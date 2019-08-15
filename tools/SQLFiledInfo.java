/**
 * 
 */
package inetbas.web.outsys.tools;

import java.io.Serializable;

/**
 * SQL查询中的字段对象
 * @author www.bip-soft.com
 * 2019-05-07 14:00:28
 */
public class SQLFiledInfo implements Serializable{
	private String filed;//原始字段
	private String filedNew;//分配字段
	private String filedIn;//分配字段
	private boolean bsum = false;//是否是sum字段
	private int index = 0;
	public SQLFiledInfo(String _filed,int _index) {
		this.filed = _filed;
		index = _index;
		initFiled();
	}
	
	private void initFiled() {
		String s10 = "";
		if(filed.startsWith("'")){
			s10 = filed; 
			bsum = true;
		}else{
			int kh = filed.lastIndexOf(")");
			if(kh == -1){
				String[] s1 = filed.split(" as ");
				if(s1.length ==1){
					s1 = filed.split(" ");
				}
				s10 = s1[0];
			}else{
				if(filed.startsWith("count(")){
					String[] s1 = filed.split(" as ");
					if(s1.length ==1){
						s1 = filed.split(" ");
					}
					s10 = s1[0];
					bsum = true;
				}else{
					s10 = filed.substring(0,kh+1);
				}
			} 
			
			if(s10.startsWith("sum(")) {
				bsum = true;
			}
		}
		filedIn = s10;
		filedNew = "f"+index;
	}
	
	public String getFiled() {
		return filed;
	}
	public void setFiled(String filed) {
		this.filed = filed;
	}
	public String getFiledNew() {
		return filedNew;
	}
	public void setFiledNew(String filedNew) {
		this.filedNew = filedNew;
	}
	public boolean isBsum() {
		return bsum;
	}
	public void setBsum(boolean bsum) {
		this.bsum = bsum;
	}

	public String getFiledIn() {
		return filedIn;
	}

	public void setFiledIn(String filedIn) {
		this.filedIn = filedIn;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	
}
