package cn.jsou.ftpclient.utils;

import java.io.File;

public class GlobalPathUtil {
	public static String toUnixPath(String path) {
		return path.replace(File.separatorChar, '/');
	}

	public static String normalizePath(String path) {
		if (path == null) {
			return null;
		}

		// Step 1: Replace all backslashes with forward slashes
		String normalizedPath = path.replace("\\", "/");

		// Step 2: Replace multiple consecutive slashes with a single slash
		normalizedPath = normalizedPath.replaceAll("/+", "/");

		return normalizedPath;
	}
}
