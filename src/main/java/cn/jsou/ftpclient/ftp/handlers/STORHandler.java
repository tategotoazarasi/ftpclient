package cn.jsou.ftpclient.ftp.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class STORHandler implements ConnectionHandler {
	private static final Logger         logger = LogManager.getLogger(STORHandler.class);
	private final        CountDownLatch latch  = new CountDownLatch(1);
	private final        java.io.File   file;

	public STORHandler(java.io.File file) {
		this.file = file;
	}

	@Override public void handleConnection(Socket socket) {
		try (OutputStream outputStream = socket.getOutputStream();
		     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
		     FileInputStream fis = new FileInputStream(file)) {
			byte[] buffer = new byte[1 << 11]; // 创建一个缓冲区
			int    length;
			while ((length = fis.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length); // 将缓冲区的数据写入输出流
			}
			outputStream.flush(); // 确保所有数据都被写出
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
