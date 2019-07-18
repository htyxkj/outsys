package inetbas.web.outsys.entity;

import inetbas.cli.cutil.CCliTool;
import inetbas.pub.coob.Cell;
import inetbas.web.outsys.uiparam.LayCell;

import java.util.ArrayList;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;

/**
 * 平台-WEB端辅助信息
 * @author www.bip-soft.com
 *
 */
public class BipInsAid {
	 
	private String title;//标题
	private int[] showColsIndex;//显示列下标
	private String[] showColsName;//显示列名称
	private String[] labers;//列标签   ---- 辅助中的标识
	private LayCell[] layCells;//数据类型
	private int type = 0;//辅助类型     0-select，1-group，2-gdic
	private String slink;//初始化SQL
	private String sflag;//标识
	private int total=0;//一个有多少条数据
	private ArrayList<JSONObject> values;//数据 
	
	private BipInsAidType bType;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	} 
	public String[] getLabers() {
		return labers;
	}
	public void setLabers(String[] labers) {
		this.labers = labers;
	}
	public LayCell[] getLayCells() {
		return layCells;
	}
	public void setLayCells(LayCell[] layCells) {
		this.layCells = layCells;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getSlink() {
		return slink;
	}
	public void setSlink(String slink) {
		this.slink = slink;
	}
	
	public int[] getShowColsIndex() {
		return showColsIndex;
	}
	public void setShowColsIndex(int[] showColsIndex) {
		this.showColsIndex = showColsIndex;
	}
	public String[] getShowColsName() {
		return showColsName;
	}
	public void setShowColsName(String[] showColsName) {
		this.showColsName = showColsName;
	}
	public ArrayList<JSONObject> getValues() {
		return values;
	}
	public void setValues(ArrayList<JSONObject> values) {
		this.values = values;
	}
	public void mklayCells(Cell[] cells){
		LayCell[] lay = new LayCell[cells.length];
		for (int i = 0; i < cells.length; i++) {
			Cell cell = cells[i];
			LayCell layCell = new LayCell();
			layCell.id=cell.ccName;
			layCell.type=cell.ccType;//字段类型
			layCell.labelString=cell.labelString;//标签名称
			lay[i] = layCell;
		} 
		setLayCells(lay);
	}
	public void mklaySQLCells(String sql){
		sql = sql.split("from")[0];
		sql = sql.replace("select", "");
		String[] item = sql.split(","); 
		LayCell[] lay = new LayCell[item.length];
		for (int i = 0; i < item.length; i++) { 
			String it = item[i];
			it = it.trim();
			String[] itkg = it.split(" ");
			if(itkg!=null && itkg.length>=2){
				it = itkg[itkg.length-1].trim();
			}
			String[] itas = it.split("as");
			if(itas!=null && itas.length>=2){
				it = itkg[itas.length-1].trim();
			}
			String[] itAS = it.split("AS");
			if(itAS!=null && itAS.length>=2){
				it = itkg[itAS.length-1].trim();
			}
			
			LayCell layCell = new LayCell();
			layCell.id=it;
			layCell.type=12;//字段类型
			layCell.labelString=it;//标签名称
			lay[i] = layCell;
		} 
		setLayCells(lay);
	}
	public void mklables(String sflag){
		if(sflag!=null&&sflag.length()>2){
			int _idxfh = sflag.indexOf(";");
			if(_idxfh>=0){
				String cel = sflag.substring(0,_idxfh);
				sflag = sflag.substring(_idxfh+1);
				String[] shlb = cel.split(","); 
				setShowColsName(shlb); 
			}
			_idxfh = sflag.indexOf("/");
			if(_idxfh>0){
				String _idxstr = sflag.substring(0,_idxfh);
				String[] _idx = _idxstr.split(",");
				sflag = sflag.substring(_idxfh+1); 
				if(_idx.length>0)
					setShowColsIndex(_idx);
			}
			if(_idxfh==0){
				sflag = sflag.substring(1);
			}
			_idxfh = sflag.indexOf("/");
			if(_idxfh>0){
				String showlb = sflag.substring(0,_idxfh);
				setLabers(showlb.split(","));
				sflag = sflag.substring(_idxfh+1);
			}else {
				setShowColsIndex(sflag.split(","));
			}
		}
	}
	private void setShowColsIndex(String[] _idx){
		int[] indexs = new int[_idx.length];
		boolean isShowCel = true;
		for(int i=0;i<_idx.length;i++){
			int indx = CCliTool.objToInt(_idx[i], -1);
			if(indx>-1)
				indexs[i] = CCliTool.objToInt(indx, 0);
			else{
				isShowCel = false;
				break;
			}
		}
		if(isShowCel)
			setShowColsIndex(indexs);
	}
	public String getSflag() {
		return sflag;
	}
	public void setSflag(String sflag) {
		this.sflag = sflag;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public BipInsAidType getbType() {
		return bType;
	}
	public void setbType(BipInsAidType bType) {
		this.bType = bType;
	}

}
