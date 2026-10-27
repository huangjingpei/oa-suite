package org.jeecg.common.system.controller;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.BashUtil;
import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping("/s")
public class ShortLinkController {

	@Value(value = "${jeecg.path.upload}")
	private String uploadpath = "/root/jeecg/upFiles";

	@Value(value = "${jeecg.path.convert}")
	private String convert = "convert";

	/**
	 * 根据ShortURL跳转真实URL
	 * @param code
	 * @return
	 */
	@GetMapping(value = "/{code}")
	public Result<String> queryURLValid(@PathVariable("code")  String code , HttpServletRequest req) {

		log.info(code);

		return null;
	}


}
