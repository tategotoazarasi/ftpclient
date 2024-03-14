package cn.jsou.ftpclient.ftp;

public enum ReplyCode {
	/**
	 * 重启标记回复
	 *
	 * <p>在这种情况下，文本是准确的，不留给特定的实现；它必须读作： MARK yyyy = mmmm
	 * 其中yyyy是用户进程数据流标记，mmmm是服务器的等效标记（注意标记和”=“之间的空格）。</p>
	 */
	RESTART_MARKER_REPLY(110, "Restart marker reply."),
	/**
	 * 服务在nnn分钟内准备就绪
	 */
	SERVICE_READY_IN_MINUTES(120, "Service ready in nnn minutes."),
	/**
	 * 数据连接已打开；传输开始
	 */
	DATA_CONNECTION_ALREADY_OPEN(125, "Data connection already open; transfer starting."),
	/**
	 * 文件状态正常；即将打开数据连接
	 */
	FILE_STATUS_OKAY(150, "File status okay; about to open data connection."),
	/**
	 * 命令正常
	 */
	COMMAND_OKAY(200, "Command okay."),
	/**
	 * 命令未实现，此站点多余
	 */
	COMMAND_NOT_IMPLEMENTED_SUPERFLUOUS(202, "Command not implemented, superfluous at this site."),
	/**
	 * 系统状态，或系统帮助回复
	 */
	SYSTEM_STATUS_OR_HELP_REPLY(211, "System status, or system help reply."),
	/**
	 * 目录状态
	 */
	DIRECTORY_STATUS(212, "Directory status."),
	/**
	 * 文件状态
	 */
	FILE_STATUS(213, "File status."),
	/**
	 * 帮助消息
	 *
	 * <p>关于如何使用服务器或某个特定的非标准命令的含义。这个回复只对人类用户有用。</p>
	 */
	HELP_MESSAGE(214, "Help message."),
	/**
	 * NAME系统类型
	 *
	 * <p>其中NAME是从分配的数字文档中的官方系统名称列表中选取的。</p>
	 */
	NAME_SYSTEM_TYPE(215, "NAME system type."),
	/**
	 * 服务为新用户准备就绪
	 */
	SERVICE_READY_FOR_NEW_USER(220, "Service ready for new user."),
	/**
	 * 服务关闭控制连接
	 *
	 * <p>如适当，注销。</p>
	 */
	SERVICE_CLOSING_CONTROL_CONNECTION(221, "Service closing control connection."),
	/**
	 * 数据连接打开；没有进行中的传输
	 */
	DATA_CONNECTION_OPEN_NO_TRANSFER_IN_PROGRESS(225, "Data connection open; no transfer in progress."),
	/**
	 * 关闭数据连接
	 *
	 * <p>请求的文件操作成功（例如，文件传输或文件中止）。</p>
	 */
	CLOSING_DATA_CONNECTION(226, "Closing data connection."),
	/**
	 * 进入被动模式
	 */
	ENTERING_PASSIVE_MODE(227, "Entering Passive Mode (h1,h2,h3,h4,p1,p2)."),
	/**
	 * 用户登录，继续
	 */
	USER_LOGGED_IN(230, "User logged in, proceed."),
	/**
	 * 请求的文件操作正常，已完成
	 */
	REQUESTED_FILE_ACTION_OKAY(250, "Requested file action okay, completed."),
	/**
	 * “PATHNAME”已创建
	 */
	PATHNAME_CREATED(257, "\"PATHNAME\" created."),
	/**
	 * 用户名正常，需要密码
	 */
	USER_NAME_OKAY_NEED_PASSWORD(331, "User name okay, need password."),
	/**
	 * 登录需要账户
	 */
	NEED_ACCOUNT_FOR_LOGIN(332, "Need account for login."),
	/**
	 * 请求的文件操作等待更多信息
	 */
	REQUESTED_FILE_ACTION_PENDING_FURTHER_INFORMATION(350, "Requested file action pending further information."),
	/**
	 * 服务不可用，关闭控制连接
	 *
	 * <p>如果服务知道它必须关闭，则可能是对任何命令的回复。</p>
	 */
	SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION(421, "Service not available, closing control connection."),
	/**
	 * 无法打开数据连接
	 */
	CANT_OPEN_DATA_CONNECTION(425, "Can’t open data connection."),
	/**
	 * 连接关闭；传输中止
	 */
	CONNECTION_CLOSED_TRANSFER_ABORTED(426, "Connection closed; transfer aborted."),
	/**
	 * 未采取请求的文件操作
	 *
	 * <p>文件不可用（例如，文件忙）</p>
	 */
	REQUESTED_FILE_ACTION_NOT_TAKEN_FILE_UNAVAILABLE(450, "Requested file action not taken. File unavailable."),
	/**
	 * 请求的操作中止：处理中的本地错误
	 */
	REQUESTED_ACTION_ABORTED_LOCAL_ERROR_IN_PROCESSING(451, "Requested action aborted: local error in processing."),
	/**
	 * 未采取请求的操作
	 *
	 * <p>系统中的存储空间不足。</p>
	 */
	REQUESTED_ACTION_NOT_TAKEN_INSUFFICIENT_STORAGE_SPACE(452,
	                                                      "Requested action not taken. Insufficient storage space in system."),
	/**
	 * 语法错误，命令无法识别
	 *
	 * <p>这可能包括命令行太长等错误。</p>
	 */
	SYNTAX_ERROR_COMMAND_UNRECOGNIZED(500, "Syntax error, command unrecognized."),
	/**
	 * 参数或参数中的语法错误
	 */
	SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS(501, "Syntax error in parameters or arguments."),
	/**
	 * 命令未实现
	 */
	COMMAND_NOT_IMPLEMENTED(502, "Command not implemented."),
	/**
	 * 命令序列错误
	 */
	BAD_SEQUENCE_OF_COMMANDS(503, "Bad sequence of commands."),
	/**
	 * 该参数未实现命令
	 */
	COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER(504, "Command not implemented for that parameter."),
	/**
	 * 未登录
	 */
	NOT_LOGGED_IN(530, "Not logged in."),
	/**
	 * 存储文件需要账户
	 */
	NEED_ACCOUNT_FOR_STORING_FILES(532, "Need account for storing files."),
	/**
	 * 未采取请求的操作
	 *
	 * <p>文件不可用（例如，文件未找到，无访问权限）</p>
	 */
	REQUESTED_ACTION_NOT_TAKEN_FILE_UNAVAILABLE_ACCESS(550, "Requested action not taken. File unavailable."),
	/**
	 * 请求的操作中止：页面类型未知
	 */
	REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN(551, "Requested action aborted: page type unknown."),
	/**
	 * 请求的文件操作中止
	 *
	 * <p>超出存储分配（对当前目录或数据集）。</p>
	 */
	REQUESTED_FILE_ACTION_ABORTED_EXCEEDED_STORAGE(552, "Requested file action aborted. Exceeded storage allocation."),
	/**
	 * 未采取请求的操作
	 *
	 * <p>文件名不允许。</p>
	 */
	REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED(553, "Requested action not taken. File name not allowed.");

	private final int    code;
	private final String message;

	ReplyCode(int code, String message) {
		this.code    = code;
		this.message = message;
	}

	/**
	 * 通过字符串形式的响应代码查找对应的枚举实例。
	 *
	 * @param codeStr 字符串形式的响应代码
	 *
	 * @return 对应的ReplyCode枚举实例
	 */
	public static ReplyCode findByCode(String codeStr) {
		try {
			int code = Integer.parseInt(codeStr);
			return findByCode(code);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid ReplyCode string: " + codeStr, e);
		}
	}

	/**
	 * 通过整数响应代码查找对应的枚举实例。
	 *
	 * @param code 整数响应代码
	 *
	 * @return 对应的ReplyCode枚举实例
	 */
	public static ReplyCode findByCode(int code) {
		for (ReplyCode rc : ReplyCode.values()) {
			if (rc.getCode() == code) {
				return rc;
			}
		}
		throw new IllegalArgumentException("No matching ReplyCode found for code: " + code);
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
