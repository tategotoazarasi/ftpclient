package cn.jsou.ftpclient.vfs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
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
			             try {
				             BasicFileAttributes
						             attrs =
						             Files.readAttributes(f.toPath(), BasicFileAttributes.class);
				             LocalDateTime
						             modifiedTime =
						             attrs.lastModifiedTime()
						                  .toInstant()
						                  .atZone(ZoneId.systemDefault())
						                  .toLocalDateTime();
				             LocalDateTime
						             creationTime =
						             attrs.creationTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				             return new cn.jsou.ftpclient.vfs.File(f.getName(),
				                                                   f.length(), modifiedTime, creationTime);
			             } catch (IOException e) {
				             logger.error("Failed to read file attributes", e);
				             return null;
			             }
		             })
		             .collect(Collectors.toList());
	}

	@Override
	public boolean isDirectory(String path) {
		java.io.File file = new java.io.File(path);
		return file.isDirectory();
	}

	@Override public void refresh() {
		// do nothing
	}

	@Override
	public void mkDir(String path) {
		File directory = new File(path);
		if (!directory.exists()) {
			boolean created = directory.mkdirs();
			if (!created) {
				logger.error("Failed to create directory: {}", path);
			}
		}
	}
}