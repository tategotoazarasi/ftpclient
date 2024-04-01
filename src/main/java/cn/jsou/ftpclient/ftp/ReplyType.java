package cn.jsou.ftpclient.ftp;

/**
 * FTP回复类型枚举
 */
public enum ReplyType {
	/**
	 * 正面预备回复
	 *
	 * <p>请求的动作正在被启动；在进行新命令之前，期待另一个回复。（在完成回复之前发送另一个命令的用户进程将违反协议；
	 * 但服务器-FTP进程应该对在前一个命令进行中时到达的任何命令进行排队。）这种类型的回复可以用来指示命令已被接受，
	 * 用户进程现在可以注意数据连接，对于同时监控困难的实现而言。服务器-FTP进程最多可以对每个命令发送一个1yz回复。</p>
	 */
	POSITIVE_PRELIMINARY(1, "Positive Preliminary reply"),
	/**
	 * 正面完成回复
	 *
	 * <p>请求的动作已成功完成。可以启动一个新请求。</p>
	 */
	POSITIVE_COMPLETION(2, "Positive Completion reply"),
	/**
	 * 正面中间回复
	 *
	 * <p>命令已被接受，但请求的动作正在等待接收更多信息后再进行。用户应发送另一个指定此信息的命令。这个回复用于命令序列组。</p>
	 */
	POSITIVE_INTERMEDIATE(3, "Positive Intermediate reply"),
	/**
	 * 临时负面完成回复
	 *
	 * <p>命令未被接受，请求的动作没有发生，但错误条件是暂时的，动作可能会被再次请求。用户应返回到命令序列的开始，如果有的话。
	 * 很难给“临时”分配一个意义，尤其是当两个不同的站点（服务器和用户进程）必须对解释达成一致时。 4yz类别中的每个回复可能有稍微不同的时间值，但意图是鼓励用户进程再次尝试。
	 * 在判断回复属于4yz还是5yz（永久负面）类别时的经验法则是，如果命令可以重复而不改变命令形式或用户或服务器的属性，
	 * 则回复为4yz（例如，命令拼写相同，使用相同的参数；用户没有改变他的文件访问或用户名；服务器没有实施新的实现）。</p>
	 */
	TRANSIENT_NEGATIVE_COMPLETION(4, "Transient Negative Completion reply"),
	/**
	 * 永久负面完成回复
	 *
	 * <p>命令未被接受，请求的动作没有发生。不鼓励用户进程重复完全相同的请求（以相同的序列）。即使一些“永久”的错误条件可以被纠正，
	 * 所以人类用户可能希望在未来的某个时刻通过直接行动指导他的用户进程重新启动命令序列（例如，在拼写被更改后，或用户已更改他的目录状态后）。</p>
	 */
	PERMANENT_NEGATIVE_COMPLETION(5, "Permanent Negative Completion reply");
	/**
	 * 回复类型
	 */
	private final int    type;
	/**
	 * 描述
	 */
	private final String description;

	/**
	 * 构造函数
	 *
	 * @param type        回复类型
	 * @param description 描述
	 */
	ReplyType(int type, String description) {
		this.type        = type;
		this.description = description;
	}

	/**
	 * 根据回复代码的第一位返回对应的ReplyType枚举类型。
	 *
	 * @param replyCode 回复代码枚举
	 *
	 * @return 对应的ReplyType枚举类型
	 */
	public static ReplyType getReplyType(ReplyCode replyCode) {
		int firstDigit = Integer.parseInt(Integer.toString(replyCode.getCode()).substring(0, 1));
		return switch (firstDigit) {
			case 1 -> ReplyType.POSITIVE_PRELIMINARY;
			case 2 -> ReplyType.POSITIVE_COMPLETION;
			case 3 -> ReplyType.POSITIVE_INTERMEDIATE;
			case 4 -> ReplyType.TRANSIENT_NEGATIVE_COMPLETION;
			case 5 -> ReplyType.PERMANENT_NEGATIVE_COMPLETION;
			default -> throw new IllegalArgumentException("Invalid ReplyCode: " + replyCode);
		};
	}

	/**
	 * 获取回复类型的整数值
	 *
	 * @return 回复类型的整数值
	 */
	public int getType() {
		return type;
	}

	/**
	 * 获取回复类型的描述
	 *
	 * @return 回复类型的描述
	 */
	public String getDescription() {
		return description;
	}
}