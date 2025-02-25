package cn.jsou.ftpclient.ftp;

import cn.jsou.ftpclient.utils.GlobalPathUtil;
import com.google.common.base.Joiner;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static cn.jsou.ftpclient.ftp.Command.*;

/**
 * 提供FTP命令发送的功能类
 */
public class FtpCommands {
	private static final Logger         logger = LogManager.getLogger(FtpCommands.class);
	/**
	 * 用于读取服务器响应的缓冲读取器
	 */
	private final        BufferedReader reader;
	/**
	 * 用于向服务器发送命令的打印写入器
	 */
	private final        PrintWriter    writer;

	/**
	 * 构造一个新的FtpCommands实例，初始化与服务器的通信渠道
	 *
	 * @param socket 客户端与服务器间的连接套接字
	 *
	 * @throws IOException 如果从套接字获取输入/输出流时发生I/O错误
	 */
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

	/**
	 * 向服务器发送FTP命令并返回响应
	 *
	 * @param command 要发送的FTP命令
	 * @param args    命令的参数
	 *
	 * @return 命令执行后服务器的响应对象
	 *
	 * @throws IOException 如果发生I/O错误
	 */
	private Response sendCommand(Command command, String... args) throws IOException {
		String commandLine = command.getCommand() + " " + Joiner.on(" ").join(args) + "\r\n";
		writer.print(commandLine);
		writer.flush();
		logger.info("Sent command:\t{}", commandLine);
		return readResponse();
	}

	/**
	 * 从服务器读取响应。
	 *
	 * @return 服务器响应的内容。
	 *
	 * @throws IOException 如果读取过程中发生I/O错误。
	 */
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
	 * <p>此命令是CWD的特例，包含在内是为了简化在具有不同父目录命名语法的操作系统之间传输目录树的程序的实现。
	 * 回复代码应与CWD的回复代码相同。有关更多详细信息，请参见附录II。</p>
	 *
	 * @return 服务器的响应。回复代码应与CWD的回复代码相同。
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response changeWorkingDirectory(String pathname) throws IOException {
		pathname = GlobalPathUtil.normalizePath(pathname);
		return sendCommand(CHANGE_WORKING_DIRECTORY, pathname);
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
	 * 数据端口
	 *
	 * <p>参数是用于数据连接的数据端口的HOST-PORT规范。用户和服务器数据端口都有默认值，在正常情况下不需要这个命令及其回复。
	 * 如果使用此命令，参数是32位互联网主机地址和16位TCP端口地址的连接。 这个地址信息被分解为8位字段，每个字段的值作为十进制数（以字符字符串表示）传输。字段之间用逗号分隔。一个端口命令将是： PORT
	 * h1,h2,h3,h4,p1,p2 其中h1是互联网主机地址的高8位。</p>
	 *
	 * @param socket 用于数据连接的数据端口
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response dataPort(ServerSocket socket) throws IOException {
		InetAddress localAddress = socket.getInetAddress();
		int         port         = socket.getLocalPort();

		// 将IP地址转换为FTP命令所需的格式
		String hostAddress = localAddress.getHostAddress();
		String hostNumber  = hostAddress.replace(".", ",");

		// 计算端口号的高位和低位字节
		int highPort = port / (1 << 8);
		int lowPort  = port % (1 << 8);

		// 构造PORT命令的参数
		String commandArgument = String.format("%s,%d,%d", hostNumber, highPort, lowPort);

		// 发送PORT命令
		return sendCommand(DATA_PORT, commandArgument);
	}

	/**
	 * 机器列表目录
	 *
	 * <p>此命令要求服务器为指定的目录发送一个完整的机器可解析的目录列表，包括所有子目录和文件。MLSD是MLST命令的扩展，
	 * 它提供了一种获取目录及其所有内容的详细信息的方法。与MLST类似，MLSD命令的输出设计为由客户端软件解析。</p>
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc3659">RFC 3659</a>
	 */
	Response machineListDictionary() throws IOException {
		return sendCommand(MACHINE_LIST_DICTIONARY);
	}

	/**
	 * 存储
	 *
	 * <p>此命令使服务器-DTP接受通过数据连接传输的数据，并将数据作为文件存储在服务器站点。
	 * 如果路径名中指定的文件在服务器站点存在，则其内容将被传输的数据替换。如果路径名中指定的文件不存在，则将在服务器站点创建新文件。</p>
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response store(String pathname) throws IOException {
		pathname = GlobalPathUtil.normalizePath(pathname);
		return sendCommand(STORE, pathname);
	}

	/**
	 * 检索
	 *
	 * <p>此命令使服务器-DTP将指定路径名中的文件副本传输到数据连接另一端的服务器或用户-DTP。服务器站点上的文件的状态和内容不受影响。</p>
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response retrieve(String filename) throws IOException {
		filename = GlobalPathUtil.normalizePath(filename);
		return sendCommand(RETRIEVE, filename);
	}

	/**
	 * 重命名从
	 *
	 * <p>此命令指定要重命名的文件的旧路径名。此命令必须立即由指定新文件路径名的“重命名到”命令跟随。两个命令一起导致文件被重命名。</p>
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response renameFrom(String pathname) throws IOException {
		pathname = GlobalPathUtil.normalizePath(pathname);
		return sendCommand(RENAME_FROM, pathname);
	}

	/**
	 * 重命名到
	 *
	 * <p>此命令指定在紧接前面的“重命名从”命令中指定的文件的新路径名。两个命令一起导致一个文件被重命名。</p>
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response renameTo(String filename) throws IOException {
		filename = GlobalPathUtil.normalizePath(filename);
		return sendCommand(RENAME_TO, filename);
	}

	/**
	 * 删除
	 *
	 * <p>此命令导致在路径名中指定的文件在服务器站点被删除。
	 * 如果需要额外的保护级别（如查询，“您真的希望删除吗？”），则应由用户-FTP进程提供。</p>
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response delete(String filepath) throws IOException {
		filepath = GlobalPathUtil.normalizePath(filepath);
		return sendCommand(DELETE, filepath);
	}

	/**
	 * 创建目录
	 *
	 * <p>此命令导致在路径名中指定的目录被创建为一个目录（如果路径名是绝对的）或作为当前工作目录的子目录（如果路径名是相对的）。</p>
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response makeDirectory(String pathname) throws IOException {
		pathname = GlobalPathUtil.normalizePath(pathname);
		return sendCommand(MAKE_DIRECTORY, pathname);
	}

	/**
	 * 删除目录
	 *
	 * <p>此命令导致在路径名中指定的目录被删除，作为一个目录（如果路径名是绝对的）或作为当前工作目录的子目录（如果路径名是相对的）。</p>
	 *
	 * @return 服务器的响应
	 *
	 * @throws IOException 如果发生I/O错误
	 * @see <a href="https://tools.ietf.org/html/rfc959">RFC 959</a>
	 */
	Response removeDirectory(String pathname) throws IOException {
		pathname = GlobalPathUtil.normalizePath(pathname);
		return sendCommand(REMOVE_DIRECTORY, pathname);
	}

	/**
	 * 关闭与FTP服务器的通信渠道
	 */
	public void close() {
		IOUtils.closeQuietly(reader);
		IOUtils.closeQuietly(writer);
	}
}
