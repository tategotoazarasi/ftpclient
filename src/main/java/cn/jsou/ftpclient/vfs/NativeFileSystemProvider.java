package cn.jsou.ftpclient.vfs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NativeFileSystemProvider implements FileSystemProvider {
	private static final Logger logger = LogManager.getLogger(NativeFileSystemProvider.class);

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
		             .map(f -> {
			             return new cn.jsou.ftpclient.vfs.File(f.getName(),
			                                                   f.length(),
			                                                   Instant.ofEpochMilli(f.lastModified())
			                                                          .atZone(ZoneId.systemDefault())
			                                                          .toLocalDateTime());
		             })
		             .collect(Collectors.toList());
	}
}