package cn.jsou.ftpclient.ftp;

import java.io.*;
import java.net.Socket;

import static cn.jsou.ftpclient.ftp.Command.*;

public class FtpClient {
	private final String         server;
	private final Socket         socket;
	private final BufferedReader reader;
	private final PrintWriter    writer;

	public FtpClient(String server, String port) throws IOException {
		this.server = server;
		this.socket = new Socket(server, Integer.parseInt(port));
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

		// 处理服务器的欢迎信息
		readMultilineResponse();
	}

	// 方法：读取多行响应并返回状态码
	// 修改readMultilineResponse方法返回ReplyCode枚举类型
	private ReplyCode readMultilineResponse() throws IOException {
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
		ReplyCode userResponseCode = userName(username);
		if (userResponseCode != ReplyCode.USER_NAME_OKAY_NEED_PASSWORD) { // 331表示用户名OK，需要密码
			return false;
		}

		ReplyCode passResponseCode = password(password);
		return passResponseCode == ReplyCode.USER_LOGGED_IN; // 230表示登录成功
	}

	/**
	 * 用户名
	 *
	 * <p>这个命令通常是用户在控制连接建立后传输的第一个命令（有些服务器可能要求这样）。一些服务器还可能要求额外的身份信息，如密码和/或账户命令。
	 * 服务器可能允许在任何时候输入新的USER命令，以更改访问控制和/或记账信息。这将清除已提供的任何用户、密码和账户信息，并重新开始登录序列。 所有传输参数保持不变，任何正在进行的文件传输都将在旧的访问控制参数下完成</p>
	 *
	 * @param username 一个Telnet字符串，用于识别用户。用户标识是服务器为访问其文件系统所需的。
	 *
	 * @return 服务器的响应代码
	 *
	 * @throws IOException 如果发生I/O错误
	 */
	public ReplyCode userName(String username) throws IOException {
		return sendCommand(USER_NAME, username);
	}

	/**
	 * 密码
	 *
	 * <p>这个命令必须紧跟在用户名命令之后，并且对于一些站点来说，完成了用户的访问控制身份验证。</p>
	 *
	 * @param password 一个指定用户密码的Telnet字符串
	 *
	 * @return 服务器的响应代码
	 *
	 * @throws IOException 如果发生I/O错误
	 */
	public ReplyCode password(String password) throws IOException {
		return sendCommand(PASSWORD, password);
	}

	/**
	 * 账户
	 *
	 * <p>这个命令不一定与USER命令相关，因为有些站点可能需要登录账户，而其他站点只在特定访问时需要，如存储文件。
	 * 在后一种情况下，命令可以在任何时候到达。对于自动化，有回复代码来区分这些情况：当登录需要账户信息时，
	 * 成功的PASSword命令的响应是回复代码332。另一方面，如果登录不需要账户信息，成功的PASSword命令的回复是230； 如果在对话中稍后发出的命令需要账户信息，
	 * 服务器应根据它是存储（等待接收ACCounT命令）还是丢弃命令，分别返回332或532回复。</p>
	 *
	 * @param accountInformation 一个Telnet字符串，用于识别用户的账户
	 *
	 * @return 服务器的响应代码
	 *
	 * @throws IOException 如果发生I/O错误
	 */
	public ReplyCode account(String accountInformation) throws IOException {
		return sendCommand(ACCOUNT, accountInformation);
	}

	private ReplyCode sendCommand(Command command, String... args) throws IOException {
		StringBuilder commandBuilder = new StringBuilder(command.getCommand());
		for (String arg : args) {
			commandBuilder.append(" ").append(arg);
		}
		commandBuilder.append("\r\n");
		writer.print(commandBuilder);
		writer.flush();
		return readMultilineResponse();
	}

	/**
	 * 打印工作目录
	 *
	 * <p>此命令导致在回复中返回当前工作目录的名称。</p>
	 *
	 * @return 服务器的响应代码
	 *
	 * @throws IOException 如果发生I/O错误
	 */
	public ReplyCode printWorkingDirectory() throws IOException {
		return sendCommand(PRINT_WORKING_DIRECTORY);
	}

	/**
	 * 更改到父目录
	 *
	 * <p>此命令是CWD的特例，包含在内是为了简化在具有不同父目录命名语法的操作系统之间传输目录树的程序的实现。</p>
	 *
	 * @return 服务器的响应代码。回复代码应与CWD的回复代码相同。
	 *
	 * @throws IOException 如果发生I/O错误
	 */
	public ReplyCode changeToParentDirectory() throws IOException {
		return sendCommand(CHANGE_TO_PARENT_DIRECTORY);
	}

	/**
	 * 结构挂载
	 *
	 * <p>此命令允许用户在不更改其登录或账户信息的情况下，挂载不同的文件系统数据结构。
	 * 传输参数同样未改变。参数是指定目录或其他系统依赖的文件组指示符的路径名。</p>
	 *
	 * @param pathname 一个Telnet字符串，用于识别文件系统的文件组
	 *
	 * @return 服务器的响应代码
	 *
	 * @throws IOException 如果发生I/O错误
	 */
	public ReplyCode structureMount(String pathname) throws IOException {
		return sendCommand(STRUCTURE_MOUNT, pathname);
	}

	/**
	 * 注销
	 *
	 * <p>此命令终止一个USER会话，如果没有进行文件传输，服务器将关闭控制连接。如果文件传输正在进行中，连接将保持开放以等待结果响应，
	 * 然后服务器将其关闭。如果用户进程为多个用户传输文件但不希望为每个用户关闭然后重新打开连接，则应使用REIN命令而不是QUIT。 控制连接上的意外关闭将导致服务器采取中止（ABOR）和注销（QUIT）的有效操作。</p>
	 *
	 * @return 服务器的响应代码
	 *
	 * @throws IOException 如果发生I/O错误
	 */
	public ReplyCode logout() throws IOException {
		return sendCommand(LOGOUT);
	}

	/**
	 * 重新初始化
	 *
	 * <p>此命令终止一个USER会话，清除所有I/O和账户信息，但允许任何正在进行的传输完成。
	 * 所有参数重置为默认设置，控制连接保持开放。这与用户刚刚打开控制连接后发现的状态相同。预计之后会有一个USER命令。</p>
	 *
	 * @return 服务器的响应代码
	 *
	 * @throws IOException 如果发生I/O错误
	 */
	public ReplyCode reinitialize() throws IOException {
		return sendCommand(REINITIALIZE);
	}

	public void disconnect() throws IOException {
		if (socket != null) {
			socket.close();
		}
	}
}