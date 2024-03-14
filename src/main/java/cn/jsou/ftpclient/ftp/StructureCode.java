package cn.jsou.ftpclient.ftp;

public enum StructureCode {
	/**
	 * 文件（无记录结构）
	 */
	FILE('F'),
	/**
	 * 记录结构
	 */
	RECORD('R'),
	/**
	 * 页面结构
	 */
	PAGE('P');
	private final char code;

	StructureCode(char code) {
		this.code = code;
	}

	public char getCode() {
		return code;
	}
}
