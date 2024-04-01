package cn.jsou.ftpclient.ftp.handlers;

import cn.jsou.ftpclient.vfs.VirtualFileSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 实现ConnectionHandler接口，用于处理MLSD命令的连接
 */
public class MLSDHandler implements ConnectionHandler {
	private static final Logger            logger = LogManager.getLogger(MLSDHandler.class);
	/**
	 * 虚拟文件系统，用于创建文件和目录
	 */
	private final        VirtualFileSystem vfs;
	/**
	 * 用于同步等待处理完成的闭锁
	 */
	private final        CountDownLatch    latch  = new CountDownLatch(1);

	/**
	 * 构造函数
	 *
	 * @param vfs 用于文件操作的虚拟文件系统实例
	 */
	public MLSDHandler(VirtualFileSystem vfs) {
		this.vfs = vfs;
	}

	/**
	 * 处理传入的连接
	 *
	 * @param socket 传入连接的套接字。
	 */
	@Override public void handleConnection(Socket socket) {
		try (InputStream inputStream = socket.getInputStream();
		     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				logger.debug("MLSD line: {}", line);
				String[]            facts    = line.split(";");
				String              filename = facts[facts.length - 1].trim();
				Map<String, String> factsMap = new HashMap<>();
				for (String fact : facts) {
					int equalsIndex = fact.indexOf('=');
					if (equalsIndex != -1) {
						String key   = fact.substring(0, equalsIndex);
						String value = fact.substring(equalsIndex + 1);
						factsMap.put(key, value);
					}
				}
				if (factsMap.containsKey("type") && factsMap.get("type").equals("dir")) {
					vfs.createDirectory(filename);
				} else if (factsMap.containsKey("type") && factsMap.get("type").equals("file")) {
					vfs.createFile(filename, factsMap);
				}
			}
		} catch (IOException e) {
			logger.error("Error handling MLSD data connection", e);
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
