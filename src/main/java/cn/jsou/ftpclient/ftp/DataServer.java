package cn.jsou.ftpclient.ftp;

import cn.jsou.ftpclient.ftp.handlers.ConnectionHandler;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 数据服务器类，用于监听数据连接并将连接分派给注册的处理函数
 */
public class DataServer implements Runnable {
	private static final Logger            logger = LogManager.getLogger(DataServer.class);
	/**
	 * 服务器套接字，用于监听传入的数据连接
	 */
	final                ServerSocket      serverSocket;
	/**
	 * 注册的连接处理器，用于处理接收到的数据连接
	 */
	private              ConnectionHandler connectionHandler; // 注册的处理函数

	/**
	 * 构造函数，创建一个新的数据服务器实例
	 *
	 * @param ipAddress 服务器将绑定到的IP地址
	 *
	 * @throws IOException 如果创建ServerSocket时发生IO错误
	 */
	public DataServer(InetAddress ipAddress) throws IOException {
		// 创建一个ServerSocket，系统自动分配可用端口
		this.serverSocket = new ServerSocket(0, 1, ipAddress);
	}

	/**
	 * 注册一个连接处理器以处理接收到的数据连接
	 *
	 * @param handler 要注册的连接处理器
	 */
	public void setConnectionHandler(ConnectionHandler handler) {
		this.connectionHandler = handler;
	}

	/**
	 * 开始监听并接受数据连接。当接受到连接时，使用注册的处理函数进行处理
	 */
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

	/**
	 * 关闭数据服务器，停止接收数据连接
	 */
	public void close() {
		if (!serverSocket.isClosed()) {
			IOUtils.closeQuietly(serverSocket);
		}
	}

	/**
	 * 等待注册的连接处理器完成所有处理
	 */
	public void waitHandlerComplete() {
		try {
			connectionHandler.waitForCompletion();
		} catch (InterruptedException e) {
			logger.error("DataServer stopped: {}", e.getMessage());
		}
	}
}
