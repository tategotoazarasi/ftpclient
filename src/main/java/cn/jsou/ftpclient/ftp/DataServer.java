package cn.jsou.ftpclient.ftp;

import cn.jsou.ftpclient.ftp.handlers.ConnectionHandler;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class DataServer implements Runnable {
	private static final Logger            logger = LogManager.getLogger(DataServer.class);
	final ServerSocket serverSocket;
	private              ConnectionHandler connectionHandler; // 注册的处理函数

	public DataServer(InetAddress ipAddress) throws IOException {
		// 创建一个ServerSocket，系统自动分配可用端口
		this.serverSocket = new ServerSocket(0, 1, ipAddress);
	}

	// 注册处理函数
	public void registerConnectionHandler(ConnectionHandler handler) {
		this.connectionHandler = handler;
	}

	// 开始监听数据连接
	@Override
	public void run() {
		try {
			while (!serverSocket.isClosed()) {
				Socket socket = serverSocket.accept();
				if (connectionHandler != null) {
					connectionHandler.handleConnection(socket);
				}
				IOUtils.closeQuietly(socket);
			}
		} catch (IOException e) {
			logger.error("DataServer stopped: {}", e.getMessage());
		}
	}

	// 关闭数据服务器
	public void close() {
		if (!serverSocket.isClosed()) {
			IOUtils.closeQuietly(serverSocket);
		}
	}

	public void waitHandlerComplete() throws InterruptedException {
		connectionHandler.waitForCompletion();
	}
}
