/**
 * 
 */
package inetbas.web.outsys.tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import cl.ICL;

/**
 * 
 * @author www.bip-soft.com
 * 2019-05-07 14:00:09
 */
public class SQLInfoE implements Serializable{
	private String sql0;//原始sql
	private String sqlfrom;//from 后面的sql
	private String totalSql;//查询总数sql
	private String pagingSql;//分页sql
	private String orderBy;//orderBy
	private String groupBy;//group By
	private String whereCont;//where
	private int dbType;//数据库类型
	
	private ArrayList<SQLFiledInfo> filedInfos;
	public SQLInfoE() {	}
	public SQLInfoE(String _sql,int dtype) {
		sql0 = _sql;
		dbType = dtype;
	}
	
	
	public String getTotalSql() {
		return totalSql;
	}
	public void setTotalSql(String totalSql) {
		this.totalSql = totalSql;
	}
	public String getPagingSql() {
		return pagingSql;
	}
	public void setPagingSql(String pagingSql) {
		this.pagingSql = pagingSql;
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

	public ArrayList<SQLFiledInfo> getFiledInfos() {
		return filedInfos;
	}
	public void setFiledInfos(ArrayList<SQLFiledInfo> filedInfos) {
		this.filedInfos = filedInfos;
	}
	public int getDbType() {
		return dbType;
	}
	public void setDbType(int dbType) {
		this.dbType = dbType;
	}
	public String getSql0() {
		return sql0;
	}
	public void setSql0(String sql0) {
		this.sql0 = sql0;
	}
	public String getSqlfrom() {
		return sqlfrom;
	}
	public void setSqlfrom(String sqlfrom) {
		this.sqlfrom = sqlfrom;
	}
	/**
	 * @return
	 * 2019-05-07 18:04:00
	 */
	public String getCommFiled() {
		String sql1 = "";
		for (SQLFiledInfo sqlFiledInfo : filedInfos) {
			if(!sqlFiledInfo.isBsum())
				sql1+=sqlFiledInfo.getFiledIn()+",";
		}
		return sql1.substring(0, sql1.length()-1);
	}
	
	public String getCommOrderByFiled() {
		String sql1 = "";
		HashMap<String, String> k1 = new HashMap<String, String>();
		for (SQLFiledInfo sqlFiledInfo : filedInfos) {
			if(!sqlFiledInfo.isBsum()&&!sqlFiledInfo.getFiledIn().equals("null")) {
				String sf = sqlFiledInfo.getFiledIn();
				if(!k1.containsKey(sf)&&!CommUtils.isNumber(sf)) {
					sql1+=sqlFiledInfo.getFiledIn()+",";
					k1.put(sf, sf);
				}	
			}
			
		}
		return sql1.substring(0, sql1.length()-1);
	}
	
	public String getAllFiled(boolean bf1) {
		String sql1 = "";
		for (SQLFiledInfo sqlFiledInfo : filedInfos) {
			if(bf1)
				sql1+=sqlFiledInfo.getFiledNew()+",";
			else
				sql1+=sqlFiledInfo.getFiledIn()+" "+sqlFiledInfo.getFiledNew()+",";
		}
		return sql1.substring(0, sql1.length()-1);
	}
	
	
	/**
	 * 
	 * 2019-05-07 18:07:55
	 */
	public void makeTotal() {
		String filed1 = getFirstFiled();
		if(groupBy!=null&&groupBy.length()>0) {
//			String count = groupBy.replace("group by", "");
//			HVector hh = CCliTool.divide(count, ',', true);
//			String sel = "";
//			for (int i = 0; i < hh.size(); i++) {
//				String cc = hh.elementAt(i)+"";
//				if(cc.indexOf(")") !=-1){
//					cc = hh.elementAt(i) + " as hh"+i+" ";
//				}
//				if(i == hh.size()-1){ 
//					sel += cc;
//				}else{
//					sel += cc+",";
//				}
//			}
//			totalSql = "select count(*) from (select "+sel +" "+ sqlfrom+" "+groupBy+") b";	
			if(dbType == ICL.MYSQL) {
				filed1 = " ifnull("+filed1+",'') as coun";
				totalSql = "select count(*) from (select "+filed1+" " + sqlfrom+" "+groupBy+") b";
			}else{
				filed1 = " isnull("+filed1+",'') as coun";
				totalSql = "select count(*) from (select "+filed1+" " + sqlfrom+" "+groupBy+") b";
			}
		}else {
//			totalSql = "select count("+filed1+") " + sqlfrom;
			totalSql = "select count(*) " + sqlfrom;
		}
	}
	
	private String getFirstFiled() {
		String sql1 = "";
		for (SQLFiledInfo sqlFiledInfo : filedInfos) {
			if(!sqlFiledInfo.isBsum()) {
				sql1=sqlFiledInfo.getFiledIn();
				break;
			}
		}
		return sql1;
	}
	/**
	 * @param startNum
	 * @param pageSize
	 * 2019-05-08 09:26:49
	 */
	public void makeSelectPage(int startNum, int pageSize) {
		StringBuffer sBuffer = new StringBuffer();
		if(dbType == ICL.MYSQL) {
			sBuffer.append("select ").append(getAllFiled(false)).append(" ").append(sqlfrom).append(" ");
			if(groupBy!=null&&groupBy.length()>0)
			sBuffer.append(groupBy).append(" ");
			sBuffer.append(orderBy).append(" limit ").append(startNum).append(",").append(pageSize);
		}else {
			sBuffer.append("select top ").append(pageSize).append(" ").append(getAllFiled(true)).append(" from (");
			sBuffer.append("select ROW_NUMBER() over(").append(orderBy).append(") _r,").append(getAllFiled(false));
			sBuffer.append(" "+sqlfrom);
			if(groupBy!=null&&groupBy.length()>0) {
				sBuffer.append(" "+groupBy);
			}
//			if(orderBy!=null&&orderBy.length()>0) {
//				sBuffer.append(" ").append(orderBy);
//			}
			sBuffer.append(") b where _r>").append(startNum);
		}
		pagingSql = sBuffer.toString();	
	}
	public String getWhereCont() {
		return whereCont;
	}
	public void setWhereCont(String whereCont) {
		this.whereCont = whereCont;
	} 
}
