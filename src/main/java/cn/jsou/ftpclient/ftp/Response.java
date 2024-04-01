package cn.jsou.ftpclient.ftp;

/**
 * FTP回复类
 */
public class Response {
	/**
	 * 回复码
	 */
	private final ReplyCode replyCode;
	/**
	 * 回复消息
	 */
	private final String    message;

	/**
	 * 构造函数
	 *
	 * @param replyCode 回复码
	 * @param message   回复消息
	 */
	public Response(ReplyCode replyCode, String message) {
		this.replyCode = replyCode;
		this.message   = message;
	}

	/**
	 * 获取回复码
	 *
	 * @return 回复码
	 */
	public ReplyCode getReplyCode() {
		return replyCode;
	}

	/**
	 * 获取回复消息
	 *
	 * @return 回复消息
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * 判断是否成功
	 *
	 * @return 是否成功
	 */
	public boolean isSuccess() {
		ReplyType replyType = ReplyType.getReplyType(replyCode);
		return (replyType == ReplyType.POSITIVE_INTERMEDIATE ||
		        replyType == ReplyType.POSITIVE_PRELIMINARY ||
		        replyType == ReplyType.POSITIVE_COMPLETION);
	}
}
