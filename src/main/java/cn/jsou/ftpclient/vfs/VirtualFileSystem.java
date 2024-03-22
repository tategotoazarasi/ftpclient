package cn.jsou.ftpclient.vfs;

import java.util.Map;

public class VirtualFileSystem {
	private final Directory root; // 根目录
	private       Directory currentDirectory; // 当前目录

	public VirtualFileSystem() {
		this.root             = new Directory("/");
		this.currentDirectory = root;
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
}
