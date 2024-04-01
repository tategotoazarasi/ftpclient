package cn.jsou.ftpclient.vfs;

import java.util.HashMap;
import java.util.Map;

/**
 * 表示一个目录，包含目录名、文件、子目录和父目录的引用
 */
public class Directory {
	/**
	 * 目录名称
	 */
	String                 name;
	/**
	 * 此目录下的文件映射，键为文件名，值为对应的File对象
	 */
	Map<String, File>      files       = new HashMap<>();
	/**
	 * 此目录下的子目录映射，键为目录名，值为对应的Directory对象
	 */
	Map<String, Directory> directories = new HashMap<>();
	/**
	 * 此目录的父目录引用
	 */
	Directory              parent;

	/**
	 * 构造函数，创建一个新的目录实例
	 *
	 * @param name 目录名称
	 */
	public Directory(String name) {
		this.name = name;
	}

	/**
	 * 在当前目录下创建新文件
	 *
	 * @param name 文件名称
	 * @param size 文件大小（字节）
	 */
	public void createFile(String name, long size) {
		files.put(name, new File(name, size));
	}

	/**
	 * 根据文件属性映射表在当前目录下创建新文件
	 *
	 * @param name     文件名称
	 * @param factsMap 包含文件属性的映射表
	 */
	public void createFile(String name, Map<String, String> factsMap) {
		files.put(name, new File(name, factsMap));
	}

	/**
	 * 在当前目录下创建新目录，并返回新创建的目录对象
	 *
	 * @param name 新目录的名称
	 *
	 * @return 新创建的目录对象
	 */
	public Directory createDirectory(String name) {
		Directory directory = new Directory(name);
		directories.put(name, directory);
		directory.parent = this;
		return directory;
	}

	/**
	 * 获取目录名称
	 *
	 * @return 目录名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 获取此目录下的所有文件
	 *
	 * @return 文件映射表
	 */
	public Map<String, File> getFiles() {
		return files;
	}

	/**
	 * 获取此目录下的所有子目录
	 *
	 * @return 子目录映射表
	 */
	public Map<String, Directory> getDirectories() {return directories;}

	/**
	 * 获取此目录的父目录
	 *
	 * @return 父目录
	 */
	public Directory getParent() {
		return parent;
	}

	/**
	 * 清空此目录下的所有文件和子目录
	 */
	public void clear() {
		files.clear();
		directories.clear();
	}
}