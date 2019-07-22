/**
 * 
 */
package inetbas.web.outsys.tools;

import java.util.regex.Pattern;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;

import inet.HVector;
import inetbas.cli.cutil.CCliTool;
import inetbas.cli.cutil.CCliToolEx;
import inetbas.cli.cutil.CDBLINK;
import inetbas.pub.coob.Cells;

/**
 * @author www.bip-soft.com 2019-07-02 11:09:22
 */
public class CommUtils {
	/**
	 * [!][fp0|fp1..,]..f0,f1,..,fcp[&qtybk|cid]/t0...&... 数值可以用多个,比较以第一项为准,其它的按比率折算
	 * 返回值:分组字段,取值字段,条件
	 */
	public static String[] divItem(String s0, int cn) {
		int x1 = s0.indexOf('/'), x0 = s0.indexOf('&');
		String[] ss = new String[] { null, null, s0.substring(x1 + 1) };// ;--条件
		if (x0 > 0)
			x1 = x0;
		x0 = x1;
		while (cn > 0) {
			x0 = s0.lastIndexOf(',', x0 - 1);
			cn--;
		}
		ss[0] = s0.substring(0, x0);// ;--分组
		if (x0 < x1)
			ss[1] = s0.substring(x0 + 1, x1);
		return ss;
	}

	/**
	 * 返回值的位为(0:-)(1:+) 来源只能有一个账套连接,由外部传入。
	 */
	public static int calcSQL(HVector vsql, String sto, int cn, String vid, boolean bly) {
		int x0, x1, x2, ifhb = 0, iB = 1;
		String s0, sf, ss0[], fld, sc, dbto;
		CDBLINK dbo = null;
		while (true) {
			x0 = CCliTool.indexOf(sto, ';', 0, true, true);
			s0 = x0 < 0 ? sto : sto.substring(0, x0);
			if (CCliTool.havedblnk(s0)) {
				// --来源可以区分不同的账套。
				if (dbo == null)
					dbo = new CDBLINK();
				CDBLINK.toLink(dbo, s0, null, false);
				dbto = dbo.dblnk;
				s0 = dbo.bds;
			} else
				dbto = null;
			if (s0.charAt(0) == '-') {
				s0 = s0.substring(1);
				ifhb |= iB;
			}
			sf = cl.ICL.EQ_queryVec;
			ss0 = divItem(s0, cn);
			fld = ss0[0];// 分组字段
			sc = ss0[2];// 查询条件
			x2 = sc.lastIndexOf('/');
			x1 = fld.indexOf(cl.ICL.CH_FLD_DIV);// 是否是复合组件
			if (x1 > 0) {
				if (bly) {
					s0 = fld.substring(x1 + 1);
					fld = fld.substring(0, x1);// ;--只取来源单号
					x1 = s0.indexOf(',');
					if (x1 > 0)
						fld += s0.substring(x1);// ;-加上其它非复合主键
				} else {
					fld = fld.replace(cl.ICL.CH_FLD_DIV, ',');// ;--多主键时,来源单合成一个值返回。
					sf += "*" + CCliTool.toUnion(0, CCliTool.calccount(fld, ','), cl.ICL.CH_FLD_DIV);
				}
			}
			s0 = ss0[1];
			if (cn > 0)
				s0 = "sum(" + CCliTool.replace(s0, ',', "),sum(") + ")";// ;--数值型比较
			s0 = "select " + fld + "," + s0 + " from " + (x2 < 0 ? sc : sc.substring(0, x2)) + " where ";
			// ;--条件在初始读入时已做宏和变量格式化。
			if (bly)
				s0 += CCliTool.updateSQLVID(sc.substring(x2 + 1), vid, true);// ;--引用主表单号。
			else {
				s0 += fld + " in " + vid;// ;--来源单号
				if (x2 > 0) {
					sc = filterRef(sc.substring(x2 + 1));// ;-去掉引用关联
					if (sc != null && sc.length() > 0)
						s0 += " and " + sc;
				}
			}
			if (cn > 0)
				s0 += " group by " + fld;
			s0 += " order by " + fld;
			if (cn < 1)
				s0 += "," + s0;// ;--无比较字段
			if (dbto != null && dbto.length() > 0)
				s0 = "@" + dbto + "@" + s0;
			vsql.addElement(s0);
			vsql.addElement(sf);
			if (x0 < 0)
				return ifhb;
			sto = sto.substring(x0 + 1);
			iB <<= 1;
		}
	}

	/**
	 * 去掉"?"变量
	 */
	public static String filterRef(String sc) {
		int x1 = sc.indexOf('?'), x0, t0;
		char c0 = '0', cs0[];
		while (x1 > 0) {
			cs0 = sc.toCharArray();
			x0 = x1;
			t0 = cs0.length;
			while (x0 >= 0) {
				c0 = cs0[x0];
				x0--;
				if (c0 == '<' || c0 == '>' || c0 == '=')
					break;
			}
			while (x0 >= 0) {
				c0 = cs0[x0];
				if (c0 > ' ' && c0 != '<' && c0 != '>' && c0 != '=')
					break;// ;--找到字段名
			}
			for (; x0 >= 0 && cs0[x0] > ' '; x0--)
				;// ;-字段名
			for (; x0 >= 0 && cs0[x0] <= ' '; x0--)
				;// ;-空格
			for (; x0 >= 0 && cs0[x0] > ' '; x0--)
				;// ;-"and","or"等关键字
			for (x1++; x1 < t0 && cs0[x1] > ' '; x1++)
				;// ;-跳过当前项
			if (x0 <= 0) {
				// ;--前面到头
				for (; x1 < t0 && cs0[x1] <= ' '; x1++)
					;// ;-空格
				for (; x1 < t0 && cs0[x1] > ' '; x1++)
					;// ;-"and","or"等关键字
				for (; x1 < t0 && cs0[x1] <= ' '; x1++)
					;// ;-空格
				sc = x1 < t0 ? sc.substring(x1) : null;
			} else {
				if (x1 < t0)
					sc = sc.substring(0, x0) + sc.substring(x1);
				else
					sc = sc.substring(0, x0);
			}
			if (sc == null || sc.length() < 1)
				return null;
			x1 = sc.indexOf('?');
		}
		return sc;
	}

	/**
	 * 将字段转参照字段
	 * 2019-07-03 09:53:57
	 */
	public static  String fieldMap(String sfld, String smap) {
		 if (smap == null || smap.length() < 1)
		  return sfld;
		 //;--注意组合字段
		 HVector v0 = CCliToolEx.divide(sfld, ',', false, String.valueOf(cl.ICL.CH_FLD_DIV));
		 int t0 = v0.size() - 1, i;
		 String sdiv = "," + v0.elementAt(t0), ss0[] = new String[t0];
		 for (i = 0;i < t0;i++)
		  ss0[i] = (String) v0.elementAt(i);
		 ss0 = CCliTool.fieldMap(ss0, CCliToolEx.mapToAry(smap), false);//;--按反向计算
		 t0 = ss0.length;
		 sfld = "";
		 for (i = 0;i < t0;i++)
		  sfld += sdiv.charAt(i) + ss0[i];
		 return sfld.substring(1);
		}
	public static String checkcomp(String scp) {
		int x0 = scp.indexOf(cl.ICL.CH_FLD_DIV);// --有匹配
		if (x0 < 1)
			return scp;
		int x1 = scp.indexOf(',', x0);
		String s0 = scp.substring(0, x0);
		return x1 < 1 ? s0 : s0 + scp.substring(x1);
	}

	/***
	 * SQL语句格式化
	 * @param bForSql
	 * @param cont
	 * @return
	 */
	public static String formartSql(String bForSql) {
		if(bForSql==null) {
			return null;
		}
		if (bForSql.startsWith("[0~]")) {
			bForSql = bForSql.replace("[0~]", "");
		}
		if (bForSql.startsWith("[0+]")) {
			bForSql = bForSql.replace("[0+]", "");
		}
		if (bForSql.startsWith("#[0~]")) {
			bForSql = bForSql.replace("#[0~]", "");
		}
		if (bForSql.startsWith("#[0+]")) {
			bForSql = bForSql.replace("#[0+]", "");
		}
		if (bForSql.startsWith("0~")) {
			bForSql = bForSql.replace("0~", "");
		}
		if (bForSql.indexOf("&select") > 0) {
			int c0 = bForSql.indexOf("&");
			bForSql = bForSql.substring(c0 + 1);
		}
		return bForSql;
	}

	public static String getContStr(Cells cell,String jsonData) {
		if(CCliTool.isNull(jsonData, true)) {
			return null;
		}
		int[] its = cell.getTypes(null);
		String[] flds = cell.getNames(null, false,true);
		Object[] os0 = getFldData(cell,jsonData);
		String s0 = CCliTool.toCondictions(flds, its, CCliTool.toAutoFit(cell, os0));
		return s0;
	}
	
	/**
	 * @param cell 查询对象Cell
	 * @param jsonData 界面传递过来的JSON格式的数据
	 * @return
	 * 2019-03-20 17:53:19
	 */
	public static Object[] getFldData(Cells cell, String jsonData) {
		JSONObject jsonObject = JSONObject.parseObject(jsonData);
		int len = cell.all_cels.length;
		Object[] os0 = new Object[len];
		for(int i=0;i<len;i++) {
			String id = cell.all_cels[i].ccName;
			if(jsonObject.containsKey(id)) {
				os0[i] = jsonObject.get(id);
			}
		}
		return os0;
	}
	
	   public static boolean isNumber(String string) {
	        if (string == null)
	            return false;
	        Pattern pattern = Pattern.compile("^-?\\d+(\\.\\d+)?$");
	        return pattern.matcher(string).matches();
	    }
	   
	
	
}
