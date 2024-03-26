package cn.jsou.ftpclient.ftp.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class RETRHandler implements ConnectionHandler {
	private static final Logger         logger = LogManager.getLogger(RETRHandler.class);
	private final        CountDownLatch latch  = new CountDownLatch(1);
	private final        java.io.File   file;

	public RETRHandler(java.io.File file) {
		this.file = file;
	}

	@Override public void handleConnection(Socket socket) {
		try (InputStream inputStream = socket.getInputStream();
		     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		     FileOutputStream fos = new FileOutputStream(file, false)) {
			byte[] buffer = new byte[1 << 11]; // 创建一个缓冲区
			int    length;
			while ((length = inputStream.read(buffer)) > 0) {
				fos.write(buffer, 0, length); // 将缓冲区的数据写入输出流
			}
		} catch (IOException e) {
			logger.error("Error handling STOR data connection", e);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				logger.error("Error closing data connection socket", e);
			}
			latch.countDown(); // 处理完成，计数减1
		}
	}

	@Override public void waitForCompletion() throws InterruptedException {
		latch.await(); // 等待处理完成
	}
}
