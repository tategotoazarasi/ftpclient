package cn.jsou.ftpclient.vfs;

import cn.jsou.ftpclient.ftp.FtpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class VirtualFileSystem implements FileSystemProvider {
	private static final Logger    logger = LogManager.getLogger(VirtualFileSystem.class);
	private final        Directory root; // 根目录
	private final        FtpClient ftpClient;
	private              Directory currentDirectory; // 当前目录

	public VirtualFileSystem(FtpClient ftpClient) {
		this.root             = new Directory("/");
		this.currentDirectory = root;
		this.ftpClient = ftpClient;
	}

	public Directory getSubDirectory(String name) {
		return currentDirectory.getDirectories().get(name);
	}

	// 创建目录
	public void createDirectory(String name) {
		currentDirectory.createDirectory(name);
	}

	// 创建文件
	public void createFile(String name, long size) {
		currentDirectory.createFile(name, size);
	}

	// 创建文件
	public void createFile(String name, Map<String, String> factsMap) {
		currentDirectory.createFile(name, factsMap);
	}

	private Directory createOrGetDirectory(Directory current, String[] pathComponents, int index) {
		if (index >= pathComponents.length) {
			return current;
		}

		// 尝试获取子目录，如果不存在则创建一个新的
		current.directories.putIfAbsent(pathComponents[index], new Directory(pathComponents[index]));
		Directory nextDir = current.directories.get(pathComponents[index]);
		nextDir.parent = current; // 确保父目录被正确设置

		// 递归创建/获取下一个目录
		return createOrGetDirectory(nextDir, pathComponents, index + 1);
	}

	public void changeDirectory(Directory dir) {
		this.currentDirectory = dir;
	}

	// 切换目录
	public boolean changeDirectory(String path) {
		if ("/".equals(path)) {
			currentDirectory = root;
			return true;
		} else if ("..".equals(path)) {
			if (currentDirectory.parent != null) {
				currentDirectory = currentDirectory.parent;
				return true;
			}
			return false;
		} else {
			// 支持多级目录创建
			String[] pathComponents = path.split("/");
			if (pathComponents.length > 0) {
				// 如果第一个组件为空（即路径以"/"开头），从root开始
				Directory startingDir = pathComponents[0].isEmpty() ? root : currentDirectory;
				int       startIndex  = pathComponents[0].isEmpty() ? 1 : 0; // 跳过空的起始组件
				currentDirectory = createOrGetDirectory(startingDir, pathComponents, startIndex);
				return true;
			}
			return false;
		}
	}

	// 获取当前目录路径
	public String getCurrentDirectoryPath() {
		StringBuilder path = new StringBuilder();
		Directory     temp = currentDirectory;
		while (temp != null) {
			path.insert(0, temp.name);
			temp = temp.parent;
			if (temp != null) path.insert(0, "/");
		}
		return path.toString();
	}

	@Override public List<String> getDirectories(String path) {
		if (ftpClient == null) {return Collections.emptyList();}
		try {
			ftpClient.machineListDictionary(path);
			changeDirectory(path);
			return new ArrayList<>(currentDirectory.directories.keySet());
		} catch (IOException e) {
			logger.error("Failed to list directories", e);
			return Collections.emptyList();
		}
	}

	@Override public List<File> getFiles(String path) {
		if (ftpClient == null) {return Collections.emptyList();}
		try {
			ftpClient.machineListDictionary(path);
			changeDirectory(path);
			return new ArrayList<>(currentDirectory.files.values());
		} catch (IOException e) {
			logger.error("Failed to list directories", e);
			return Collections.emptyList();
		}
	}

	@Override
	public boolean isDirectory(String path) {
		// 根目录总是存在且是一个目录
		if ("/".equals(path) || path.isEmpty()) {
			return true;
		}

		String[]  pathComponents = path.split("/");
		Directory dir            = root; // 从根目录开始遍历

		// 遍历路径的每个组成部分，查找对应的目录
		for (String component : pathComponents) {
			if (!component.isEmpty()) { // 忽略空字符串（路径开头可能会有斜杠）
				if (dir.directories.containsKey(component)) {
					dir = dir.directories.get(component); // 进入下一级目录
				} else {
					return false; // 路径中的一部分不存在，或者不是一个目录
				}
			}
		}

		return true; // 成功遍历完整个路径，且每一部分都存在，因此这是一个目录
	}
}
