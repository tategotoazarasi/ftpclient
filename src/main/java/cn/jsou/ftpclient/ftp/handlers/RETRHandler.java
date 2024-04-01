package cn.jsou.ftpclient.ftp.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * 实现ConnectionHandler接口，用于处理RETR命令的连接
 * <p>该类负责从FTP客户端接收文件</p>
 */
public class RETRHandler implements ConnectionHandler {
	private static final Logger         logger = LogManager.getLogger(RETRHandler.class);
	/**
	 * 用于同步等待处理完成的闭锁
	 */
	private final        CountDownLatch latch  = new CountDownLatch(1);
	/**
	 * 需要接收的文件
	 */
	private final        java.io.File   file;

	/**
	 * 构造函数
	 *
	 * @param file 接收文件时文件的存储位置
	 */
	public RETRHandler(java.io.File file) {
		this.file = file;
	}

	/**
	 * 处理传入的连接，从中读取数据并写入指定的文件中
	 *
	 * @param socket 传入连接的套接字。
	 */
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

	/**
	 * 等待所有连接的处理完成
	 *
	 * @throws InterruptedException 如果线程在等待时被中断
	 */
	@Override public void waitForCompletion() throws InterruptedException {
		latch.await(); // 等待处理完成
	}
}
