package inetbas.web.outsys.entity;

import inetbas.cli.cutil.CCliTool;

/**
 * @author www.bip-soft.com
 *
 */
public class AssistObj {
	private String sid;
	private String slable;
	private String slink;
	private String sclass;
	private String[] showCel;
	private int[] showCel_idx=null;
	private String[] showCel_lable;
	private String queryStr="";
	private String whereStr="";
	private String orderBy="";
	private String groupBy="";
	private String[] cols;
	private boolean distinct = false;//是否包含 distinct
	public int type = 0;//ASSIST_SELECE=0,ASSIST_GROUP=1,ASSIST_GDIC=2;//辅助执行的是  inetbas.cli.cutil.CGroupEditor 类
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	public String getSlable() {
		return slable;
	}
	public void setSlable(String slable) {
		this.slable = slable;
	}
	public String getSlink() {
		return slink;
	}
	public void setSlink(String slink) {
		this.slink = slink;
		makeSQLString();
	}
	/**
	 * 
	 */
	private void makeSQLString() {
		if(this.slink.length()>0){
			String _str = this.slink;
			if(_str.startsWith("#[0~]")){
				_str = _str.replace("#[0~]", "");
				this.slink = _str;
			}
			
			if(_str.indexOf("distinct")!=-1){
				this.distinct = true;
				_str = _str.replace("distinct", "");
			}
			
			_str = _str.toLowerCase();
			int _idx = _str.indexOf("where");
			int _group = _str.indexOf("group by");
			int _order = _str.indexOf("order by");
			
			if(_idx>=0){
				this.queryStr = _str.substring(0,_idx).trim();
			}
			if(_group>=0){
				if(_idx<0)
					this.queryStr = _str.substring(0,_group);
				this.groupBy = _str.substring(_group).trim();
				int aa=0;
				aa=this.groupBy.indexOf("order by");
				if(aa>0){
					this.groupBy = this.groupBy.substring(0,aa).trim();
				}
			}
			if(_order>=0){
				if(_idx<0)
					this.queryStr = _str.substring(0,_order);
				this.orderBy = _str.substring(_order).trim();
			}
			
			if(_idx>=0){
				if(_group>0){
					this.whereStr = _str.substring(_idx,_group).trim();
					if(_order>=0){
						this.groupBy = _str.substring(_group,_order).trim();
					}else{
						this.groupBy = _str.substring(_group).trim();
					}
				}else{
					if(_order>=0){
						this.whereStr = _str.substring(_idx,_order).trim();
						this.orderBy = _str.substring(_order).trim();
					}else{
						this.whereStr = _str.substring(_idx);
					}
				}
				
			}
			if(_idx<0&&_order<0&&_group<0){
				this.queryStr = this.slink;
			}
			_idx = _str.indexOf("select");
			_order = _str.indexOf("from");
			_str = _str.substring(_idx+6,_order).trim();
			cols = _str.split(",");
//			if(this.showCel_idx==null&&this.showCel.length>0){
			if(this.showCel_idx==null&&this.showCel != null&&this.showCel.length>0){
				int len = this.showCel.length;
				int _index[] = new int[len];
				for(int i=0;i<len;i++){
					String id = this.showCel[i];
					for(int j=0;j<cols.length;j++){
						String id1 = cols[j];
						if(id.equals(id1)){
							_index[i] = j;
							continue;
						}
					}
				}
				this.showCel_idx = _index;
			}else{
				int colslen = this.showCel_idx.length;
				showCel = new String[colslen];
				for(int i=0;i<colslen;i++){
					int m = this.showCel_idx[i];
					if(m>colslen) {
						showCel[i] = cols[0];
					}else {
						showCel[i] = cols[m];
					}
					
				}
			}
		}
	}
	public String[] getShowCel() {
		return showCel;
	}
	public void setShowCel(String[] showCel) {
		this.showCel = showCel;
	}
	public int[] getShowCel_idx() {
		return showCel_idx;
	}
	public void setShowCel_idx(int[] showCel_idx) {
		this.showCel_idx = showCel_idx;
	}
	public String[] getShowCel_lable() {
		return showCel_lable;
	}
	public void setShowCel_lable(String[] showCel_lable) {
		this.showCel_lable = showCel_lable;
	}
	
	public void mklables(String sflag){
		if(sflag!=null&&sflag.length()>2){
			int _idxfh = sflag.indexOf(";");
			if(_idxfh>=0){
				String cel = sflag.substring(0,_idxfh);
				sflag = sflag.substring(_idxfh+1);
				String[] shlb = cel.split(",");
				setShowCel(shlb);
			}
			_idxfh = sflag.indexOf("/");
			if(_idxfh>0){
				String _idxstr = sflag.substring(0,_idxfh);
				String[] _idx = _idxstr.split(",");
				sflag = sflag.substring(_idxfh+1);
				if(_idx.length>0)
					setShowCel_idx(_idx);
			}
			if(_idxfh==0){
				sflag = sflag.substring(1);
			}
			_idxfh = sflag.indexOf("/");
			if(_idxfh>0){
				String showlb = sflag.substring(0,_idxfh);
				setShowCel_lable(showlb.split(","));
				sflag = sflag.substring(_idxfh+1);
			}else {
				setShowCel_idx(sflag.split(","));
			}
		}
	}
	/**
	 * @param _idx
	 */
	private void setShowCel_idx(String[] _idx) {
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
			setShowCel_idx(indexs);
	}
	public String getQueryStr() {
		return queryStr;
	}
	public void setQueryStr(String queryStr) {
		this.queryStr = queryStr;
	}
	public String getWhereStr() {
		return whereStr;
	}
	public void setWhereStr(String whereStr) {
		this.whereStr = whereStr;
	}
	public String getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	public String getGroupBy() {
		return groupBy;
	}
	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}
	/**
	 * @param cont
	 * @return
	 */
	public String makeQuery(String cont) {
		String sql=this.queryStr;
		if(this.distinct){
			sql = sql .replace("select", "select distinct ");
		}
		String where = "(";
		int nn = cont.indexOf("~");
		int mm = cont.indexOf("@");
		if(nn<0 && mm<0){
			for(int i=0;i<this.showCel.length;i++){
				String kk = this.showCel[i];
				kk = kk.replaceAll("distinct", "");
				if(i==0){
					where +=""+kk+" like '%"+cont+"%'";
				}else{
					where +=" or "+kk+" like '%"+cont+"%'";
				}
			}
		}else if(mm>=0){
			cont = cont.replace("@", "");
			for(int i=0;i<this.showCel.length;i++){
				String kk = this.showCel[i];
				kk = kk.replaceAll("distinct", "");
				if(i==0){
					where +=""+kk+" in "+cont+"";
				}else{
					where +=" or "+kk+" in "+cont+"";
				}
			}
		}else{
			cont = cont.substring(1);
			where += cont;
		}
		where+=")";
		if(this.whereStr.length()>0){
			sql+=" "+ this.whereStr+" and " + where +" "+ groupBy+" "+ orderBy;
		}else{
			sql+=" where " + where +" "+ groupBy+" " + orderBy;
		}
		return sql;
	}
	public String[] getCols() {
		return cols;
	}
	public void setCols(String[] cols) {
		this.cols = cols;
	}
	public String getSclass() {
		return sclass;
	}
	public void setSclass(String sclass) {
		this.sclass = sclass;
	}
	

}
