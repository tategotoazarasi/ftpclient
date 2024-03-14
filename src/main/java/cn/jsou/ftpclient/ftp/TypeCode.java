package cn.jsou.ftpclient.ftp;

public enum TypeCode {
	/**
	 * ASCII类型
	 */
	ASCII('A'),
	/**
	 * EBCDIC类型
	 */
	EBCDIC('E'),
	/**
	 * 图像类型（二进制）
	 */
	IMAGE('I'),
	/**
	 * 本地类型。需要指定字节大小。
	 */
	LOCAL('L');

	private final char code;

	TypeCode(char code) {
		this.code = code;
	}

	public char getCode() {
		return code;
	}
}
