package cn.jsou.ftpclient.vfs;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NativeFileSystemProvider implements FileSystemProvider {
	@Override
	public List<String> getDirectories(String path) {
		java.io.File file = new File(path);
		return Arrays.stream(file.listFiles(File::isDirectory))
		             .map(File::getName)
		             .collect(Collectors.toList());
	}

	@Override
	public List<cn.jsou.ftpclient.vfs.File> getFiles(String path) {
		java.io.File file = new File(path);
		return Arrays.stream(file.listFiles(File::isFile))
		             .map(f -> new cn.jsou.ftpclient.vfs.File(f.getName(), f.length()))
		             .collect(Collectors.toList());
	}
}