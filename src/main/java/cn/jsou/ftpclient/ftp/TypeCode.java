package cn.jsou.ftpclient.ftp;

/**
 * FTP传输类型枚举
 */
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
	/**
	 * 传输类型代码
	 */
	private final char code;

	/**
	 * 构造函数
	 *
	 * @param code 传输类型代码
	 */
	TypeCode(char code) {
		this.code = code;
	}

	/**
	 * 获取传输类型代码
	 *
	 * @return 传输类型代码
	 */
	public char getCode() {
		return code;
	}
}
