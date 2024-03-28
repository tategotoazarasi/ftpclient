package cn.jsou.ftpclient.vfs;

import java.util.List;

public interface FileSystemProvider {
	List<String> getDirectories(String path);

	List<File> getFiles(String path);

	boolean isDirectory(String path); // 判断路径是否为目录

	void refresh();

	void mkDir(String path);

	void rename(String oldPathname, String newFilename);

	void delete(String filepath);
}