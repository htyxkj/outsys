package inetbas.web.outsys.entity;

/**
 * @author www.bip-soft.com
 *
 */
public class WebCEAPars {
	public String sid="";//制单号码
	public String sbuid="";//业务码
	public String yjcontext="";//审批意见
	public int statefr;//来源状态
	public int stateto;//下一状态
	public String bup="0";// 0未审批 1审批通过 2驳回申请
	public String content="";//微信段内容
	public String tousr="";//下一个审批人Id
	public boolean ckd;

}
