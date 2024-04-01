package cn.jsou.ftpclient.ftp;

import cn.jsou.ftpclient.ftp.handlers.ConnectionHandler;
import cn.jsou.ftpclient.ftp.handlers.MLSDHandler;
import cn.jsou.ftpclient.ftp.handlers.RETRHandler;
import cn.jsou.ftpclient.ftp.handlers.STORHandler;
import cn.jsou.ftpclient.vfs.VirtualFileSystem;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FtpClient {
	private static final Logger            logger     = LogManager.getLogger(FtpClient.class);
	public final         ServerInfo        serverInfo = new ServerInfo();
	private final        String            server;
	private final        Socket            serverSocket;
	private final        FtpCommands       ftpCommands;
	private final        Thread            serverThread;
	public DataServer        dataServer;
	public               String            username;
	public VirtualFileSystem remoteFs = new VirtualFileSystem(this);

	public FtpClient(String server, String port) throws IOException {
		this.server       = server;
		this.serverSocket = new Socket(server, Integer.parseInt(port));
		this.ftpCommands  = new FtpCommands(serverSocket);
		this.dataServer   = new DataServer(serverSocket.getLocalAddress());

		// 在新线程中运行DataServer
		this.serverThread = new Thread(dataServer);
		this.serverThread.start();

		// 处理服务器的欢迎信息
		ftpCommands.readResponse();
	}

	// 登录方法
	public boolean login(String username, String password) throws IOException {
		Response userResp = ftpCommands.userName(username);
		if (!userResp.isSuccess()) {
			return false;
		}
		Response passResp = ftpCommands.password(password);
		if (!passResp.isSuccess()) {
			return false;
		}
		this.username = username;
		return true;
	}

	public void init() throws IOException {
		Response sysResp = ftpCommands.system();
		if (sysResp.isSuccess()) {
			serverInfo.setSysInfo(sysResp.getMessage());
		}

		Response featResp = ftpCommands.features();
		if (featResp.isSuccess()) {
			// 使用正则表达式分割字符串，匹配所有连续的换行符及其前后的连续空白字符
			String[] features = featResp.getMessage().split("\\s*\\n+\\s*");
			// 检查features数组长度是否大于2，确保至少存在一个特性（除去第一个和最后一个元素）
			if (features.length > 2) {
				// 使用Stream API从features数组中去除第一个和最后一个元素，并去除每个特性字符串前后的连续空白
				Arrays.stream(features, 1, features.length - 1) // 跳过第一个和最后一个元素
				      .map(String::trim) // 去除前后的连续空白
				      .forEach(serverInfo::addFeature); // 加入到serverInfo中
			}
		}

		if (serverInfo.hasFeature("UTF8")) {
			Response optsResp = ftpCommands.options("UTF8", "ON");
			if (!optsResp.isSuccess()) {
				logger.warn("Failed to enable UTF-8 support with reply code {}", optsResp.getReplyCode());
			}
		}

		Response pwdResp = ftpCommands.printWorkingDirectory();
		if (pwdResp.isSuccess()) {
			String message = pwdResp.getMessage().trim();
			// 使用正则表达式提取被引号包裹的路径
			Pattern pattern = Pattern.compile("\"([^\"]*)\"");
			Matcher matcher = pattern.matcher(message);
			if (matcher.find()) {
				String path = matcher.group(1); // 获取第一个匹配的组，即被引号包裹的内容
				remoteFs.changeDirectory(path);
			}
		}

		Response typeResp = ftpCommands.representationType(TypeCode.IMAGE);
		if (!typeResp.isSuccess()) {
			logger.warn("Failed to set representation type to IMAGE with reply code: {}", typeResp.getReplyCode());
		}

		Response portResp = ftpCommands.dataPort(dataServer.serverSocket);
		if (!portResp.isSuccess()) {
			logger.warn("Failed to set data port with reply code: {}", portResp.getReplyCode());
		}

		machineListDictionary(remoteFs.getCurrentDirectoryPath());
	}

	public boolean machineListDictionary(String name) throws IOException {
		Response cwdResp = ftpCommands.changeWorkingDirectory(name);
		if (!cwdResp.isSuccess()) {
			logger.warn("Failed to change working directory to {} with reply code: {}", name, cwdResp.getReplyCode());
		} else {
			String tmp = remoteFs.getCurrentDirectoryPath();
			remoteFs.changeDirectory(name);
			Response portResp = ftpCommands.dataPort(dataServer.serverSocket);
			if (!portResp.isSuccess()) {
				logger.warn("Failed to set data port with reply code: {}", portResp.getReplyCode());
			}
			if (serverInfo.hasFeature("MLSD")) {
				//dataServer = new DataServer(serverSocket.getLocalAddress());
				ConnectionHandler ch = new MLSDHandler(remoteFs);
				dataServer.setConnectionHandler(ch);
				Response mlsdResp = ftpCommands.machineListDictionary();
				if (!mlsdResp.isSuccess()) {
					logger.warn("Failed to enable MLSD support with reply code: {}", mlsdResp.getReplyCode());
					remoteFs.createDirectory(tmp);
					return false;
				}
				try {
					ch.waitForCompletion();
				} catch (InterruptedException e) {
					logger.error("Failed to wait for data server to complete", e);
				}
				return true;
			}
		}
		return false;
	}

	public boolean uploadFile(java.io.File file) {
		try {
			Response portResp = ftpCommands.dataPort(dataServer.serverSocket);
			if (!portResp.isSuccess()) {
				logger.error("Failed to set data port with reply code: {}", portResp.getReplyCode());
				return false;
			}
			ConnectionHandler ch = new STORHandler(file);
			dataServer.setConnectionHandler(ch);
			Response storResp = ftpCommands.store(file.getName());
			if (!storResp.isSuccess()) {
				logger.error("Failed to store file with reply code: {}", storResp.getReplyCode());
				return false;
			}
		} catch (IOException e) {
			logger.error("Failed to upload file", e);
			return false;
		}
		return true;
	}

	public boolean uploadDirectory(java.io.File file) {
		String current = remoteFs.getCurrentDirectoryPath();
		try {
			if (!file.isDirectory()) {
				return false;
			}
			makeDirectory(file.getName());
			Response cwdResp = ftpCommands.changeWorkingDirectory(file.getName());
			if (!cwdResp.isSuccess()) {
				logger.error("Failed to change working directory to {} with reply code: {}",
				             file.getName(),
				             cwdResp.getReplyCode());
				return false;
			}
			remoteFs.changeDirectory(remoteFs.getCurrentDirectoryPath() + '/' + file.getName());
			for (java.io.File f : Objects.requireNonNull(file.listFiles())) {
				if (f.isDirectory()) {
					uploadDirectory(f);
				} else {
					uploadFile(f);
					dataServer.waitHandlerComplete();
				}
			}
			return true;
		} catch (Exception e) {
			logger.error("Failed to upload directory", e);
			return false;
		} finally {
			remoteFs.changeDirectory(current);
		}
	}

	public boolean downloadFile(String filename, java.io.File file) {
		try {
			Response portResp = ftpCommands.dataPort(dataServer.serverSocket);
			if (!portResp.isSuccess()) {
				logger.error("Failed to set data port with reply code: {}", portResp.getReplyCode());
				return false;
			}
			ConnectionHandler ch = new RETRHandler(file);
			dataServer.setConnectionHandler(ch);
			Response retrResp = ftpCommands.retrieve(filename);
			if (!retrResp.isSuccess()) {
				logger.error("Failed to retrieve file with reply code: {}", retrResp.getReplyCode());
				return false;
			}
		} catch (IOException e) {
			logger.error("Failed to download file", e);
			return false;
		}
		return true;
	}

	public void rename(String oldPathname, String newFilename) {
		try {
			Response renameResp = ftpCommands.renameFrom(oldPathname);
			if (!renameResp.isSuccess()) {
				logger.error("Failed to rename file with reply code: {}", renameResp.getReplyCode());
			}
			Response renameToResp = ftpCommands.renameTo(newFilename);
			if (!renameToResp.isSuccess()) {
				logger.error("Failed to rename file with reply code: {}", renameToResp.getReplyCode());
			}
		} catch (IOException e) {
			logger.error("Failed to rename file", e);
		}
	}

	public void delete(String pathname) {
		try {
			if (remoteFs.isDirectory(pathname)) {
				remoteFs.getDirectories(pathname).forEach(d -> delete(pathname + '/' + d));
				remoteFs.getFiles(pathname).forEach(f -> delete(pathname + '/' + f.getName()));
				Response rmdResp = ftpCommands.removeDirectory(pathname);
				if (!rmdResp.isSuccess()) {
					logger.error("Failed to remove directory with reply code: {}", rmdResp.getReplyCode());
				}
			} else {
				Response deleteResp = ftpCommands.delete(pathname);
				if (!deleteResp.isSuccess()) {
					logger.error("Failed to delete file with reply code: {}", deleteResp.getReplyCode());
				}
			}
		} catch (IOException e) {
			logger.error("Failed to delete file", e);
		}
	}

	public void makeDirectory(String pathname) {
		try {
			Response mkdResp = ftpCommands.makeDirectory(pathname);
			if (!mkdResp.isSuccess()) {
				logger.error("Failed to make directory with reply code: {}", mkdResp.getReplyCode());
			}
		} catch (IOException e) {
			logger.error("Failed to make directory", e);
		}
	}

	public void logout() {
		try {
			ftpCommands.logout();
		} catch (IOException e) {
			logger.error("Failed to logout", e);
		}
	}

	public void close() {
		try {
			ftpCommands.close();
			IOUtils.closeQuietly(serverSocket);
			dataServer.close();
			serverThread.join();
		} catch (InterruptedException e) {
			logger.error("Failed to close the FTP client", e);
		}
	}
}