package cn.jsou.ftpclient.ftp.handlers;

import cn.jsou.ftpclient.vfs.VirtualFileSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class MLSDHandler implements ConnectionHandler {
	private static final Logger logger = LogManager.getLogger(MLSDHandler.class);

	private final VirtualFileSystem vfs;

	public MLSDHandler(VirtualFileSystem vfs) {
		this.vfs = vfs;
	}

	@Override public void handleConnection(Socket socket) {
		try (InputStream inputStream = socket.getInputStream();
		     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				String[]            facts    = line.split(";");
				String              filename = facts[facts.length - 1].trim();
				Map<String, String> factsMap = new HashMap<>();
				for (String fact : facts) {
					int equalsIndex = fact.indexOf('=');
					if (equalsIndex != -1) {
						String key   = fact.substring(0, equalsIndex);
						String value = fact.substring(equalsIndex + 1);
						factsMap.put(key, value);
					}
				}
				if (factsMap.containsKey("type") && factsMap.get("type").equals("dir")) {
					vfs.createDirectory(filename);
				} else if (factsMap.containsKey("type") && factsMap.get("type").equals("file")) {
					vfs.createFile(filename, factsMap);
				}
			}
		} catch (IOException e) {
			logger.error("Error handling MLSD data connection", e);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				logger.error("Error closing data connection socket", e);
			}
		}
	}
}
