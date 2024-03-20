package cn.jsou.ftpclient.ftp;

import com.google.common.base.Joiner;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

import static cn.jsou.ftpclient.ftp.Command.*;

public class FtpCommands {
	private static final Logger logger = LogManager.getLogger(FtpCommands.class);

	private final BufferedReader reader;
	private final PrintWriter    writer;

	public FtpCommands(Socket socket) throws IOException {
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
	}

	/**
	 * 用户名
	 *
	 * <p>这个命令通常是用户在控制连接建立后传输的第一个命令（有些服务器可能要求这样）。一些服务器还可能要求额外的身份信息，如密码和/或账户命令。
	 * 服务器可能允许在任何时候输入新的USER命令，以更改访问控制和/或记账信息。这将清除已提供的任何用户、密码和账户信息，并重新开始登录序列。 所有传输参数保持不变，任何正在进行的文件传输都将在旧的访问控制参数下完成</p>
	 *
	 * @param username 一个Telnet字符串，用于识别用户。用户标识是服务器为访问其文件系统所需的。
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 */
	Response userName(String username) throws IOException {
		return sendCommand(USER_NAME, username);
	}

	private Response sendCommand(Command command, String... args) throws IOException {
		String commandLine = command.getCommand() + " " + Joiner.on(" ").join(args) + "\r\n";
		writer.print(commandLine);
		writer.flush();
		logger.info("Sent command:\t{}", commandLine);
		return readResponse();
	}

	Response readResponse() throws IOException {
		StringBuilder responseBuilder = new StringBuilder();
		String        responseLine;
		String        statusCode      = null;

		// 读取第一行响应
		responseLine = reader.readLine();
		logger.info("Server response:\t{}", responseLine);

		// 确定是否为多行响应
		boolean isMultiline = responseLine.matches("^\\d{3}-.*");
		if (isMultiline) {
			statusCode = responseLine.substring(0, 3);
			// 对于多行响应的第一行，去除状态码和空格后追加剩余文本
			responseBuilder.append(responseLine.substring(4));

			while ((responseLine = reader.readLine()) != null) {
				logger.info("Server response:\t{}", responseLine);
				// 对于最后一行响应，判断是否以状态码开头，如果是则去除状态码和空格
				if (responseLine.startsWith(statusCode + " ")) {
					responseBuilder.append("\n").append(responseLine.substring(4));
					break;
				} else {
					// 对于中间行，直接追加
					responseBuilder.append("\n").append(responseLine);
				}
			}
		} else if (responseLine.matches("^\\d{3} .*")) {
			// 对于非多行响应，直接返回状态码后的文本
			statusCode = responseLine.substring(0, 3);
			responseBuilder.append(responseLine.substring(4));
		}

		// 构造并返回Response对象
		return new Response(ReplyCode.findByCode(statusCode), responseBuilder.toString());
	}

	/**
	 * 密码
	 *
	 * <p>这个命令必须紧跟在用户名命令之后，并且对于一些站点来说，完成了用户的访问控制身份验证。</p>
	 *
	 * @param password 一个指定用户密码的Telnet字符串
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response password(String password) throws IOException {
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
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response account(String accountInformation) throws IOException {
		return sendCommand(ACCOUNT, accountInformation);
	}

	/**
	 * 打印工作目录
	 *
	 * <p>此命令导致在回复中返回当前工作目录的名称。</p>
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response printWorkingDirectory() throws IOException {
		return sendCommand(PRINT_WORKING_DIRECTORY);
	}

	/**
	 * 更改到父目录
	 *
	 * <p>此命令是CWD的特例，包含在内是为了简化在具有不同父目录命名语法的操作系统之间传输目录树的程序的实现。</p>
	 *
	 * @return 服务器的响应。回复代码应与CWD的回复代码相同。
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response changeToParentDirectory() throws IOException {
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
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response structureMount(String pathname) throws IOException {
		return sendCommand(STRUCTURE_MOUNT, pathname);
	}

	/**
	 * 注销
	 *
	 * <p>此命令终止一个USER会话，如果没有进行文件传输，服务器将关闭控制连接。如果文件传输正在进行中，连接将保持开放以等待结果响应，
	 * 然后服务器将其关闭。如果用户进程为多个用户传输文件但不希望为每个用户关闭然后重新打开连接，则应使用REIN命令而不是QUIT。 控制连接上的意外关闭将导致服务器采取中止（ABOR）和注销（QUIT）的有效操作。</p>
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response logout() throws IOException {
		return sendCommand(LOGOUT);
	}

	/**
	 * 重新初始化
	 *
	 * <p>此命令终止一个USER会话，清除所有I/O和账户信息，但允许任何正在进行的传输完成。
	 * 所有参数重置为默认设置，控制连接保持开放。这与用户刚刚打开控制连接后发现的状态相同。预计之后会有一个USER命令。</p>
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response reinitialize() throws IOException {
		return sendCommand(REINITIALIZE);
	}

	/**
	 * 系统
	 *
	 * <p>此命令用于找出服务器的操作系统类型。回复的第一个词应该是在当前版本的Assigned Numbers文档中列出的系统名称之一。</p>
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response system() throws IOException {
		return sendCommand(SYSTEM);
	}

	/**
	 * 功能列表
	 *
	 * <p>此命令要求服务器列出所有支持的命令以及任何特定于服务器的功能。这有助于客户端自动发现服务器能力，
	 * 并启用对特定FTP扩展的支持。FEAT命令的响应是一个多行回复，其中列出了服务器支持的所有功能和扩展。</p>
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc2389">RFC 2389</a>
	 */
	Response features() throws IOException {
		return sendCommand(FEATURES);
	}

	/**
	 * 选项设置
	 *
	 * <p>此命令用于启用或修改命令的特定选项。它允许客户端和服务器就使用特定FTP命令时的行为达成一致。
	 * 例如，客户端可以使用OPTS命令为MLST或MLSD命令指定希望在响应中看到的确切信息类型。这提高了客户端和服务器之间的互操作性，并允许针对特定会话自定义行为。</p>
	 *
	 * @param commandName    命令名称
	 * @param commandOptions 命令选项
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc2389">RFC 2389</a>
	 */
	Response options(String commandName, String commandOptions) throws IOException {
		if (commandOptions == null) {
			return sendCommand(OPTIONS, commandName);
		} else {
			return sendCommand(OPTIONS, commandName, commandOptions);
		}
	}

	/**
	 * 表示类型
	 *
	 * <p>参数指定了在数据表示和存储部分描述的表示类型。一些类型需要第二个参数。第一个参数由单个Telnet字符表示，
	 * 就像ASCII和EBCDIC的第二个格式参数一样；本地字节的第二个参数是一个十进制整数，表示字节大小。参数之间用（空格，ASCII代码32）分隔。
	 * 默认表示类型是ASCII非打印。如果更改了格式参数，稍后仅更改第一个参数，格式则返回到非打印默认值。</p>
	 *
	 * @param tc 表示类型
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response representationType(TypeCode tc) throws IOException {
		return sendCommand(REPRESENTATION_TYPE, String.valueOf(tc.getCode()));
	}

	/**
	 * 表示类型
	 *
	 * <p>参数指定了在数据表示和存储部分描述的表示类型。一些类型需要第二个参数。第一个参数由单个Telnet字符表示，
	 * 就像ASCII和EBCDIC的第二个格式参数一样；本地字节的第二个参数是一个十进制整数，表示字节大小。参数之间用（空格，ASCII代码32）分隔。
	 * 默认表示类型是ASCII非打印。如果更改了格式参数，稍后仅更改第一个参数，格式则返回到非打印默认值。</p>
	 *
	 * @param tc 表示类型
	 * @param fc 格式效果器
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response representationType(TypeCode tc, FormCode fc) throws IOException {
		StringBuilder param = new StringBuilder();
		param.append(tc.getCode());
		if (fc != null) {
			param.append(fc.getCode());
		}
		return sendCommand(REPRESENTATION_TYPE, param.toString());
	}

	public void close() {
		IOUtils.closeQuietly(reader);
		IOUtils.closeQuietly(writer);
	}
}
