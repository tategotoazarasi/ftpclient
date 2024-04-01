package cn.jsou.ftpclient.ftp.handlers;

import java.net.Socket;

/**
 * 用于处理传入连接的接口。
 */
public interface ConnectionHandler {
	/**
	 * 处理传入的连接。
	 *
	 * @param socket 传入连接的套接字。
	 */
	void handleConnection(Socket socket);

	/**
	 * 等待所有连接的完成。
	 *
	 * @throws InterruptedException 如果线程在等待时被中断。
	 */
	void waitForCompletion() throws InterruptedException;
}
