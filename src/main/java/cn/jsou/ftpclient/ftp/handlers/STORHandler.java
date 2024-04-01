package cn.jsou.ftpclient.ftp.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * 实现ConnectionHandler接口，用于处理STOR命令的连接
 * <p>该类负责将文件发送到FTP客户端</p>
 */
public class STORHandler implements ConnectionHandler {
	private static final Logger         logger = LogManager.getLogger(STORHandler.class);
	/**
	 * 用于同步等待处理完成的闭锁
	 */
	private final        CountDownLatch latch  = new CountDownLatch(1);
	/**
	 * 需要发送的文件
	 */
	private final        java.io.File   file;

	/**
	 * 构造函数
	 *
	 * @param file 发送文件时文件的位置
	 */
	public STORHandler(java.io.File file) {
		this.file = file;
	}

	/**
	 * 处理传入的连接，读取指定文件的数据并通过套接字发送
	 *
	 * @param socket 传入连接的套接字。
	 */
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

	/**
	 * 等待所有连接的处理完成
	 *
	 * @throws InterruptedException 如果线程在等待时被中断
	 */
	@Override public void waitForCompletion() throws InterruptedException {
		latch.await(); // 等待处理完成
	}
}
