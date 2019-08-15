/**
 * 
 */
package inetbas.web.outsys.api;


/**
 * @author www.bip-soft.com
 *
 */
public class APIConst {
	//登录ID
	public static final String APIID_LOGIN="login";
	//登出ID
	public static final String APIID_LOGOUT="logout";
	//不用密码登录ID
	public static final String APIID_OUTLOGIN="outlogin";
	//单点登录需要秘钥
	public static final String APIID_SINGLELOGIN="inglelogin";

	// 获取菜单参数和cell总Id，
	public static final String APIID_MPARAMS="mparam";
	//获取cellID 
	public static final String APIID_CELLPARAMS="cellparams";

	//业务定义
	public static final String APIID_OPERATION = "buid";

	// 保存数据
	public static final String APIID_SAVEDATA="savedata";

	//访问数据的APIId
	public static final String APIID="apiId";
	//导出数据
	public static final String APIID_EXPDATA="expdata";
	
	public static final String APIID_TIME="systime";
	//查询数据ID
	public static final String APIID_FINDDATA="finddata";
	public static final String APIID_FINDCELLDATA="findcelldata";
	public static final String APIID_FINDSTATDATA="findstatdata";//获取统计数据
	public static final String APIID_BIPINSAID="bipinsaid";//
	
	
	//保存数据是提交的数据类型，分为json格式和cell格式
	public static final int CELLDATA = 0;//纯数据提交，和cell形成对应
	public static final int JSONDATA = 1;//保存Json格式数据提交
	
	public static final String APIID_WORKFLOW = "workflow";//工作流
	
	public static final String APIID_DLGSQLRUN = "dlgsqlrun" ;//弹出框执行sql 

	
	public static final String APIID_RPT = "rpt";//RPT
	
	//审批提交
	public static final String APIID_CHKUP = "chkup";
	
	//文件上传(2017-12-08添加)
	public static final String APIID_UPDWNFILE = "upf";
	public static final int AIDID_FILEUP = 33; //上传文件
	public static final int AIDID_FILEDEL= 34;//删除文件
	public static final int AIDID_FILEINFO = 35;//文件信息
	public static final int AIDID_FILEDOWN = 36;//下载文件列表
	
	//任务消息处理
	public static final String APIID_TA_MSG  = "taskmsg";//apiid
	public static final int APIID_TM_ALL  = 200;//获取任务和消息
	public static final int APIID_TM_TASK = 201;// 只获取未处理任务
	public static final int APIID_TM_MSG  = 202;//只获取未处理消息
	public static final int APIID_TM_TASK_UPD  = 203;//更新状态
	public static final int APIID_TM_MSG_DTL  = 212;//只获取未处理消息
	public static final int APIID_TM_MSG_UPD  = 213;//更新状态
	public static final int APIID_TM_RL  = 249;//重新加载RMQ配置信息
	
	
}
