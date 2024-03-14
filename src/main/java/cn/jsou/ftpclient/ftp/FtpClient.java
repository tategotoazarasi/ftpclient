package cn.jsou.ftpclient.ftp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class FtpClient {
	private final String         server;
	private final Socket         socket;
	private final BufferedReader reader;
	private final PrintWriter    writer;

	public FtpClient(String server, String port) throws Exception {
		this.server = server;
		this.socket = new Socket(server, Integer.parseInt(port));
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

		// 处理服务器的欢迎信息
		readMultilineResponse();
	}

	// 方法：读取多行响应并返回状态码
	// 修改readMultilineResponse方法返回ReplyCode枚举类型
	private ReplyCode readMultilineResponse() throws Exception {
		String responseLine;
		String statusCode = null;

		responseLine = reader.readLine();
		System.out.println("Server response: " + responseLine);
		if (responseLine.matches("^\\d{3}-.*")) {
			statusCode = responseLine.substring(0, 3);
			while ((responseLine = reader.readLine()) != null) {
				System.out.println("Server response: " + responseLine);
				if (responseLine.startsWith(statusCode + " ")) {
					break;
				}
			}
		} else if (responseLine.matches("^\\d{3} .*")) {
			// 对于非多行响应，直接返回状态码
			statusCode = responseLine.substring(0, 3);
		}
		return ReplyCode.findByCode(statusCode); // 使用findByCode查找并返回ReplyCode枚举
	}

	// 登录方法
	public boolean login(String username, String password) throws Exception {
		ReplyCode userResponseCode = executeCommand("USER", username);
		if (userResponseCode != ReplyCode.USER_NAME_OKAY_NEED_PASSWORD) { // 331表示用户名OK，需要密码
			return false;
		}

		ReplyCode passResponseCode = executeCommand("PASS", password);
		return passResponseCode == ReplyCode.USER_LOGGED_IN; // 230表示登录成功
	}

	// 方法：执行FTP命令并读取响应
	public ReplyCode executeCommand(String command, String... args) throws Exception {
		StringBuilder commandBuilder = new StringBuilder(command);
		for (String arg : args) {
			commandBuilder.append(" ").append(arg);
		}
		writer.println(commandBuilder);

		return readMultilineResponse();
	}

	public void disconnect() throws Exception {
		if (socket != null) {
			socket.close();
		}
	}
}

