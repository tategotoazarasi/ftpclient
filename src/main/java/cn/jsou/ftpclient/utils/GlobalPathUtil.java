package cn.jsou.ftpclient.utils;

import java.io.File;

public class GlobalPathUtil {
	public static String toUnixPath(String path) {
		return path.replace(File.separatorChar, '/');
	}
}
