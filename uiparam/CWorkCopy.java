/**
 * 
 */
package inetbas.web.outsys.uiparam;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import inetbas.pub.coob.Cell;
import inetbas.pub.coob.Cells;

/**
 * 拷贝定义参数
 * @author www.bip-soft.com
 * 2018-10-30 09:31:54
 */
public class CWorkCopy implements Serializable{
	private String objId;//对应的对象号
	private String buid;//当前业务号
	private String stable;//当前数据库表名
	private String buidfr;//来源业务号
	private String stablefr;//来源业务表名
	private String smap;//来源-->目标字段对照
	private String sload;//目标数据加载过滤
	private String sfrom;//数据来源
	
	private List<String> fromFldList;
	private List<String> toFldList;
	
	
	public CWorkCopy() {}
	
	public CWorkCopy(String buid,String stable,String buidfr,String stablefr,String smap,String sload,String sfrom) {
		this.buid = buid;
		this.stable = stable;
		this.buidfr = buidfr;
		this.stablefr = stablefr;
		this.smap = smap;
		this.sload = sload;
		this.sfrom = sfrom;
	}
	
	
	/**
	 * 
	 * 2019-06-28 10:09:44
	 */

	public String getBuid() {
		return buid;
	}
	public void setBuid(String buid) {
		this.buid = buid;
	}
	public String getStable() {
		return stable;
	}
	public void setStable(String stable) {
		this.stable = stable;
	}
	public String getBuidfr() {
		return buidfr;
	}
	public void setBuidfr(String buidfr) {
		this.buidfr = buidfr;
	}
	public String getStablefr() {
		return stablefr;
	}
	public void setStablefr(String stablefr) {
		this.stablefr = stablefr;
	}
	public String getSmap() {
		return smap;
	}
	public void setSmap(String smap) {
		this.smap = smap;
	}
	public String getSload() {
		return sload;
	}
	public void setSload(String sload) {
		this.sload = sload;
	}
	public String getSfrom() {
		return sfrom;
	}
	public void setSfrom(String sfrom) {
		this.sfrom = sfrom;
	}
	
	@Override
	public String toString() {
		return "CWorkCopy [buid=" + buid + ", stable=" + stable + ", buidfr=" + buidfr + ", stablefr=" + stablefr
				+ ", smap=" + smap + ", sload=" + sload + ", sfrom=" + sfrom + "]";
	}

	public List<String> getFromFldList() {
		return fromFldList;
	}

	public void setFromFldList(List<String> fromFldList) {
		this.fromFldList = fromFldList;
	}

	public List<String> getToFldList() {
		return toFldList;
	}

	public void setToFldList(List<String> toFldList) {
		this.toFldList = toFldList;
	}

	public String getObjId() {
		return objId;
	}

	public void setObjId(String objId) {
		this.objId = objId;
	}

	/**
	 * @param c1
	 * 2019-06-28 10:28:33
	 */
	public void makeConFigFld(Cells cells) {
		if(smap!=null&&smap.length()>0) {
			String[] flds = smap.split(",");
			fromFldList = new ArrayList<String>();
			toFldList = new ArrayList<String>();
			for(int i=0;i<cells.all_cels.length;i++) {
				String id = cells.all_cels[i].ccName;
				boolean bexit = false;
				fromFldList.add(id);
				for(int m=0;m<flds.length;m++) {
					String[] s1 = flds[m].split("/");
					if(id.equals(s1[0])) {
						toFldList.add(s1[1]);
						bexit = true;
					}
				}
				if(!bexit)
					toFldList.add(id);
			}
		}else {
			fromFldList = new ArrayList<String>();
			toFldList = new ArrayList<String>();
			for(int i=0;i<cells.all_cels.length;i++) {
				String id = cells.all_cels[i].ccName;
				fromFldList.add(id);
				toFldList.add(id);
			}
		}
	}
}
