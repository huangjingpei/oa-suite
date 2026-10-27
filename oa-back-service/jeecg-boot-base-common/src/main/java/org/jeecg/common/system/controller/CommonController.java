package org.jeecg.common.system.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.BashUtil;
import org.jeecg.common.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
@Slf4j
@RestController
@RequestMapping("/sys/common")
public class CommonController {

	@Value(value = "${jeecg.path.upload}")
	private String uploadpath = "/root/jeecg/upFiles";

	@Value(value = "${jeecg.path.convert}")
	private String convert = "convert";

	@Value(value = "${jeecg.redis.cachetime}")
	private long cacheTime =  60 ;

	@Autowired
	@Lazy
	private ISysBaseAPI sysBaseAPI;

	@Autowired
	private RedisUtil redisUtil;

	/**
	 * @Author 政辉
	 * @return
	 */
	@GetMapping("/403")
	public Result<?> noauth()  {
		return Result.error("没有权限，请联系管理员授权");
	}

	/**
	 * 查询URL是否存在
	 * @param path
	 * @return
	 */
	@GetMapping(value = "/url")
	public Result<String> queryURLValid(@RequestParam(name = "path", defaultValue = "/") String path , HttpServletRequest req) {

		//定义返回结果
		Result<String> result = new Result<>();

		path = URLDecoder.decode(path);
		path = path.substring(path.indexOf("/files/")+1);

		log.info("url:" + path);

		File file = new File(uploadpath + File.separator + path);

		result.setSuccess(file.exists());
		result.setResult(file.exists()?"true":"false");

		return result;
	}

	/**
	 * 查询Redis中Hash类型的值为code的缓存
	 * @param token
	 * @return
	 */
	@GetMapping(value = "/token/{token}")
	public JSONObject queryToken(@PathVariable("token")  String token , HttpServletRequest req) {

		// 解密获得username，用于和数据库进行对比
		String username = JwtUtil.getUsername(token);

		// 获取加密Key
		String key = CommonConstant.SECRET_USER_TOKEN + ":" + username + ":" + token;

		// 获取加密秘钥
		String secret = (String) redisUtil.get(key);

		//获取JSON信息
		JSONObject json = new JSONObject();

		//获取登录用户信息
		LoginUser user = sysBaseAPI.getUserByName(username);

		//如果没有获取到用户，则token失效；否则签名成功
		if (username == null) {
			json.put("status" , false);
			json.put("result" , "token invalid");
		} else {

			// 获取签名结果
			//String code = JwtUtil.sign(username , secret);

			//刷新加密秘钥时间
			redisUtil.expire(CommonConstant.SECRET_USER_TOKEN + ":" + username + ":" + token , JwtUtil.EXPIRE_TIME);

			//刷新操作时间
			redisUtil.expire(CommonConstant.PREFIX_USER_SHIRO_CACHE + user.getId() , JwtUtil.EXPIRE_TIME);
			redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token , JwtUtil.EXPIRE_TIME);
			redisUtil.expire(CacheConstant.SYS_USERS_CACHE + "::" + username ,  JwtUtil.EXPIRE_TIME);

			json.put("status" , true);
			json.put("result" , "success");
		}

		log.info("check token over , username : " + username + " userid: " + user.getId() +  " secret : " + secret + " request token :" + token +  " code : " + json.toJSONString());

		return json;
	}

	/**
	 * 查询Redis中Hash类型的值为code的缓存
	 * @param code
	 * @return
	 */
	@GetMapping(value = "/cache/{code}")
	public JSONObject queryCache(@PathVariable("code")  String code , @RequestParam("token")  String token  , HttpServletRequest req) {

		//定义JSON字符串
		JSONObject json = new JSONObject();

		//解密获得username，用于和数据库进行对比
		String username = JwtUtil.getUsername(token);

		//如果没有获取到用户，则token失效；否则签名成功
		if (username == null ) {
			json.put("status" , false);
			json.put("value" , "token invalid");
			return json;
		}

		try{
			Object obj = redisUtil.get("rest:cache:key:" + username + ":" + code );

			json.put("status" , true);
			json.put("value" , obj);

			log.info("/cache/" + code + " result : " + json.toJSONString());
		} catch (Exception e){
			log.error(e.getMessage());
		}

		return json;
	}

	/**
	 * 新增或者修改Redis中Hash类型的值为code的缓存
	 * @param code
	 * @return
	 */
	@PostMapping(value = "/cache/{code}")
	public JSONObject postCache(@PathVariable("code")  String code , @RequestParam(name="value") String value , @RequestParam("token")  String token , @RequestParam("second")  Long second  , HttpServletRequest req) {

		//定义JSON字符串
		JSONObject json = new JSONObject();

		//解密获得username，用于和数据库进行对比
		String username = JwtUtil.getUsername(token);

		LoginUser loginUser = sysBaseAPI.getUserByName(username);

		//如果没有传入second,则使用默认缓存时间
		if(second == null || second == 0){
			second = cacheTime;
		}

		//如果没有获取到用户，则token失效；否则签名成功
		if (username == null ) {
			json.put("status" , false);
			json.put("value" , "token invalid");
			return json;
		}

		try{

			Object obj = redisUtil.set("rest:cache:key:" + username + ":" + code  , value , second);

			json.put("status" , true);
			json.put("value" , obj);

			log.info("/cache/" + code + " result : " + json.toJSONString());
		} catch (Exception e){
			log.error(e.getMessage());
		}

		return json;
	}

	/**
	 * 查询Redis中Hash类型的值为code的缓存
	 * @param code
	 * @return
	 */
	@GetMapping(value = "/recache/{code}")
	public JSONObject queryCache(@PathVariable("code")  String code , HttpServletRequest req) {

		//定义JSON字符串
		JSONObject json = new JSONObject();

		try{
			Object obj = redisUtil.get("rest:cache:key:" + code );

			json.put("status" , true);
			json.put("value" , obj);

			log.info("/cache/" + code + " result : " + json.toJSONString());
		} catch (Exception e){
			log.error(e.getMessage());
		}

		return json;
	}

	/**
	 * 新增或者修改Redis中Hash类型的值为code的缓存
	 * @param code
	 * @return
	 */
	@PostMapping(value = "/recache/{code}")
	public JSONObject postCache(@PathVariable("code")  String code , @RequestParam(name="value") String value , @RequestParam("second")  Long second , HttpServletRequest req) {

		//定义JSON字符串
		JSONObject json = new JSONObject();

		//如果没有传入second,则使用默认缓存时间
		if(second == null || second == 0){
			second = cacheTime;
		}

		try{
			Object obj = redisUtil.set("rest:cache:key:" + code , value , second);

			json.put("status" , true);
			json.put("value" , obj);

			log.info("/cache/" + code + " result : " + json.toJSONString());
		} catch (Exception e){
			log.error(e.getMessage());
		}

		return json;
	}

	/**
	 * @function 上传函数
	 * @param request
	 * @param response
	 * @return
	 */
	@PostMapping(value = "/upload")
	public Result<?> upload(HttpServletRequest request, HttpServletResponse response) {

		//定义返回结果
		Result<?> result = new Result<>();

		//定义上传区域块
		try {

			//获取上传目录
			String ctxPath = uploadpath;
			//定义文件名称
			String fileName = null;
			//定义图片名称
			String thumborName = null;
            //设置图片名称
            String thumborName100x60 =null;
            //定义图片名称
            String originName = null;
            //定义上传文件子目录
			String bizPath = "files";
			//获取当前时间
			String nowtime = new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date());
			//获取文件上传目录
			File file = new File(ctxPath + File.separator + bizPath);

			//如果文件不存在，则创建文件根目录
			if (!file.exists()) {
				file.mkdirs();
			}

			//获取多媒体Request请求对象
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

			// 获取上传文件对象
			MultipartFile mf = multipartRequest.getFile("file");

			//获取文件名
			String orgName = mf.getOriginalFilename();
			//获取文件类型
			String fileType = orgName.substring(orgName.lastIndexOf(".")).toLowerCase();
			//临时名称
			String tempName = orgName.substring(0, orgName.lastIndexOf("."));

			//临时名称去掉标点
			try{
				tempName = tempName.replaceAll("([-+*/^()（）\\]\\[\"|/\\\\<>?？&$￥%%%@;.。 ，、；：:：；“”‘’\t\n])","")
						.replaceAll("[\\pP\\p{Punct}]","")
						.replaceAll("\\pP|\\pS|\\pC|\\pN|\\pZ", "");
			} catch (Exception e){
				log.error("上传附件名称去除特殊符号异常",e);
			}

			//重命名文件
			fileName = nowtime + "_" + tempName  + fileType;

			//设置图片名称
			thumborName = nowtime  + "_" + tempName + "_S240x160" + fileType;

            //设置图片名称
            thumborName100x60 = nowtime  + "_" + tempName + "_S100x60" + fileType;

            //设置图片名称
            originName = nowtime  + "_" + tempName + fileType;

			//设置保存路径
			String savePath = file.getPath() + File.separator + fileName;

			//设置可以执行保存的文件对象
			File savefile = new File(savePath);

			//执行保存文件操作
			FileCopyUtils.copy(mf.getBytes(), savefile);

			//设置应显示的文件保存路径
			String dbpath = bizPath + File.separator + fileName;

			//字符串\\转换为/
			if (dbpath.contains("\\")) {
				dbpath = dbpath.replace("\\", "/");
			}

			Integer fileSize = new BigDecimal((float)savefile.length()/1000).intValue() ;

			//定义待执行命令
			String command = null;

			//如果是Excel文档，则转化为Html,如果是Word文档，则转化为PDF文档，如果是PPT文档，则转化为PDF文档，如果是图片，则进行压缩处理
			if(fileType.equals(".xlsx") || fileType.equals(".xls")) {
				//如果是Excel文档，则转化为Html
				command = "libreoffice --convert-to html {src} --outdir {des}";
			} else if(fileType.equals(".doc")) {
				//老版本的Word可能转换异常，使用unoconv转换
				command = "unoconv -f pdf {src} -o {des} ";
			} else if(fileType.equals(".ppt")) {
				//老版本的Word可能转换异常，使用unoconv转换
				command = "unoconv -f pdf {src} -o {des} ";
			} else if(fileType.equals(".docx")){
			    //如果是Word文档，则转化为PDF
				command = "libreoffice --convert-to pdf:writer_pdf_Export {src} --outdir {des}";
			} else if(fileType.equals(".pptx")){
			    //如果是PPT文档，则转化为PDF
				command = "libreoffice --convert-to pdf:writer_pdf_Export {src} --outdir {des}";
			} else if(fileType.equals(".pdf")){
				//如果是PDF文档，则转化为PDF
				command = "ln -d {src} {des} ";
			} else if(fileType.equals(".jpeg") || fileType.equals(".jpg") || fileType.equals(".gif") || fileType.equals(".png") || fileType.equals(".bmp")){

				//默认压缩比率
				String ratio = "90";
				//如果文件大于500KB，则压缩
				if(fileSize>300 && fileSize <= 500){
					ratio = "85";
				} else if(fileSize>500 && fileSize <= 800){
					ratio = "80";
				} else if(fileSize>800){
					ratio = "75";
				}

				//定义图片压缩脚本 压缩率85%
				command = "convert -strip -interlace Plane -gaussian-blur 0.025 -quality {ratio}% {src} {src}"
						.replace("{ratio}" , ratio)
						.replace("{src}",ctxPath + File.separator + bizPath + File.separator + fileName);
				//执行图片压缩命令
				BashUtil.exec(command);

				//定义图片压缩脚本 规格240x160 //
				command = "convert {src} -resize 240x160 {des} "
						.replace("{src}",ctxPath + File.separator + bizPath + File.separator + fileName)
						.replace("{des}", ctxPath + File.separator + bizPath + File.separator + "images" + File.separator + thumborName);
				//执行图片压缩命令
				BashUtil.exec(command);

				//定义图片压缩脚本 规格100x60
				command = "convert {src} -resize 100x60 {des} "
						.replace("{src}",ctxPath + File.separator + bizPath + File.separator + fileName)
						.replace("{des}", ctxPath + File.separator + bizPath + File.separator + "images" + File.separator + thumborName100x60);
				//执行图片压缩命令
				BashUtil.exec(command);

				//定义图片压缩脚本 规格1024x768
				command = "convert {src} -resize 1024x768 {des} "
						.replace("{src}",ctxPath + File.separator + bizPath + File.separator + fileName)
						.replace("{des}", ctxPath + File.separator + bizPath + File.separator + "origin" + File.separator + originName);
				//执行图片压缩命令
				BashUtil.exec(command);

				//执行图片裁剪脚本
				BashUtil.exec("bash /root/jeecg/upFiles/files/thumbor.sh");
			} else {
				command = "";
			}

			//执行shell命令区域
			try{
				//设置命令中的src des
				command = command.replace("{src}",ctxPath + File.separator + bizPath + File.separator + fileName);
				command = command.replace("{des}", ctxPath + File.separator + bizPath + File.separator + convert + File.separator);

				//打印日志
				log.info("command :" + command + " fileSize:" + fileSize +  " fileType:" + fileType + " ctxPath:" + ctxPath + " bizPath:" + bizPath + " fileName:" + fileName + " convert:" + convert) ;

				//先设置文档可读
				BashUtil.exec("chmod 755 -R " + ctxPath + File.separator + bizPath + File.separator + "*");

				//执行文档转换命令
				BashUtil.exec(command);

                //执行Git Push命令，将本次变动实时同步到远程Git仓库中
                BashUtil.exec("bash /root/jeecg/upFiles/files/start.sh");

			} catch (Exception e){
				log.error(e.getMessage(), e);
			}

			//设置返回消息
			result.setMessage(dbpath);

			//设置返回状态
			result.setSuccess(true);

		} catch (IOException e) {

			result.setSuccess(false);
			result.setMessage("保存文件异常");
			log.error(e.getMessage(), e);

		}

		//返回结果
		return result;

	}

	/**
	 * 预览图片
	 * 请求地址：http://localhost:8080/common/view/{user/20190119/e1fe9925bc315c60addea1b98eb1cb1349547719_1547866868179.jpg}
	 *
	 * @param request
	 * @param response
	 */
	@GetMapping(value = "/view/**")
	public void view(HttpServletRequest request, HttpServletResponse response) {
		// ISO-8859-1 ==> UTF-8 进行编码转换
		String imgPath = extractPathFromPattern(request);
		// 其余处理略
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			imgPath = imgPath.replace("..", "");
			if (imgPath.endsWith(",")) {
				imgPath = imgPath.substring(0, imgPath.length() - 1);
			}
			response.setContentType("image/jpeg;charset=utf-8");
			String localPath = uploadpath;
			String imgurl = localPath + File.separator + imgPath;
			inputStream = new BufferedInputStream(new FileInputStream(imgurl));
			outputStream = response.getOutputStream();
			byte[] buf = new byte[1024];
			int len;
			while ((len = inputStream.read(buf)) > 0) {
				outputStream.write(buf, 0, len);
			}
			response.flushBuffer();
		} catch (IOException e) {
			log.error("预览图片失败" + e.getMessage());
			// e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}

	}

	/**
	 * 下载文件
	 * 请求地址：http://localhost:8080/common/download/{user/20190119/e1fe9925bc315c60addea1b98eb1cb1349547719_1547866868179.jpg}
	 *
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@GetMapping(value = "/download/**")
	public void download(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// ISO-8859-1 ==> UTF-8 进行编码转换
		String filePath = extractPathFromPattern(request);
		// 其余处理略
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			filePath = filePath.replace("..", "");
			if (filePath.endsWith(",")) {
				filePath = filePath.substring(0, filePath.length() - 1);
			}
			String localPath = uploadpath;
			String downloadFilePath = localPath + File.separator + filePath;
			File file = new File(downloadFilePath);
	         if (file.exists()) {
	        	 response.setContentType("application/force-download");// 设置强制下载不打开            
	 			response.addHeader("Content-Disposition", "attachment;fileName=" + new String(file.getName().getBytes("UTF-8"),"iso-8859-1"));
	 			inputStream = new BufferedInputStream(new FileInputStream(file));
	 			outputStream = response.getOutputStream();
	 			byte[] buf = new byte[1024];
	 			int len;
	 			while ((len = inputStream.read(buf)) > 0) {
	 				outputStream.write(buf, 0, len);
	 			}
	 			response.flushBuffer();
	         }

		} catch (Exception e) {
			log.info("文件下载失败" + e.getMessage());
			// e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	/**
	 * @功能：pdf预览Iframe
	 * @param modelAndView
	 * @return
	 */
	@RequestMapping("/pdf/pdfPreviewIframe")
	public ModelAndView pdfPreviewIframe(ModelAndView modelAndView) {
		modelAndView.setViewName("pdfPreviewIframe");
		return modelAndView;
	}

	/**
	  *  把指定URL后的字符串全部截断当成参数
	  *  这么做是为了防止URL中包含中文或者特殊字符（/等）时，匹配不了的问题
	 * @param request
	 * @return
	 */
	private static String extractPathFromPattern(final HttpServletRequest request) {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		return new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);
	}

	public static void main(String[] args) {

		String path = "https%3A%2F%2Fwww.shengtai.club%2Ffiles%2Fconvert%2F20200107030626476_蓝光生态集团公文管理培训.pdf";

		path = URLDecoder.decode(path);

		path = path.substring(path.indexOf("/files/")+1);

		log.info("url:" + path);
	}

}
