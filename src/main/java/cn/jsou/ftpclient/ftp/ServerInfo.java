package cn.jsou.ftpclient.ftp;

import java.util.HashSet;
import java.util.Set;

/**
 * 表示FTP服务器信息的类，包括服务器支持的特性和系统信息
 */
public class ServerInfo {
	/**
	 * 存储服务器支持的特性集合
	 */
	private final Set<String> features = new HashSet<>();
	/**
	 * 系统信息
	 */
	private       String      sysInfo;

	/**
	 * 构造函数
	 */
	public ServerInfo() {}

	/**
	 * 设置服务器的系统信息
	 *
	 * @param sysInfo 系统信息字符串
	 */
	public void setSysInfo(String sysInfo) {
		this.sysInfo = sysInfo;
	}

	/**
	 * 向特性集合中添加一个新特性
	 *
	 * @param feature 要添加的特性字符串
	 */
	public void addFeature(String feature) {
		features.add(feature);
	}

	/**
	 * 检查服务器是否支持指定的特性
	 *
	 * @param feature 要检查的特性字符串
	 *
	 * @return 如果服务器支持该特性，返回true；否则返回false
	 */
	public boolean hasFeature(String feature) {
		return features.contains(feature);
	}
}
