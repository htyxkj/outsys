/**
 * 
 */
package inetbas.web.outsys.entity;

import inet.PTool;
import inetbas.cli.cutil.CCliTool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import cl.ICL;

/**
 * @author www.bip-soft.com
 *
 */
public class MenuParams implements Serializable {
	private String width ="50";//表格宽度
	private String plabel=""; //说明
	private String pbuid=""; //标识
	private String pcell=""; //组成
	private String playout=""; //布局+
	private String pinvoke=""; // INVOKE
	private String pproc=""; // PROC
	private String pdata=""; // 取数条件
	private String pflow=""; // 业务号
	private String pwfproc=""; // 流程接口 服务端调用
	private int pattr=0; // pattr 按钮属性，按位运算
//	private String pbds=""; // 其它+
	private Hashtable<String,String> pbds = new Hashtable<String, String>();//其他
	private boolean beBill=true; //单据类型，true是单据，false是报表
	
	private String pclass=""; //小程序
	
	//初始化统计取数
	private boolean bgroup=false;//是否是显示图表
	private ArrayList<String> groupfilds;//分组字段
	private ArrayList<String> sumfilds ;//合计字段
	private String ctype;//图表类型 
	
	public MenuParams(){}
	
	public void initParams(Hashtable<String,String> hts){
		if(hts.containsKey(ICL.plabel)){
			this.plabel = hts.get(ICL.plabel);
		}
		if(hts.containsKey(ICL.pbuid)){
			this.pbuid = hts.get(ICL.pbuid);
		}
		if(hts.containsKey(ICL.pcell)){
			this.pcell = hts.get(ICL.pcell);
		}
		if(hts.containsKey(ICL.playout)){
			this.playout = hts.get(ICL.playout);
		}
		if(hts.containsKey(ICL.pinvoke)){
			this.pinvoke = hts.get(ICL.pinvoke);
		}
		if(hts.containsKey(ICL.pproc)){
			this.pproc = hts.get(ICL.pproc);
		}
		if(hts.containsKey(ICL.pdata)){
			this.pdata = hts.get(ICL.pdata);
		}
		if(hts.containsKey(ICL.pflow)){
			this.pflow = hts.get(ICL.pflow);
		}
		if(hts.containsKey(ICL.pattr)){
			this.pattr = CCliTool.objToInt(hts.get(ICL.pattr),0);
		}
		if(hts.containsKey(ICL.pwfproc)){
			this.pwfproc = hts.get(ICL.pwfproc);
		}
		if(hts.containsKey(ICL.pbds)){
			PTool.divideURL(this.pbds, hts.get(ICL.pbds));
		}
		if(hts.containsKey(ICL.pclass)){
			this.pclass = hts.get(ICL.pclass);
		}
	}
	
	public String getPlabel() {
		return plabel;
	}
	public void setPlabel(String plabel) {
		this.plabel = plabel;
	}
	public String getPbuid() {
		return pbuid;
	}
	public void setPbuid(String pbuid) {
		this.pbuid = pbuid;
	}
	public String getPcell() {
		return pcell;
	}
	public void setPcell(String pcell) {
		this.pcell = pcell;
	}
	public String getPlayout() {
		return playout;
	}
	public void setPlayout(String playout) {
		this.playout = playout;
	}
	public String getPinvoke() {
		return pinvoke;
	}
	public void setPinvoke(String pinvoke) {
		this.pinvoke = pinvoke;
	}
	public String getPproc() {
		return pproc;
	}
	public void setPproc(String pproc) {
		this.pproc = pproc;
	}
	public String getPdata() {
		return pdata;
	}
	public void setPdata(String pdata) {
		this.pdata = pdata;
	}
	public String getPflow() {
		return pflow;
	}
	public void setPflow(String pflow) {
		this.pflow = pflow;
	}
	public String getPwfproc() {
		return pwfproc;
	}
	public void setPwfproc(String pwfproc) {
		this.pwfproc = pwfproc;
	}
	public int getPattr() {
		return pattr;
	}
	public void setPattr(int pattr) {
		this.pattr = pattr;
	}
	public Hashtable<String,String> getPbds() {
		return pbds;
	}
	public void setPbds(Hashtable<String,String> pbds) {
		this.pbds = pbds;
	}

	/**
	 * @return the beBill
	 */
	public boolean isBeBill() {
		return beBill;
	}

	/**
	 * @param beBill the beBill to set
	 */
	public void setBeBill(boolean beBill) {
		this.beBill = beBill;
	}

	/**
	 * @return the pclass
	 */
	public String getPclass() {
		return pclass;
	}

	/**
	 * @param pclass the pclass to set
	 */
	public void setPclass(String pclass) {
		this.pclass = pclass;
	}

	public boolean isBgroup() {
		return bgroup;
	}

	public void setBgroup(boolean bgroup) {
		this.bgroup = bgroup;
	}

	public ArrayList<String> getGroupfilds() {
		return groupfilds;
	}

	public void setGroupfilds(ArrayList<String> groupfilds) {
		this.groupfilds = groupfilds;
	}

	public ArrayList<String> getSumfilds() {
		return sumfilds;
	}

	public void setSumfilds(ArrayList<String> sumfilds) {
		this.sumfilds = sumfilds;
	}

	public String getCtype() {
		return ctype;
	}

	public void setCtype(String ctype) {
		this.ctype = ctype;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	} 
	
}
