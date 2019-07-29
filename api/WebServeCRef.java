/**
 * 
 */
package inetbas.web.outsys.api;

import inetbas.CMain;
import inetbas.cli.cutil.CCliTool;
import inetbas.cli.cutil.CVarImpl;
import inetbas.pub.coob.Cell;
import inetbas.pub.coob.Cells;
import inetbas.pub.cutil.CRefBas;
import inetbas.pub.cutil.GOException;
import inetbas.pub.cutil.IMacroListener;
import inetbas.pub.cutil.IREFListener;
import inetbas.pub.cutil.ISQLVarListener;

/**
 * @author www.bip-soft.com
 * 2019-07-29 11:44:16
 */
public class WebServeCRef implements ISQLVarListener {
	private CRefBas _rf = null;
	public boolean b_qc = false;
	private IMacroListener _ima;
	/**
	 * @param cc
	 * @param eq
	 */
	public WebServeCRef(CRefBas cc, IMacroListener ima) {
		_rf = cc;
		_ima = ima;
	}

	@Override
	public String proc_SqlVar(String sv) throws Exception {
		String s0 = CCliTool.toCorpSQLVar(sv, CMain.i_co);
		if (s0 != null)
			return s0;
		int x1 = sv.length() - 1;
		CRefBas ref = _rf;
		if (ref != null) {
			IREFListener rv = ref._oMFT;
			if (rv != null && sv.charAt(0) == cl.INN.CH_SQLVAR_I)
				return rv.refformat(sv.substring(1));// ;--外部定义
		}
		if (sv.charAt(0) == '[' && sv.charAt(x1) == ']')
			return CCliTool.formatMacroItm(sv.substring(1, x1), _ima);// ;--宏定义中的值。
		Object ov = CCliTool.toF_K_V(sv);
		String sf = null, sk = null;
		char c0, chinn = cl.ICL.INN_AUTO;
		boolean bqc = false;
		if (ov instanceof String == false) {
			String[] fvs = (String[]) ov;
			sf = fvs[0];
			c0 = sf.charAt(0);
			if (c0 == '\\')
				sf = sf.substring(1);// 加转义符
			else if (c0 == '#') {
				chinn = sf.charAt(1);// ;-指定SQL类型,转化时不依赖单元定义。
				sf = sf.substring(2);
			} else {
				bqc = b_qc && c0 == '@';// ;-期初日期特别标识
				if (bqc)
					sf = sf.substring(1);
			}
			sk = fvs[1];
			sv = fvs[2];
		}
		c0 = sv.charAt(0);
		if (c0 == cl.ICL.CH_MAXYM && sv.length() == 1)
			return String.valueOf(CVarImpl.getMaxYM());
		if (c0 == cl.ICL.CH_CMCODE || c0 == cl.ICL.CH_ORGCODE || c0 == cl.ICL.CH_USRCODE)
			return CCliTool.toOUCond(sf, sk, sv, c0, _ima);
		x1 = sv.indexOf('|');
		int iYMD = -1;
		if (x1 > 0) {
			// ;--日期格工|数值公式。
			iYMD = CCliTool.toVarYMD(sv.substring(0, x1), c0, x1);// ;--检查指定日期的格式。
			sv = sv.substring(x1 + 1);
		}
		boolean bmul = false, bvdiv = false;
		char cnot = 0;
		if (sv != null && sv.length() > 0) {
			bmul = sv.charAt(0) == '~';// ;--复合字段(需要过滤处理)
			if (bmul)
				sv = sv.substring(1);
			cnot = sv.length() > 3 && sv.charAt(0) == '(' && sv.charAt(2) == ')' ? sv.charAt(1) : 0;
			if (cnot > 0)
				sv = sv.substring(3);
			if (sv.charAt(0) == '^') {
				sv = sv.substring(1);// ;--父对象
				//ref = _rfpar;
			}
			bvdiv = sv.charAt(0) == ',';// ;--','换成";"
			if (bvdiv)
				sv = sv.substring(1);
			x1 = ref == null ? -1 : ref.ref_Index(sv);
			if (x1 < 0)
				throw new RuntimeException(cl.IERR.ERR_UNDEFINE + "<" + cl.IERR.ERR_CELL + ">" + sv);
			if (ref.c_vals == null)
				return null;
			ov = ref.getValue(x1);
		} else
			ov = null;
		Cells cell = ref.c_cell;
		Cell cels[] = cell.db_cels, cel = cels[x1];
		int itp;
		if (chinn == cl.ICL.INN_AUTO) {
			itp = cel.ccType;
			chinn = CCliTool.sqlTypeToChar(itp);// ;--内部SQL类型(可以由外部指定)。
		} else
			itp = cl.ICL.EQOK_OTHERR;
		boolean bDT = chinn == cl.ICL.INN_DATE || chinn == cl.ICL.INN_TIME;
		boolean bundiv = false, bne = ref == null ? false : ref.nullexit;
		if (bDT && iYMD >= 0) {
			bundiv = (iYMD & 0x100) != 0;// ;--年月没有分隔。
			sv = CCliTool.dateToString(ov, !bundiv, iYMD & 0xFF);// ;--日期格式
		} else {
			sv = CCliTool.objToString(ov, cel.ccType);// ;-保持单元指定类型
			if (bmul && sv != null)
				sv = CCliTool.toMSQLString(sv, true);// ;-复合字段
		}
		if (sv != null && (bvdiv || cnot != 0)) {
			if (cnot != 0) {
				if (sv.charAt(0) == cnot)
					sv = sv.substring(1);
				sv = sv.replace(cnot, ',');// ;-替换标识字符
			}
			if (bvdiv)
				sv = sv.replace(',', ';');
		}
		if (sk != null && sk.length() > 0) {
			c0 = sk.charAt(0);
			// ;--检查条件中的非空项。
			boolean notNull = (cell.attr & cl.ICL.ocCondiction) != 0 && (cel.attr & Cell.NOTNULL) != 0;// ;-不为空的单元必需有值
			if (c0 == '=') {
				if ((cel.attr & Cell.AUTOFIT) != 0 && CCliTool.isAutoFit(sv))
					sv = "%" + sv + "%";// 匹配功能
				if (itp == cl.ICL.EQOK_OTHERR)
					itp = CCliTool.charToSQLType(chinn);
				sv = CCliTool.toBlurCond(sf, itp, sv, (cel.attr & Cell.USELEVEL) != 0);
				if (sv == null || sv.length() < 1) {
					if (notNull)
						throw new RuntimeException(cel.labelString + ":" + cl.IERR.ERR_NOTNULL);// ;-指定的条件项不能为空
					if (bne)
						throw new GOException(GOException.EXIT);
				}
				return sv;
			}
			if (c0 == '<' || c0 == '>')
				sv = CCliTool.getCondItem(sv, c0 == '<');// ;--取得第一项或最后一项。
			if (sv == null || sv.length() < 1) {
				if (bqc || notNull)
					throw new RuntimeException(cel.labelString + ":" + cl.IERR.ERR_NOTNULL);// ;-期初日期项不能为空或指定的条件项不能为空
				if (bne)
					throw new GOException(GOException.EXIT);
				return null;
			}
			if (bqc)
				return proc_qc(sf, sk, sv);// ;--期初单独处理。
		} else if (sv == null || sv.length() < 1) {
			if (bne)
				throw new GOException(GOException.EXIT);
			return null;
		}
		if (bDT) {
			if (bundiv)
				return sk == null ? sv : sf + " " + sk + " " + sv;// ;--直接值
			return sv == null || sv.length() < 1 ? null : CCliTool.sqlDate(sf, chinn == cl.ICL.INN_TIME, sk, sv);
		}
		if (chinn == cl.ICL.INN_STR)
			sv = CCliTool.sqlString(sv);
		return sk != null ? sf + sk + sv : sv;
	}

	/**
	 * @param sf
	 * @param sk
	 * @param sv
	 * @return
	 * 2019-07-29 12:27:16
	 */
	private String proc_qc(String sf, String sk, String sv) {
		System.out.println("QC");
		System.out.println(sv);
		return null;
	}

}
