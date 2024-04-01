package cn.jsou.ftpclient.vfs;

import cn.jsou.ftpclient.ftp.FtpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 实现了虚拟文件系统的类，提供了操作远程FTP服务器上的文件和目录的方法
 */
public class VirtualFileSystem implements FileSystemProvider {
	private static final Logger    logger = LogManager.getLogger(VirtualFileSystem.class);
	/**
	 * 根目录对象
	 */
	private final        Directory root;
	/**
	 * FTP客户端实例，用于与远程服务器进行通信
	 */
	private final        FtpClient ftpClient;
	/**
	 * 当前工作目录
	 */
	private              Directory currentDirectory;

	/**
	 * 构造函数，初始化虚拟文件系统
	 *
	 * @param ftpClient FTP客户端实例
	 */
	public VirtualFileSystem(FtpClient ftpClient) {
		this.root             = new Directory("/");
		this.currentDirectory = root;
		this.ftpClient        = ftpClient;
	}

	/**
	 * 在当前目录下创建新目录
	 *
	 * @param name 新目录的名称
	 */
	public void createDirectory(String name) {
		currentDirectory.createDirectory(name);
	}

	/**
	 * 在当前目录下创建新文件
	 *
	 * @param name 文件名称
	 * @param size 文件大小
	 */
	public void createFile(String name, long size) {
		currentDirectory.createFile(name, size);
	}

	/**
	 * 在当前目录下创建新文件，并设置文件属性
	 *
	 * @param name     文件名称
	 * @param factsMap 文件属性映射表
	 */
	public void createFile(String name, Map<String, String> factsMap) {
		currentDirectory.createFile(name, factsMap);
	}

	/**
	 * 递归创建或获取目录
	 *
	 * @param current        当前目录
	 * @param pathComponents 路径组件
	 * @param index          当前路径组件的索引
	 *
	 * @return 目标目录对象
	 */
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

	/**
	 * 改变当前工作目录
	 *
	 * @param dir 目标目录对象
	 */
	public void changeDirectory(Directory dir) {
		this.currentDirectory = dir;
	}

	/**
	 * 改变当前工作目录到指定路径
	 *
	 * @param path 目标目录的绝对路径
	 *
	 * @return 如果切换成功返回true，否则返回false
	 */
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

	/**
	 * 获取当前目录的路径
	 *
	 * @return 当前目录的路径字符串
	 */
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

	/**
	 * 获取指定路径下的所有目录
	 *
	 * @param path 要查询的绝对路径
	 *
	 * @return 目录名称列表
	 */
	@Override public List<String> getDirectories(String path) {
		if (ftpClient == null) {return Collections.emptyList();}
		try {
			changeDirectory(path);
			currentDirectory.clear();
			ftpClient.machineListDictionary(path);
			return new ArrayList<>(currentDirectory.directories.keySet());
		} catch (IOException e) {
			logger.error("Failed to list directories", e);
			return Collections.emptyList();
		}
	}

	/**
	 * 获取指定路径下的所有文件
	 *
	 * @param path 要查询的绝对路径
	 *
	 * @return 文件对象列表
	 */
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

	/**
	 * 判断给定的路径是否为目录
	 *
	 * @param path 要判断的绝对路径
	 *
	 * @return 如果路径是目录，返回true；否则返回false
	 */
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

	/**
	 * 刷新文件系统，重新加载目录和文件列表
	 */
	@Override
	public void refresh() {
		if (ftpClient == null) {
			return;
		}
		try {
			String currentPath = getCurrentDirectoryPath();
			ftpClient.machineListDictionary(currentPath);
			changeDirectory(currentPath);
		} catch (IOException e) {
			logger.error("Failed to refresh the directory", e);
		}
	}

	/**
	 * 在指定路径下创建一个新的目录
	 *
	 * @param path 要创建目录的绝对路径
	 */
	@Override
	public void mkDir(String path) {
		if (!isDirectory(path)) {
			String[]  pathComponents = path.split("/");
			Directory currentDir     = root;

			for (int i = 1; i < pathComponents.length; i++) {
				String component = pathComponents[i];
				if (!component.isEmpty()) {
					if (!currentDir.directories.containsKey(component)) {
						currentDir.createDirectory(component);
					}
					currentDir = currentDir.directories.get(component);
				}
			}
			ftpClient.makeDirectory(path);
		}
	}

	/**
	 * 重命名或移动文件或目录
	 *
	 * @param oldPathname 原始相对路径
	 * @param newFilename 新的文件或目录名称（相对路径）
	 */
	@Override public void rename(String oldPathname, String newFilename) {
		ftpClient.rename(oldPathname, newFilename);
	}

	/**
	 * 删除指定的文件或目录
	 *
	 * @param filepath 要删除的文件或目录的绝对路径
	 */
	@Override public void delete(String filepath) {
		ftpClient.delete(filepath);
	}
}
