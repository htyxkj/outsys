/**
 * 
 */
package inetbas.web.outsys.entity;

import java.io.Serializable;
import java.util.ArrayList;

import com.alibaba.fastjson.JSONObject;

/**
 * @author www.bip-soft.com
 *
 */
public class PageLayOut implements Serializable {
	public int currentPage = 1;//当前页；
	public int pageSize = 20; // 每页条数
	public ArrayList<JSONObject> celData; //返回的数据
	public int totalItem = 0;//总条数
	public int totalPage = 0;//总页数
	
	public String queryCriteria="";
	
	public String orderBy = "";//排序字段
	
	public PageLayOut() {
		
	}
	
	public PageLayOut(int page,int pagesize) {
		currentPage=(page<1?1:page);
		pageSize = pagesize;
	}
	
	public PageLayOut(int page,int pagesize,String cont) {
		currentPage=(page<1?1:page);
		pageSize = pagesize;
		queryCriteria = cont==null?"":cont;
	} 
	
	
	public void setTotalSize(int count){
		totalItem = count;
		totalPage = (count%pageSize)>0?(count/pageSize+1):(count/pageSize);
	}
	
}
