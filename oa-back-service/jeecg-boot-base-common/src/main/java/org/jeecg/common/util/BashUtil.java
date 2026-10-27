package org.jeecg.common.util;

import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * Bash命令行执行器
 * A001
 * A001A002
 * @Author zhangdaihao
 *
 */
@Slf4j
public class BashUtil {

	/**
	 * @function 执行Shell脚本命令
	 * @param command
	 * @return
	 */
	public static boolean exec(String command) {

		//如果传入参数为空，则直接返回
		if(command.equals("") || command == null){
			return false;
		}

		try {
			//打印所执行命令
			log.info("exec cmd : " + command);

			// exec()方法指示Java虚拟机创建一个子进程执行指定的可执行程序，并返回与该子进程对应的Process对象实例。
			Runtime.getRuntime().exec(command);//

		} catch (Exception e) {
			log.error(" exec cmd error :" + command, e);
			return false;
		}

		//返回执行结果
		return true;
	}

}
