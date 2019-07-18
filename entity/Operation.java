/**
 * 
 */
package inetbas.web.outsys.entity;

import java.io.Serializable;

/**
 * @author www.bip-soft.com
 *
 */
public class Operation implements Serializable {
	
	private String buid; // 业务标识、业务号
	private String pname;// 业务名称
	private boolean bmain=true;//是否主表
	private boolean bnew=true; //是否新建
	private String pmenuid;//菜单号
	private String maintb;// 主表
	private String pkfld;//主键字段
	private String buidfld;//业务号字段
	private String reffld ;//引用
	private String bulnk; //关联业务
	private String lkbuidfld;//来源类型
	private String lknofld;//来源单号
	private String statefld;//状态字段
	private String iymfld; //期间字段
	private String hpdatefld ;//日期字段
	private String sorgfld;//部门字段
	private String smakefld;//制单人字段
	private String qid; //期末号
	private String purl;//URL参数
	private String docfld;//档案号字段
	private String docfmt;//档案号格式
	private boolean us_mkvou = false;//提交生成凭证
	private boolean us_delvou = false;//退回删除凭证
	private boolean us_gmts = false;//超量提示
	private boolean us_gmerr = false;//超量报错
	private String sublnk;//字表关联
	private String procs;//流程接口
	public String getBuid() {
		return buid;
	}
	public void setBuid(String buid) {
		this.buid = buid;
	}
	public String getPname() {
		return pname;
	}
	public void setPname(String pname) {
		this.pname = pname;
	}
	public boolean getBmain() {
		return bmain;
	}
	public void setBmain(boolean bmain) {
		this.bmain = bmain;
	}
	public boolean getBnew() {
		return bnew;
	}
	public void setBnew(boolean bnew) {
		this.bnew = bnew ;
	}
	public String getPmenuid() {
		return pmenuid;
	}
	public void setPmenuid(String pmenuid) {
		this.pmenuid = pmenuid==null?"":pmenuid;
	}
	public String getMaintb() {
		return maintb;
	}
	public void setMaintb(String maintb) {
		this.maintb = maintb==null?"":maintb;
	}
	public String getPkfld() {
		return pkfld;
	}
	public void setPkfld(String pkfld) {
		this.pkfld = pkfld==null?"sid":pkfld;
	}
	public String getBuidfld() {
		return buidfld;
	}
	public void setBuidfld(String buidfld) {
		this.buidfld = buidfld==null?"sbuid":buidfld;
	}
	public String getReffld() {
		return reffld;
	}
	public void setReffld(String reffld) {
		this.reffld = reffld==null?"creftimes":reffld;
	}
	public String getBulnk() {
		return bulnk;
	}
	public void setBulnk(String bulnk) {
		this.bulnk = bulnk  == null?"": bulnk;
	}
	public String getLkbuidfld() {
		return lkbuidfld;
	}
	public void setLkbuidfld(String lkbuidfld) {
		this.lkbuidfld = lkbuidfld  == null?"slkbuid": lkbuidfld;
	}
	public String getLknofld() {
		return lknofld;
	}
	public void setLknofld(String lknofld) {
		this.lknofld = lknofld  == null?"slkid": lknofld;
	}
	public String getStatefld() {
		return statefld;
	}
	public void setStatefld(String statefld) {
		this.statefld = statefld  == null?"state": statefld;
	}
	public String getIymfld() {
		return iymfld;
	}
	public void setIymfld(String iymfld) {
		this.iymfld = iymfld  == null? "": iymfld;
	}
	public String getHpdatefld() {
		return hpdatefld;
	}
	public void setHpdatefld(String hpdatefld) {
		this.hpdatefld = hpdatefld == null?"hpdate":hpdatefld;
	}
	public String getSorgfld() {
		return sorgfld;
	}
	public void setSorgfld(String sorgfld) {
		this.sorgfld = sorgfld == null?"sorg":sorgfld;
	}
	public String getSmakefld() {
		return smakefld;
	}
	public void setSmakefld(String smakefld) {
		this.smakefld = smakefld==null?"smake":smakefld;
	}
	public String getQid() {
		return qid;
	}
	public void setQid(String qid) {
		this.qid = qid == null ? "" :qid;
	}
	public String getPurl() {
		return purl;
	}
	public void setPurl(String purl) {
		this.purl = purl==null?"":purl;
	}
	public String getDocfld() {
		return docfld;
	}
	public void setDocfld(String docfld) {
		this.docfld = docfld == null?"":docfld;
	}
	public String getDocfmt() {
		return docfmt;
	}
	public void setDocfmt(String docfmt) {
		this.docfmt = docfmt==null?"":docfmt;
	}
	public boolean getUs_mkvou() {
		return us_mkvou;
	}
	public void setUs_mkvou(boolean us_mkvou) {
		this.us_mkvou = us_mkvou;
	}
	public boolean getUs_delvou() {
		return us_delvou;
	}
	public void setUs_delvou(boolean us_delvou) {
		this.us_delvou = us_delvou;
	}
	public boolean getUs_gmts() {
		return us_gmts;
	}
	public void setUs_gmts(boolean us_gmts) {
		this.us_gmts = us_gmts;
	}
	public boolean getUs_gmerr() {
		return us_gmerr;
	}
	public void setUs_gmerr(boolean us_gmerr) {
		this.us_gmerr = us_gmerr;
	}
	public String getSublnk() {
		return sublnk;
	}
	public void setSublnk(String sublnk) {
		this.sublnk = sublnk==null?"":sublnk;
	}
	public String getProcs() {
		return procs;
	}
	public void setProcs(String procs) {
		this.procs = procs==null?"":procs;
	}
	
	
	

}
