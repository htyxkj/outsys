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
	//获取菜单参数ID并获取数据   暂时未用到
	public static final String APIID_MENUPARAM="menuparam";
	// 获取菜单参数和cell总Id，
	public static final String APIID_MPARAMS="mparam";
	//获取cellID 
	public static final String APIID_CELLPARAMS="cellparams";
	//获取cell并初始化数据
	public static final String APIID_CELLPARAM="cellparam";
	//业务定义
	public static final String APIID_OPERATION = "buid";
	//调用变量ID
	public static final String APIID_VAR="variable";
	// 保存数据
	public static final String APIID_SAVEDATA="savedata";
	//调用辅助ID
	public static final String APIID_AID="assist";
	//调用辅助ID
	public static final String APIID_AIDDATA="assistdata";
	//获取常量
	public static final String APIID_CONSTANT="constant";

	
	
	
	//访问数据的APIId
	public static final String APIID="apiId";

	public static final String APIID_TIME="systime";
 


	//调用辅助ID 
	public static final String APIID_AIDO="assisto";
	//调用辅助ID,返回值带Id和Message
	public static final String APIID_AID1="assist1";

	//查询数据ID
	public static final String APIID_FINDDATA="finddata";
	public static final String APIID_FINDCELLDATA="findcelldata";
	public static final String APIID_FINDSTATDATA="findstatdata";//获取统计数据
	public static final String APIID_BIPINSAID="bipinsaid";//获取统计数据
	
	//执行sql查询
	public static final String APIID_STS="sts";
	//mes更新状态
	public static final String APIID_MESST = "mesudp";
	
	//保存数据是提交的数据类型，分为json格式和cell格式
	
	public static final String APIID_DELDATA="deldata"; //删除数据
	public static final String APIID_EDITDATA="editdata";// 修改
	public static final int CELLDATA = 0;//纯数据提交，和cell形成对应
	public static final int JSONDATA = 1;//保存Json格式数据提交
	
	public static final String APIID_WORKFLOW = "workflow";//工作流
	public static final String APIID_DLG = "dlg";//对象弹出框按钮查询
	public static final String APIID_DLGA = "dlga";//对象弹出框按钮执行

	
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
	public static final int APIID_TM_TASK  = 201;//只获取未处理任务
//	public static final int APIID_TM_TASK_DTL  = 211;//任务明细
	public static final int APIID_TM_MSG  = 202;//只获取未处理消息
	public static final int APIID_TM_TASK_UPD  = 203;//更新状态
	public static final int APIID_TM_MSG_DTL  = 212;//只获取未处理消息
	public static final int APIID_TM_MSG_UPD  = 213;//更新状态
	public static final int APIID_TM_RL  = 249;//重新加载RMQ配置信息
	
	public static final String APIID_EXPDATA="expdata";
	
	
}
