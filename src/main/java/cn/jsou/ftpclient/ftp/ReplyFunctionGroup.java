package cn.jsou.ftpclient.ftp;

public enum ReplyFunctionGroup {
	/**
	 * 语法
	 *
	 * <p>这些回复指的是语法错误、语法上正确但不属于任何功能类别的命令、未实现或多余的命令。</p>
	 */
	SYNTAX(0, "Syntax"),
	/**
	 * 信息
	 *
	 * <p>这些是对信息请求的回复，如状态或帮助。</p>
	 */
	INFORMATION(1, "Information"),
	/**
	 * 连接
	 *
	 * <p>指的是控制和数据连接的回复。</p>
	 */
	CONNECTIONS(2, "Connections"),
	/**
	 * 身份验证和账户
	 *
	 * <p>登录过程的回复。</p>
	 */
	AUTHENTICATION_AND_ACCOUNTING(3, "Authentication and accounting"),
	/**
	 * 尚未指定
	 */
	UNSPECIFIED(4, "Unspecified as yet"),
	/**
	 * 文件系统
	 *
	 * <p>这些回复指出服务器文件系统相对于请求的传输或其他文件系统操作的状态。</p>
	 */
	FILE_SYSTEM(5, "File system");

	private final int    group;
	private final String description;

	ReplyFunctionGroup(int group, String description) {
		this.group       = group;
		this.description = description;
	}

	/**
	 * 根据回复代码的第二位返回对应的ReplyFunctionGroup枚举类型。
	 *
	 * @param replyCode 回复代码枚举
	 *
	 * @return 对应的ReplyFunctionGroup枚举类型
	 */
	public static ReplyFunctionGroup getFunctionGroup(ReplyCode replyCode) {
		int secondDigit = Integer.parseInt(Integer.toString(replyCode.getCode()).substring(1, 2));
		return switch (secondDigit) {
			case 0 -> ReplyFunctionGroup.SYNTAX;
			case 1 -> ReplyFunctionGroup.INFORMATION;
			case 2 -> ReplyFunctionGroup.CONNECTIONS;
			case 3 -> ReplyFunctionGroup.AUTHENTICATION_AND_ACCOUNTING;
			case 4 ->
				// Note: Assuming '4' is for unspecified as there's no specific group defined in the example
					ReplyFunctionGroup.UNSPECIFIED;
			case 5 -> ReplyFunctionGroup.FILE_SYSTEM;
			default -> throw new IllegalArgumentException("Invalid ReplyCode: " + replyCode);
		};
	}

	public int getGroup() {
		return group;
	}

	public String getDescription() {
		return description;
	}
}
