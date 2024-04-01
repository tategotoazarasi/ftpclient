package cn.jsou.ftpclient.utils;

import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * 用于加载SVG图标的工具类
 */
public class SvgIconLoader {
	/**
	 * 从SVG文件加载图标
	 *
	 * @param path   SVG文件的路径
	 * @param height 图标的期望高度，宽度将按比例调整
	 *
	 * @return 加载的图标，如果加载失败则返回null
	 */
	public static Icon loadSvgIcon(String path, int height) {
		try {
			// 创建一个用于转换的BufferedImageTranscoder
			BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
			// 设置期望的高度
			transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, (float) height);
			// 读取SVG文件
			try (InputStream inputStream = SvgIconLoader.class.getResourceAsStream(path)) {
				TranscoderInput input = new TranscoderInput(inputStream);
				transcoder.transcode(input, null);
				BufferedImage image = transcoder.getBufferedImage();
				return new ImageIcon(image);
			}
		} catch (IOException | TranscoderException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 内部类，用于将SVG图像转码为BufferedImage
	 */
	private static class BufferedImageTranscoder extends ImageTranscoder {
		/**
		 * 转码后的图像
		 */
		private BufferedImage image;

		/**
		 * 创建图像缓冲区
		 *
		 * @param w 图像的宽度
		 * @param h 图像的高度
		 *
		 * @return 创建的图像缓冲区
		 */
		@Override
		public BufferedImage createImage(int w, int h) {
			return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		}

		/**
		 * 将转码后的图像写入图像缓冲区
		 *
		 * @param img 要写入的图像
		 * @param out 转码输出
		 */
		@Override
		public void writeImage(BufferedImage img, TranscoderOutput out) {
			this.image = img;
		}

		/**
		 * 获取转码后的图像
		 *
		 * @return 转码后的图像
		 */
		public BufferedImage getBufferedImage() {
			return image;
		}
	}
}
