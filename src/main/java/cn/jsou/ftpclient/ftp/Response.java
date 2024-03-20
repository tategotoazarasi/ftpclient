package cn.jsou.ftpclient.ftp;

public class Response {
	private final ReplyCode replyCode;
	private final String    message;

	public Response(ReplyCode replyCode, String message) {
		this.replyCode = replyCode;
		this.message   = message;
	}

	public ReplyCode getReplyCode() {
		return replyCode;
	}

	public String getMessage() {
		return message;
	}

	public boolean isSuccess() {
		ReplyType replyType = ReplyType.getReplyType(replyCode);
		return (replyType == ReplyType.POSITIVE_INTERMEDIATE ||
		        replyType == ReplyType.POSITIVE_PRELIMINARY ||
		        replyType == ReplyType.POSITIVE_COMPLETION);
	}
}
