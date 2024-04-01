package cn.jsou.ftpclient.vfs;

import java.util.List;

/**
 * 文件系统提供者接口，定义了文件系统操作的基本方法
 */
public interface FileSystemProvider {
	/**
	 * 获取指定路径下的所有目录
	 *
	 * @param path 要查询的绝对路径
	 *
	 * @return 目录名称列表
	 */
	List<String> getDirectories(String path);

	/**
	 * 获取指定路径下的所有文件
	 *
	 * @param path 要查询的绝对路径
	 *
	 * @return 文件对象列表
	 */
	List<File> getFiles(String path);

	/**
	 * 判断给定的路径是否为目录
	 *
	 * @param path 要判断的绝对路径
	 *
	 * @return 如果路径是目录，返回true；否则返回false
	 */
	boolean isDirectory(String path);

	/**
	 * 刷新文件系统，重新加载目录和文件列表
	 */
	void refresh();

	/**
	 * 在指定路径下创建一个新的目录
	 *
	 * @param path 要创建目录的绝对路径
	 */
	void mkDir(String path);

	/**
	 * 重命名或移动文件或目录
	 *
	 * @param oldPathname 原始相对路径
	 * @param newFilename 新的文件或目录名称（相对路径）
	 */
	void rename(String oldPathname, String newFilename);

	/**
	 * 删除指定的文件或目录
	 *
	 * @param filepath 要删除的文件或目录的绝对路径
	 */
	void delete(String filepath);
}