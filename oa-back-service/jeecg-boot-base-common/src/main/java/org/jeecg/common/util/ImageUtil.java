package org.jeecg.common.util;

import lombok.extern.slf4j.Slf4j;


import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.*;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

import java.math.BigDecimal;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;

/**
 * 图片压缩执行器
 * A001
 * A001A002
 * @Author zhangdaihao
 *
 */
@Slf4j
public class ImageUtil {

	/**
	 * @function 图片压缩算法
	 * @param srcFilePath
	 * @param descFilePath
	 * @return
	 */
	public static boolean compress(String srcFilePath, String descFilePath) {
		File file = null;
		BufferedImage src = null;
		FileOutputStream out = null;
		ImageWriter imgWrier;
		ImageWriteParam imgWriteParams;

		// 指定写图片的方式为 jpg
		imgWrier = ImageIO.getImageWritersByFormatName("jpg").next();
		imgWriteParams = new JPEGImageWriteParam(null);
		// 要使用压缩，必须指定压缩方式为MODE_EXPLICIT
		imgWriteParams.setCompressionMode(imgWriteParams.MODE_EXPLICIT);
		// 这里指定压缩的程度，参数qality是取值0~1范围内，
		imgWriteParams.setCompressionQuality((float) 0.8);
		imgWriteParams.setProgressiveMode(imgWriteParams.MODE_DISABLED);
		ColorModel colorModel = ColorModel.getRGBdefault();
		// 指定压缩时使用的色彩模式
		imgWriteParams.setDestinationType(
				new javax.imageio.ImageTypeSpecifier(colorModel, colorModel.createCompatibleSampleModel(16, 16)));

		try {
			if (srcFilePath == null || srcFilePath.trim().length() == 0) {
				return false;
			} else {
				file = new File(srcFilePath);
				src = ImageIO.read(file);
				out = new FileOutputStream(descFilePath);

				imgWrier.reset();
				// 必须先指定 out值，才能调用write方法, ImageOutputStream可以通过任何
				// OutputStream构造
				imgWrier.setOutput(ImageIO.createImageOutputStream(out));
				// 调用write方法，就可以向输入流写图片
				imgWrier.write(null, new IIOImage(src, null, null), imgWriteParams);
				out.flush();
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 根据指定大小和指定精度压缩图片
	 *
	 * @param srcPath
	 *            源图片地址
	 * @param desPath
	 *            目标图片地址
	 * @param desFileSize
	 *            指定图片大小，单位kb
	 * @param accuracy
	 *            精度，递归压缩的比率，建议小于0.9
	 *  @param desMaxWidth
	 *             目标最大宽度
	 *  @param desMaxHeight
	 *             目标最大高度
	 * @return 目标文件路径
	 */
	public static String commpress(String srcPath, String desPath,
								   long desFileSize, double accuracy,
								   int desMaxWidth,int desMaxHeight) {
		if (StringUtils.isEmpty(srcPath) || StringUtils.isEmpty(srcPath)) {
			return null;
		}
		if (!new File(srcPath).exists()) {
			return null;
		}
		try {
			File srcFile = new File(srcPath);
			long srcFileSize = srcFile.length();
			System.out.println("源图片：" + srcPath + "，大小：" + srcFileSize / 1024
					+ "kb");
			//获取图片信息
			BufferedImage bim = ImageIO.read(srcFile);
			int srcWidth = bim.getWidth();
			int srcHeight = bim.getHeight();

			//先转换成jpg
			Thumbnails.Builder builder = Thumbnails.of(srcFile).outputFormat("jpg");

			// 指定大小（宽或高超出会才会被缩放）
			if(srcWidth > desMaxWidth || srcHeight > desMaxHeight) {
				builder.size(desMaxWidth, desMaxHeight);
			}else{
				//宽高均小，指定原大小
				builder.size(srcWidth,srcHeight);
			}

			// 写入到内存
			ByteArrayOutputStream baos = new ByteArrayOutputStream(); //字节输出流（写入到内存）
			builder.toOutputStream(baos);

			// 递归压缩，直到目标文件大小小于desFileSize
			byte[] bytes = commpressPicCycle(baos.toByteArray(), desFileSize, accuracy);

			// 输出到文件
			File desFile = new File(desPath);
			FileOutputStream fos = new FileOutputStream(desFile);

			try {
				fos.write(bytes);
				fos.close();
			} catch (Exception e){
				log.error("",e);
			}

			log.info("目标图片：" + desPath + "，大小" + desFile.length() / 1024 + "kb");
			log.info("图片压缩完成！");

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return desPath;
	}

	private static byte[] commpressPicCycle(byte[] bytes, long desFileSize, double accuracy) throws IOException {
		// File srcFileJPG = new File(desPath);
		long srcFileSizeJPG = bytes.length;
		// 2、判断大小，如果小于500kb，不压缩；如果大于等于500kb，压缩
		if (srcFileSizeJPG <= desFileSize * 1024) {
			return bytes;
		}
		// 计算宽高
		BufferedImage bim = ImageIO.read(new ByteArrayInputStream(bytes));
		int srcWdith = bim.getWidth();
		int srcHeigth = bim.getHeight();
		int desWidth = new BigDecimal(srcWdith).multiply(
				new BigDecimal(accuracy)).intValue();
		int desHeight = new BigDecimal(srcHeigth).multiply(
				new BigDecimal(accuracy)).intValue();

		ByteArrayOutputStream baos = new ByteArrayOutputStream(); //字节输出流（写入到内存）
		Thumbnails.of(new ByteArrayInputStream(bytes)).size(desWidth, desHeight).outputQuality(accuracy).toOutputStream(baos);
		return commpressPicCycle(baos.toByteArray(), desFileSize, accuracy);
	}

}
