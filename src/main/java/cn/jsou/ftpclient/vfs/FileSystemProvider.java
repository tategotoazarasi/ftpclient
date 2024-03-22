package cn.jsou.ftpclient.vfs;

import java.util.List;

public interface FileSystemProvider {
	List<String> getDirectories(String path);

	List<File> getFiles(String path);
}