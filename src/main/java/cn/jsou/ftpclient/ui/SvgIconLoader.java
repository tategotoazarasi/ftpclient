package cn.jsou.ftpclient.ui;

import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class SvgIconLoader {
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

	private static class BufferedImageTranscoder extends ImageTranscoder {
		private BufferedImage image;

		@Override
		public BufferedImage createImage(int w, int h) {
			return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		}

		@Override
		public void writeImage(BufferedImage img, TranscoderOutput out) {
			this.image = img;
		}

		public BufferedImage getBufferedImage() {
			return image;
		}
	}
}
