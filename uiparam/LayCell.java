/**
 * 
 */
package inetbas.web.outsys.uiparam;

import inetbas.pub.coob.CBasTool;
import inetbas.pub.coob.Cell;

import java.sql.Types;
import java.util.ArrayList;

import cl.ICL;

/**
 * @author www.bip-soft.com
 *
 */
public class LayCell {
	public String id="";
	public int type=Types.VARCHAR;//字段类型
	public String labelString;//标签名称
	public int ccHorCell=1,ccVerCell=1,alignment;//宽度单元数,高度单元数
	public String chkRule="", script=""; //校验值;公式
	public boolean unNull=false;//非空
	public String parentId=""; //父对象ID
	public long attr;//属性用64位表示
	public int editType = ICL.edTextField; //编辑器类型,参照常量定义
	public int ccLeng,ccPoint; //编辑器类型,参照常量定义
	public int lnk_inn=0;
	public String initValue,editName="",editLink=""; //缺省值
	public Object refValue; //参照值
	public String desc;//提示文字 
	
	public int index=0;
	public String psAutoInc;
	public boolean isShow = true;
	public boolean isReq = false;

	public boolean assist = false; //是否是辅助
	public String assType = null;//辅助类型
	
	public int widthIndex = 0;//cel宽度下标 只在表格中用到
	public int ccCharleng=0;//文本框长度
	public ArrayList<String> refCellIds = new ArrayList<String>();
	public ArrayList<String> pRefIds = new ArrayList<String>();
	public LayCell(){}

	public LayCell(Cell cell) {
		
		this.id = cell.ccName;
		this.labelString = cell.labelString;
		this.initValue = cell.initValue;
		this.ccHorCell = cell.ccHorCell;
		this.ccVerCell = cell.ccVerCell;
		this.desc = cell.desc;
		this.attr = cell.attr;
		this.ccLeng = cell.ccLeng;
		this.refValue = cell.refValue; 
		this.index = cell.index;
		this.parentId = (cell.c_par == null ? "" : cell.c_par.obj_id);
		String s0 = cell.chkRule;
		int t0 = s0 == null || s0.length() < 3 || s0.charAt(0) != '{' ? -1 : s0
				.indexOf('}');
		if(t0>0){
			this.chkRule = s0.substring(t0 + 1);
			s0 = s0.substring(1, t0);
			t0 = s0.indexOf('@');// ;--菜单。
			if (t0 < 0)
				t0 = s0.indexOf('$');// ;--常量表。
			if (t0 >= 0) {
				this.editLink = s0.substring(t0); // ;-
				s0 = t0 > 1 ? s0.substring(0, t0) : null;
			}
			t0 = t0 == 0 ? -1 : s0.indexOf('&');
			if (t0 > 0) {
				this.alignment = s0.charAt(0) - '0';
				this.editName = s0.length() > 2 ? s0.substring(2) : null;
			} else
				this.editName = s0;
		}
		this.script = cell.script;
		this.type = cell.ccType;
		this.editType = cell.editType;
		this.ccPoint = cell.ccPoint;
		this.psAutoInc = cell.psAutoInc;
		this.lnk_inn = cell.lnk_inn;
		this.ccCharleng = cell.ccNumChar;
		initAttr();
		initCharLeng(type);
		this.ccCharleng = (int) getNumChar();

	}
	
	public float getNumChar() {
		 int cn = ccCharleng;
		 if (cn < 42)
		  return cn;
		 int cc = ccLeng;
		 if (cc > 20)
		  return cn;
		 int itp = type;
		 //--长度=数值:(1~20),字符串>10*长度
		 if (CBasTool.isNumber(type) || ((itp == Types.VARCHAR || itp == Types.CHAR || itp == Types.NVARCHAR) && cn >= cc * 10))
		  return cn / 10.0f;
		 return cn;
	}
	
	public void initCharLeng(int t0){
		if (ccCharleng < 2) {// 定义时至少2个字符宽
			if (t0 == Types.DATE)
				ccCharleng = 10;
			else if (t0 == Types.TIMESTAMP || t0 == Types.TIME)
				ccCharleng = 15;
			else if (t0 == Types.SMALLINT)
				ccCharleng = 4;
			else if (t0 == Types.INTEGER || t0 == Types.BIGINT)
				ccCharleng = 6;
			else if (t0 == Types.DECIMAL || t0 == Types.NUMERIC) {
				t0 = ccLeng;
				ccCharleng = t0 < 5 ? 5 : (t0 > 12 ? 12 : t0);
			} else if (t0 < 0)
				ccCharleng = 25;
			else {
				t0 = ccLeng;
				ccCharleng = t0 < 3 ? 3 : (t0 > 60 ? 60 : t0);
			}
		}
	}

	/**
	 * @param attr2
	 */
	private void initAttr() {
		
		if((attr&Cell.PRIMARY)>0){
			this.unNull = true;
			this.isReq = true;
		}
		if((attr&Cell.NOTNULL)>0){
			this.unNull = true;
			this.isReq = true;
		}
		if((attr&Cell.HIDDEN)>0){
			this.isShow = false;
		}
		if((attr&Cell.AIDINPUT)>0){
			this.assist = true;
		}
	}
	
	
}
