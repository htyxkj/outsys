/**
 * 
 */
package inetbas.web.outsys.entity;

import java.io.Serializable;
import java.util.ArrayList;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author www.bip-soft.com
 * 2019-09-06 09:41:07
 */
public class BipTreeNode implements Serializable{
	private String id;
	private String label;
	private JSONObject data;
	
    @JSONField(serialize=false)
    private BipTreeNode parentNode;
    private boolean haveChildren = false;
    private ArrayList<BipTreeNode> children = new ArrayList<BipTreeNode>();
    
    private boolean isTop = false;
    
    public BipTreeNode(){}
    public BipTreeNode(String _id,String _name){
    	id = _id;
    	label = _name;
    }
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public JSONObject getData() {
		return data;
	}
	public void setData(JSONObject data) {
		this.data = data;
	}
	public BipTreeNode getParentNode() {
		return parentNode;
	}
	public void setParentNode(BipTreeNode parentNode) {
		this.parentNode = parentNode;
	}
	public boolean isHaveChildren() {
		return haveChildren;
	}
	public void setHaveChildren(boolean haveChildren) {
		this.haveChildren = haveChildren;
	}
	public ArrayList<BipTreeNode> getChildren() {
		return children;
	}
	public void setChildren(ArrayList<BipTreeNode> children) {
		this.children = children;
	}
	
    public void addChild(BipTreeNode node) {
        if (children == null)
            children = new ArrayList<BipTreeNode>();
        if(this.id=="")
             node.setTop(true);
        node.setParentNode(this);
        haveChildren = true;
        children.add(node);
    }
	public boolean isTop() {
		return isTop;
	}
	public void setTop(boolean isTop) {
		this.isTop = isTop;
	}
    
    
	
	

}
