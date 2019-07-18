/**
 * 
 */
package inetbas.web.outsys.api;

import inetbas.cli.cutil.CCliTool;
import inetbas.web.outsys.entity.ReturnObj;
import inetbas.web.outsys.entity.WebFileInfo;
import inetbas.web.outsys.tools.FileUtils;
import inetbas.web.outsys.tools.UserInfoSession;
import inetbas.webserv.SErvVars;
import inetbas.webserv.WSTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author www.bip-soft.com
 * 
 */
public class WebUPDFileService extends HttpServlet {

	public static final String UTF8 = "utf-8";
	public static final String TEMP = "/temp/";
	public static final int chuck = 2 * 1024 * 1024;
	private static Logger _log = LoggerFactory
			.getLogger(WebUPDFileService.class);

	public static Map<String, Map<String, String>> fileMap = new HashMap<String, Map<String, String>>();
	public static Map<String, String> fPATH = new HashMap<String, String>();
	public static Map<String, Boolean> iSupdMap = new HashMap<String, Boolean>();
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding(UTF8);
		response.setCharacterEncoding(UTF8);
		String sn = request.getParameter("snkey");
		int key = CCliTool.objToInt(request.getParameter("updid"), 0);
		if (key == APIConst.AIDID_FILEDOWN) {
			// 下载文件
			downFile(request, response, sn);
		}else{
			doPost(request, response);
		}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setHeader("Access-Control-Allow-Origin", "*");
		String sn = request.getParameter("snkey");
		HashMap<String, Object> hm = UserInfoSession.getUserInfo(sn);// 根据sn获取用户登录信息
		ReturnObj reoReturnObj = new ReturnObj();
		if (hm == null || hm.size() == 0) {
			reoReturnObj.makeFaile();
			reoReturnObj.setMessage("请登录系统以后再操作！");
			WebServiceAPI.WriteJsonString(response, reoReturnObj);
		} else {
			int key = CCliTool.objToInt(request.getParameter("updid"), 0);
			if (key == APIConst.AIDID_FILEUP) {
				// 上传文件
				uploadFile(request, response, sn);
			} else if (key == APIConst.AIDID_FILEDOWN) {
				// 下载文件
				downFile(request, response, sn);
			} else if (key == APIConst.AIDID_FILEINFO) {
				// 获取文件信息
				getFileInfo(request, response, sn);
			} else if (key == APIConst.AIDID_FILEDEL) {
				// 删除文件信息
				deleteFile(request, response, sn);
			} else {
				reoReturnObj.makeFaile();
				reoReturnObj.setMessage("what are you 闹啥嘞?");
				WebServiceAPI.WriteJsonString(response, reoReturnObj);
			}
		}
	}

	/**
	 * @param request
	 * @param response
	 * @param sn
	 */
	private void deleteFile(HttpServletRequest request,
			HttpServletResponse response, String sn) {
		ReturnObj reoReturnObj = new ReturnObj();
		HashMap<String, Object> hm = UserInfoSession.getUserInfo(sn);// 根据sn获取用户登录信息
		String dbid = CCliTool.objToString(hm.get(cl.ICL.DB_ID_T));
		String savePath = getFileDir(dbid, false);
//		String fjname = request.getParameter("fjname");// 附件名称;附件名称...
		String fjname = WebServiceAPI.decode(request.getParameter("fjname"));// 附件名称;附件名称...
		String fjroot = request.getParameter("fjroot");
		String file = savePath + "/" + fjroot + "/" + fjname;
		_log.info(file);
		File f = new File(file);
		if(f.exists()){
			f.delete();
			reoReturnObj.makeSuccess();
		}else{
			reoReturnObj.makeSuccess();
			reoReturnObj.setMessage("文件不存在！");
		}
		WebServiceAPI.WriteJsonString(response, reoReturnObj);
		
	}

	/**
	 * @param request
	 * @param response
	 * @param sn
	 */
	private void downFile(HttpServletRequest request,
			HttpServletResponse response, String sn) {
		HashMap<String, Object> hm = UserInfoSession.getUserInfo(sn);// 根据sn获取用户登录信息
		String dbid = CCliTool.objToString(hm.get(cl.ICL.DB_ID_T));
		String savePath = getFileDir(dbid, false);
		String fjname = WebServiceAPI.decode(request.getParameter("fjname"));// 附件名称;附件名称...
		String fjroot = request.getParameter("fjroot");
		String file = savePath + "" + fjroot + "/" + fjname;
		_log.info(file);
		File f = new File(file);
		try {
			if (f.exists()) {
				_log.info("开始完成");
				FileInputStream fis = new FileInputStream(f);
				byte[] b = new byte[1024];
				response.setCharacterEncoding(UTF8);
				
				String file_name = new String(f.getName().getBytes(), UTF8);
				response.setHeader("Content-Disposition",
						String.format("attachment; filename=\"%s\"", file_name));
				response.setContentType("application/x-msdownload;charset=utf-8");
				_log.info(file_name);
				// 获取响应报文输出流对象
				ServletOutputStream out = response.getOutputStream();
				// 输出
				int n = 0;
				while ((n = fis.read(b)) != -1) {
					out.write(b, 0, n);
				}
				fis.close();
				out.flush();
				out.close();
			}
			_log.info("下载完成");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取文件信息， 从reqest中获取相关信息
	 * 
	 * @param request
	 * @param response
	 */
	private void getFileInfo(HttpServletRequest request,
			HttpServletResponse response, String sn) {
		ReturnObj reoReturnObj = new ReturnObj();
		HashMap<String, Object> hm = UserInfoSession.getUserInfo(sn);// 根据sn获取用户登录信息
		String dbid = CCliTool.objToString(hm.get(cl.ICL.DB_ID_T));
		String savePath = getFileDir(dbid, false);
		String fjname = request.getParameter("fjname");// 附件名称;附件名称...
		fjname = decode8859_1(fjname);// 附件名称;附件名称...
//		fjname = WebServiceAPI.decode(fjname);// 附件名称;附件名称...
		String fjroot = request.getParameter("fjroot");
		String[] fjnames = fjname.split(";");
		ArrayList<WebFileInfo> files = new ArrayList<WebFileInfo>();
		for (int i = 0; i < fjnames.length; i++) {
			String name = fjnames[i];
			String file = savePath + "/" + fjroot + "/" + name;
			_log.info(file);
			File f = new File(file);
			if (f.exists()) {
				WebFileInfo ff = new WebFileInfo();
				ff.setName(f.getName());
				ff.setSize(f.length());
				files.add(ff);
			} else {
				WebFileInfo ff = new WebFileInfo();
				ff.setName("文件不存在！！");
				ff.setSize(0);
				files.add(ff);
			}
		}
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("files", files);
		reoReturnObj.setData(data);
		reoReturnObj.makeSuccess();
		WebServiceAPI.WriteJsonString(response, reoReturnObj);

	}

	/***
	 * 上传文件！
	 * 
	 * @param request
	 * @param response
	 * @param sn
	 */
	private void uploadFile(HttpServletRequest request,
			HttpServletResponse response, String sn) {
		ReturnObj reoReturnObj = new ReturnObj();
		// String sn = request.getParameter("snkey");
		String fjkey = request.getParameter("fjkey");
		HashMap<String, Object> hm = UserInfoSession.getUserInfo(sn);// 根据sn获取用户登录信息
		String dbid = CCliTool.objToString(hm.get(cl.ICL.DB_ID_T));
		String mapKey = "", fid = "";
		String savePath = getFileDir(dbid, false);
		String tempPath = getFileDir(dbid, true);
		File saveF = new File(savePath);
		File temp1 = new File(tempPath);
		if (!saveF.exists()) {
			// 创建临时目录
			saveF.mkdir();
		}
		if (!temp1.exists()) {
			// 创建临时目录
			temp1.mkdir();
		}
		String fname = WebServiceAPI.decode(request.getParameter("name"));
		String total = "", index = "";
		Map<String, String> fMap = new HashMap<String, String>();
		String filename = "", filPath = "", realSavePath = "",fjroot="";
		try {
			// 使用Apache文件上传组件处理文件上传步骤：
			// 1、创建一个DiskFileItemFactory工厂
			DiskFileItemFactory factory = new DiskFileItemFactory();
			// 设置工厂的缓冲区的大小，当上传的文件大小超过缓冲区的大小时，就会生成一个临时文件存放到指定的临时目录当中。
			factory.setSizeThreshold(1024 * 100);// 设置缓冲区的大小为100KB，如果不指定，那么缓冲区的大小默认是10KB
			// 设置上传时生成的临时文件的保存目录
			factory.setRepository(temp1);
			// 2、创建一个文件上传解析器
			ServletFileUpload upload = new ServletFileUpload(factory);
			// 监听文件上传进度
			upload.setProgressListener(new ProgressListener() {
				public void update(long pBytesRead, long pContentLength,
						int arg2) {
					// System.out.println("文件大小为：" + pContentLength + ",当前已处理："
					// + pBytesRead);
				}
			});
			// 解决上传文件名的中文乱码
			upload.setHeaderEncoding(UTF8);

			String fkey = WebServiceAPI.decode(request.getParameter("fkey"));
			fid = WebServiceAPI.decode(request.getParameter("fid"));
			mapKey = sn + "." + fkey;
			if (!iSupdMap.containsKey(mapKey)) {
				iSupdMap.put(mapKey, false);
			}
			synchronized (mapKey) {
				fMap = getSyncMap(mapKey);
				// 设置上传单个文件的大小的最大值，目前是设置为1024*1024字节，也就是1MB
				upload.setFileSizeMax(1024 * 1024 * 5);
				// 设置上传文件总量的最大值，最大值=同时上传的多个文件的大小的最大值的和，目前设置为30MB
				upload.setSizeMax(1024 * 1024 * 30);
				List<FileItem> list = upload.parseRequest(request);
				for (FileItem item : list) {
					// 如果fileitem中封装的是普通输入项的数据
					if (item.isFormField()) {
						String name = item.getFieldName();
						String value = item.getString(UTF8);
						if (name.equals("index")) {
							index = value;
						}
						if (name.equals("total")) {
							total = value;
						}
						if (name.equals("fjroot")) {
							fjroot = value;
							if(fjroot!=null){
								if(fjroot.length()>0){
									fPATH.put(mapKey, fjroot);
								}
							}
						}
					} else {
						filename = item.getName();
						if (filename.equals("blob")) {
							filename = fname;
						}
						if (filename == null || filename.trim().equals("")) {
							continue;
						}
						// 注意：不同的浏览器提交的文件名是不一样的，有些浏览器提交上来的文件名是带有路径的，如：
						// c:\a\b\1.txt，而有些只是单纯的文件名，如：1.txt
						// 处理获取到的上传文件的文件名的路径部分，只保留文件名部分
						filename = filename
								.substring(filename.lastIndexOf("/") + 1);
						// 得到上传文件的扩展名
						String fileExtName = filename.substring(filename
								.lastIndexOf(".") + 1);
						// 如果需要限制上传的文件类型，那么可以通过文件的扩展名来判断上传的文件类型是否合法
						// System.out.println("上传的文件的扩展名是："+fileExtName);
						// 获取item中的上传文件的输入流
						InputStream in = item.getInputStream();
						boolean ispath = fPATH.containsKey(mapKey);
						filPath = ispath ? fPATH.get(mapKey) : makePath(fjkey,
								savePath);
						if (!ispath)
							fPATH.put(mapKey, filPath);
						realSavePath = savePath + filPath;
						File file = new File(realSavePath + TEMP);
						// 如果目录不存在
						if (!file.exists()) {
							// 创建目录
							file.mkdirs();
						}
						// 创建一个文件输出流
						String saveFilename = filename.substring(0,
								filename.lastIndexOf("."))
								+ "_" + index + "." + fileExtName;
						String fullName = realSavePath + TEMP + saveFilename;
						File f = new File(fullName);
						if (f.exists())
							fullName = WSTool.newFileName(fullName);
						FileOutputStream out = null;
						if (index.equals("0"))
							out = new FileOutputStream(fullName, false);
						else {
							out = new FileOutputStream(fullName, true);
						}
						// 创建一个缓冲区
						byte buffer[] = new byte[1024];
						// 判断输入流中的数据是否已经读完的标识
						int len = 0;
						// 循环将输入流读入到缓冲区当中，(len=in.read(buffer))>0就表示in里面还有数据
						while ((len = in.read(buffer)) > 0) {
							// 使用FileOutputStream输出流将缓冲区的数据写入到指定的目录(savePath +
							// "\\"
							// + filename)当中
							out.write(buffer, 0, len);
						}
						// 关闭输入流
						in.close();
						// 关闭输出流
						out.close();
						// 删除处理文件上传时生成的临时文件
						item.delete();
						fMap.put(index, fullName);
						// _log.info(JSONObject.toJSONString(fMap));
						HashMap<String, Object> data = new HashMap<String, Object>();
						Number n1 = CCliTool.calcTwoNumber(fileMap.get(mapKey)
								.size(), total, '/', 2);
						n1 = CCliTool.calcTwoNumber(n1, 100.00, '*', 2);
						data.put("pros", n1.doubleValue());
						data.put("fid", CCliTool.objToInt(fid, 0));
						reoReturnObj.setData(data);
						reoReturnObj.makeSuccess("第"
								+ (CCliTool.objToInt(index, 0) + 1)
								+ "个数据块上传成功！");
						reoReturnObj.setId(1);
						_log.info("第" + index + "个数据块上传成功！");
						// reoReturnObj.makeSuccess("上传成功！");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			_log.info("出错了：",e);
			fPATH.remove(mapKey);
			fileMap.remove(mapKey);
			fMap.clear();
			reoReturnObj.makeFaile(CCliTool.traceException0(e));
		} finally {
			_log.info("fMap,99999");
			String fsize = (fMap.size()) + "";
			if (total.equals((fsize)) && (iSupdMap.containsKey(mapKey))
					&& !iSupdMap.get(mapKey)) {
				iSupdMap.put(mapKey, true);
				String merF = realSavePath + "/" + filename;
				File f = new File(merF);
				if (f.exists())
					merF = WSTool.newFileName(merF);
				boolean isok = false;
				try {
					isok = FileUtils.mergeFile(merF, fMap, true);
				} catch (IOException e) {
					e.printStackTrace();
					reoReturnObj.makeFaile(CCliTool.traceException0(e));
				}
				if (isok) {
					HashMap<String, Object> data = new HashMap<String, Object>();
					data.put("fname", getFileName(merF));
					data.put("fj_root", filPath);
					data.put("pros", 100);
					data.put("fid", CCliTool.objToInt(fid, 0));
					reoReturnObj.setData(data);
					reoReturnObj.makeSuccess("上传成功！");
					fileMap.remove(mapKey);
					iSupdMap.remove(mapKey);
					fPATH.remove(mapKey);
					// File f1 = new File(realSavePath+TEMP);
					// if(f1.exists())
					// f1.delete();
					fMap.clear();
					_log.info("OK");
				}
			}
			WebServiceAPI.WriteJsonString(response, reoReturnObj);
		}
	}

	public static synchronized Map<String, String> getSyncMap(String mapKey) {
		Map<String, String> fMap = fileMap.get(mapKey);
		if (fMap == null) {
			fMap = new HashMap<String, String>();
			fileMap.put(mapKey, fMap);
		}
		return fMap;
	}

	/**
	 * @Method: makeFileName
	 * @Description: 生成上传文件的文件名，文件名以：uuid+"_"+文件的原始名称
	 * @param filename
	 *            文件的原始名称
	 * @return uuid+"_"+文件的原始名称
	 */
	public static String makeFileName(String filename) { // 2.jpg
		// 为防止文件覆盖的现象发生，要为上传文件产生一个唯一的文件名
		return UUID.randomUUID().toString() + "_" + filename;
	}

	public static String getFileName(String fullname) {
		return fullname.substring(fullname.lastIndexOf("/") + 1);
	}

	/***
	 * 获取文件路径
	 * 
	 * @param dbid
	 * @param btemp
	 * @return
	 */
	public static String getFileDir(String dbid, boolean btemp) {
		String savePath = SErvVars.DOCDIR;
		savePath = savePath == null ? "" : savePath;
		if (savePath.length() < 1)
			savePath = SErvVars.ROOTDIR;
		if (btemp) {
			savePath += TEMP;
			savePath = CCliTool.todir(savePath);
			return savePath;
		} else {
			if (dbid != null)
				savePath += cl.ICL.PRE_SYSID + dbid;
			savePath = CCliTool.todir(savePath);
			return savePath;
		}
	}

	/**
	 * 为防止一个目录下面出现太多文件，要使用hash算法打散存储
	 * 
	 * @Method: makePath
	 * @param filename
	 *            文件名，要根据文件名生成存储目录
	 * @param savePath
	 *            文件存储路径
	 * @return 新的存储目录
	 */
	public static String makePath(String fj_root, String savePath) {
		// 得到文件名的hashCode的值，得到的就是filename这个字符串对象在内存中的地址
		// int hashcode = filename.hashCode();
		// int dir1 = hashcode&0xf; //0--15
		// int dir2 = (hashcode&0xf0)>>4; //0-15
		String dir1 = cl.ICL.PRE_FJ + fj_root;
		String dir2 = getYMDDIR();
		// 构造新的保存目录
		String dirString = dir1 + "/" + dir2;
		String dir = savePath + "/" + dirString; // upload\2\3 upload\3\5
		// File既可以代表文件也可以代表目录
		File file = new File(dir);
		// 如果目录不存在
		if (!file.exists()) {
			// 创建目录
			file.mkdirs();
		}
		return dirString;
	}

	public static String getYMDDIR() {
		Calendar calendar = Calendar.getInstance();
		return "Y" + CCliTool.dateToString(calendar, false, 1);
	}
	
	public static String getNows(){
		return Calendar.getInstance().getTimeInMillis()+"";
	}
	
	public static String decode8859_1(String s0) {
		 //;--处理如TOMCAT5是以8859_1方式硬编码时，还原成系统传输的UTF-8编码。
		 try {
		  return new String(s0.getBytes("8859_1"), cl.INN.UTF_8);
		 } catch (Exception err) {
		 }
		 return s0;
		}

}
