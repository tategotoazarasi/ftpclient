package cn.jsou.ftpclient.vfs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class File {
	String        name;
	long          size; // 文件大小（字节）
	LocalDateTime modifiedTime; // 修改时间
	LocalDateTime createdTime; // 创建时间

	public File(String name, long size) {
		this.name         = name;
		this.size         = size;
		this.createdTime  = LocalDateTime.now();
		this.modifiedTime = LocalDateTime.now();
	}

	public File(String name, long size, LocalDateTime modifiedTime, LocalDateTime createdTime) {
		this.name         = name;
		this.size         = size;
		this.createdTime = createdTime;
		this.modifiedTime = modifiedTime;
	}

	public File(String name, Map<String, String> factsMap) {
		this.name = name;
		this.size = Long.parseLong(factsMap.getOrDefault("size", "0"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		this.modifiedTime =
				LocalDateTime.parse(factsMap.getOrDefault("modify", LocalDateTime.now().format(formatter)), formatter);
		this.createdTime  =
				LocalDateTime.parse(factsMap.getOrDefault("create", LocalDateTime.now().format(formatter)), formatter);
	}

	public String getName() {
		return name;
	}

	public long getSize() {
		return size;
	}

	public LocalDateTime getModifiedTime() {
		return modifiedTime;
	}

	public LocalDateTime getCreatedTime() {
		return createdTime;
	}

}