package cn.jsou.ftpclient.ftp;

public enum FormCode {
	/**
	 * 非打印
	 * <p>不对数据进行任何转换（非打印控制字符）</p>
	 */
	NON_PRINT('N'),
	/**
	 * Telnet格式效果器
	 * <p>数据需要进行Telnet协议的转换（如回车换行处理）</p>
	 */
	TELNET_FORMAT_EFFECTORS('T'),
	/**
	 * 回车控制（ASA）
	 * <p>对数据进行ASA打印机控制字符的转换</p>
	 */
	CARRIAGE_CONTROL('C');

	private final char code;

	FormCode(char code) {
		this.code = code;
	}

	public char getCode() {
		return code;
	}
}

