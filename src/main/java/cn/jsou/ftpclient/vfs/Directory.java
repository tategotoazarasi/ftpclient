package cn.jsou.ftpclient.vfs;

import java.util.HashMap;
import java.util.Map;

public class Directory {
	String                 name;
	Map<String, File>      files       = new HashMap<>();
	Map<String, Directory> directories = new HashMap<>();
	Directory              parent; // 上一级目录

	public Directory(String name) {
		this.name = name;
	}

	// 创建文件
	public void createFile(String name, long size) {
		files.put(name, new File(name, size));
	}

	// 创建文件
	public void createFile(String name, Map<String, String> factsMap) {
		files.put(name, new File(name, factsMap));
	}

	// 创建目录
	public Directory createDirectory(String name) {
		Directory directory = new Directory(name);
		directories.put(name, directory);
		directory.parent = this;
		return directory;
	}

	public String getName() {
		return name;
	}

	public Map<String, File> getFiles() {
		return files;
	}

	public Map<String, Directory> getDirectories() {return directories;}

	public Directory getParent() {
		return parent;
	}

	public void clear() {
		files.clear();
		directories.clear();
	}
}