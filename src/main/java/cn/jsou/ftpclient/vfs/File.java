package cn.jsou.ftpclient.vfs;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * 表示文件系统中的一个文件，包括文件的基本信息
 */
public class File {
	/**
	 * 文件名
	 */
	String        name;
	/**
	 * 文件大小（单位：字节）
	 */
	long          size;
	/**
	 * 文件的修改时间
	 */
	LocalDateTime modifiedTime;
	/**
	 * 文件的创建时间
	 */
	LocalDateTime createdTime;

	/**
	 * 构造一个新的文件实例，初始化为当前时间的创建和修改时间
	 *
	 * @param name 文件名
	 * @param size 文件大小（字节）
	 */
	public File(String name, long size) {
		this.name         = name;
		this.size         = size;
		this.createdTime  = LocalDateTime.now();
		this.modifiedTime = LocalDateTime.now();
	}

	/**
	 * 构造一个新的文件实例，使用指定的创建和修改时间
	 *
	 * @param name         文件名
	 * @param size         文件大小（字节）
	 * @param modifiedTime 文件的修改时间
	 * @param createdTime  文件的创建时间
	 */
	public File(String name, long size, LocalDateTime modifiedTime, LocalDateTime createdTime) {
		this.name         = name;
		this.size         = size;
		this.createdTime  = createdTime;
		this.modifiedTime = modifiedTime;
	}

	/**
	 * 根据文件属性映射表构造一个新的文件实例
	 *
	 * @param name     文件名
	 * @param factsMap 包含文件属性的映射表，例如大小、修改时间和创建时间
	 */
	public File(String name, Map<String, String> factsMap) {
		this.name = name;
		this.size = Long.parseLong(factsMap.getOrDefault("size", "0"));
		DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSS");

		this.modifiedTime = parseDate(factsMap.get("modify"), formatter1, formatter2);
		this.createdTime  = parseDate(factsMap.get("create"), formatter1, formatter2);
	}

	private LocalDateTime parseDate(String dateStr, DateTimeFormatter formatter1, DateTimeFormatter formatter2) {
		if (dateStr != null) {
			try {
				return LocalDateTime.parse(dateStr, formatter1);
			} catch (DateTimeParseException e) {
				try {
					ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateStr, formatter2.withZone(ZoneId.of("UTC+8")));
					return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
				} catch (DateTimeParseException ex) {
					return LocalDateTime.now();
				}
			}
		}
		return null;
	}

	/**
	 * 获取文件名
	 *
	 * @return 文件名
	 */
	public String getName() {
		return name;
	}

	/**
	 * 获取文件大小（字节）
	 *
	 * @return 文件大小
	 */
	public long getSize() {
		return size;
	}

	/**
	 * 获取文件的修改时间
	 *
	 * @return 修改时间
	 */
	public LocalDateTime getModifiedTime() {
		return modifiedTime;
	}

	/**
	 * 获取文件的创建时间
	 *
	 * @return 创建时间
	 */
	public LocalDateTime getCreatedTime() {
		return createdTime;
	}

}