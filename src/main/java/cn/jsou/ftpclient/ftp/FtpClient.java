package cn.jsou.ftpclient.ftp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class FtpClient {
	private String         server;
	private Socket         socket;
	private BufferedReader reader;
	private PrintWriter    writer;

	// 构造函数，用于初始化与FTP服务器的连接
	public FtpClient(String server, String port) throws Exception {
		this.server = server;
		this.socket = new Socket(server, Integer.parseInt(port)); // FTP默认端口是21
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

		// 处理多行的服务器欢迎信息
		readMultilineResponse();
	}

	private void readMultilineResponse() throws Exception {
		String responseLine;
		// 首先读取响应的第一行
		responseLine = reader.readLine();
		System.out.println("Server response: " + responseLine);
		// 检查是否为多行响应的开始（状态码后跟"-"）
		if (responseLine.matches("^\\d{3}-.*")) {
			// 提取状态码用于匹配多行响应的结束
			String statusCode = responseLine.substring(0, 3);
			// 循环读取后续行直到遇到以状态码后跟空格开始的行
			while ((responseLine = reader.readLine()) != null) {
				System.out.println("Server response: " + responseLine);
				// 当前行是否标志着多行响应的结束
				if (responseLine.startsWith(statusCode + " ")) {
					break; // 结束行找到，退出循环
				}
			}
		}
	}

	// 登录方法
	public boolean login(String username, String password) throws Exception {
		// 发送用户名
		writer.println("USER " + username);
		String userResponse = reader.readLine();
		System.out.println("USER command response: " + userResponse);

		// 发送密码
		writer.println("PASS " + password);
		String passResponse = reader.readLine();
		System.out.println("PASS command response: " + passResponse);

		// 根据服务器响应判断登录是否成功
		// 注意：这里的判断非常简化，实际情况下需要根据FTP响应代码进行更精确的判断
		return passResponse.startsWith("230");
	}

	// 关闭连接
	public void disconnect() throws Exception {
		if (socket != null) {
			socket.close();
		}
	}
}
