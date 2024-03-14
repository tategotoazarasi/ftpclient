package cn.jsou.ftpclient.ftp;

public enum ModeCode {
	/**
	 * 流
	 */
	STREAM('S'),
	/**
	 * 块
	 */
	BLOCK('B'),
	/**
	 * 压缩
	 */
	COMPRESSED('C');
	private final char code;

	ModeCode(char code) {
		this.code = code;
	}

	public char getCode() {
		return code;
	}
}
