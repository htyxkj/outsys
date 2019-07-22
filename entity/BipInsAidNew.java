package inetbas.web.outsys.entity;

import inetbas.cli.cutil.CCliTool;
import inetbas.web.outsys.uiparam.LayCell;
import inetbas.web.outsys.uiparam.LayCells;

import java.util.ArrayList;

import com.alibaba.fastjson.JSONObject;


/**
 * 平台-WEB端辅助类
 * @author www.bip-soft.com
 *
 */
public class BipInsAidNew {
	private String id;
	private String title;//标题
	private int[] showColsIndex;//显示列下标
	private String[] showColsName;//显示列名称
	private String[] labers;//列标签   ---- 辅助中的标识
	private LayCells cells;//数据对象

	private LayCells contCells;//条件Cells
	private String slink;//初始化SQL
	private String sflag;//标识
	private String sref;//参照+
	private int total=0;//一个有多少条数据
	
	private String groupFld;
	
	private ArrayList<JSONObject> values = new ArrayList<JSONObject>();//数据 
	
	private BipInsAidType bType;//辅助对象类型
	private String addCellId;//新增按钮对应的添加CellId
	private LayCells addCells;//添加数据对象
	
	private boolean mutiple;//是否可以多选
	private boolean cl;//是否是常量
	
	private String realV;//实际值
	private String showV;//参照值
	
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
	public String getSlink() {
		return slink;
	}
	public void setSlink(String slink) {
		this.slink = slink;
		if(slink!=null) {
			int n = slink.indexOf("&");
			if(n>0) {
				sref = slink.substring(0, n);
				slink = slink.substring(n+1);
			}
		}
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
	}
	public void mklables(String sflag){
		int _n = sflag.lastIndexOf("/");
		if(_n>-1) {
			if(sflag.length()-1>_n) {
				char c = sflag.charAt(_n+1);
				if(c>='A'&&c<='Z') {
					mutiple = true;
				}
			}
		}
		if(sflag!=null&&sflag.length()>2){
			_n = sflag.indexOf("|");
			if(_n>-1) {
				addCellId = sflag.substring(_n+1);
				sflag = sflag.substring(0,_n);
			}

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
	public LayCells getCells() {
		return cells;
	}
	public void setCells(LayCells cells) {
		this.cells = cells;
	}
	public LayCells getContCells() {
		return contCells;
	}
	public void setContCells(LayCells contCells) {
		this.contCells = contCells;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getGroupFld() {
		return groupFld;
	}
	public void setGroupFld(String groupFld) {
		this.groupFld = groupFld;
	}
	public String getAddCellId() {
		return addCellId;
	}
	public void setAddCellId(String addCellId) {
		this.addCellId = addCellId;
	}
	public LayCells getAddCells() {
		return addCells;
	}
	public void setAddCells(LayCells addCells) {
		this.addCells = addCells;
	}
	public boolean isMutiple() {
		return mutiple;
	}
	public void setMutiple(boolean mutiple) {
		this.mutiple = mutiple;
	}
	public String getSref() {
		return sref;
	}
	public void setSref(String sref) {
		if(sref!=null)
			this.sref = sref;
	}
	public boolean isCl() {
		return cl;
	}
	public void setCl(boolean cl) {
		this.cl = cl;
	}
	public String getRealV() {
		return realV;
	}
	public void setRealV(String realV) {
		this.realV = realV;
	}
	public String getShowV() {
		return showV;
	}
	public void setShowV(String showV) {
		this.showV = showV;
	}
	
	

}
