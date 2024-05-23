package cn.jsou.ftpclient.ftp;

import cn.jsou.ftpclient.ftp.handlers.ConnectionHandler;
import cn.jsou.ftpclient.ftp.handlers.MLSDHandler;
import cn.jsou.ftpclient.ftp.handlers.RETRHandler;
import cn.jsou.ftpclient.ftp.handlers.STORHandler;
import cn.jsou.ftpclient.vfs.VirtualFileSystem;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FTP客户端类，用于建立和管理FTP连接，以及执行FTP命令
 */
public class FtpClient {
	private static final Logger            logger     = LogManager.getLogger(FtpClient.class);
	/**
	 * 服务器信息，包括系统信息和支持的特性
	 */
	public final         ServerInfo        serverInfo = new ServerInfo();
	/**
	 * 与FTP服务器的控制连接套接字
	 */
	private final        Socket            serverSocket;
	/**
	 * 用于发送FTP命令和接收响应的工具类
	 */
	private final        FtpCommands       ftpCommands;
	/**
	 * 数据服务器运行的线程
	 */
	private final        Thread            serverThread;
	/**
	 * 数据服务器，用于处理数据连接
	 */
	public               DataServer        dataServer;
	/**
	 * 已登录用户的用户名
	 */
	public               String            username;
	/**
	 * 远程虚拟文件系统，用于管理FTP服务器上的文件系统
	 */
	public               VirtualFileSystem remoteFs   = new VirtualFileSystem(this);

	/**
	 * 构造函数，初始化FTP客户端
	 *
	 * @param server FTP服务器的地址
	 * @param port   FTP服务器的端口号
	 *
	 * @throws IOException 如果无法建立与FTP服务器的连接
	 */
	public FtpClient(String server, String port) throws IOException {
		this.serverSocket = new Socket(server, Integer.parseInt(port));
		this.ftpCommands  = new FtpCommands(serverSocket);
		this.dataServer   = new DataServer(serverSocket.getLocalAddress());

		// 在新线程中运行DataServer
		this.serverThread = new Thread(dataServer);
		this.serverThread.start();

		// 处理服务器的欢迎信息
		ftpCommands.readResponse();
	}

	/**
	 * 登录FTP服务器
	 *
	 * @param username 用户名
	 * @param password 密码
	 *
	 * @return 如果登录成功，返回true；否则返回false
	 *
	 * @throws IOException 如果发送登录命令或读取响应时出现IO异常
	 */
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

	/**
	 * 初始化客户端，查询服务器系统信息、支持的特性等
	 *
	 * @throws IOException 如果发送命令或读取响应时出现IO异常
	 */
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

	/**
	 * 使用MLSD命令获取指定目录的详细列表，并更新远程虚拟文件系统
	 *
	 * @param name 目录的绝对路径
	 *
	 * @return 如果成功获取目录列表，返回true；否则返回false
	 *
	 * @throws IOException IOException 如果发送MLSD命令或读取响应时出现IO异常
	 */
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

	/**
	 * 上传文件到FTP服务器
	 *
	 * @param file 要上传的本地文件
	 *
	 * @return 如果文件上传成功，返回true；否则返回false
	 */
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

	/**
	 * 上传目录到FTP服务器
	 *
	 * @param file 要上传的本地目录
	 *
	 * @return 如果目录上传成功，返回true；否则返回false
	 */
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

	/**
	 * 从FTP服务器下载文件
	 *
	 * @param filename filename 要下载的文件名
	 * @param file     本地文件的存储位置
	 *
	 * @return 如果文件下载成功，返回true；否则返回false
	 */
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

	/**
	 * 从FTP服务器下载目录
	 *
	 * @param dirname 要下载的目录名
	 * @param file    本地目录的存储位置
	 *
	 * @return 如果目录下载成功，返回true；否则返回false
	 */
	public boolean downloadDirectory(String dirname, java.io.File file) {
		String current = remoteFs.getCurrentDirectoryPath();
		try {
			if (!file.exists()) {
				file.mkdir();
			}
			Response cwdResp = ftpCommands.changeWorkingDirectory(dirname);
			if (!cwdResp.isSuccess()) {
				logger.error("Failed to change working directory to {} with reply code: {}",
				             dirname,
				             cwdResp.getReplyCode());
				return false;
			}
			machineListDictionary(dirname);
			remoteFs.changeDirectory(remoteFs.getCurrentDirectoryPath() + File.separator + dirname);
			for (var f : remoteFs.getFiles(remoteFs.getCurrentDirectoryPath())) {
				java.io.File newFile = new java.io.File(file.getAbsolutePath() + File.separator + f.getName());
				downloadFile(f.getName(), newFile);
				dataServer.waitHandlerComplete();
			}
			for (var d : remoteFs.getDirectories(remoteFs.getCurrentDirectoryPath())) {
				java.io.File newFile = new java.io.File(file.getAbsolutePath() + File.separator + d);
				newFile.mkdir();
				downloadDirectory(d, newFile);
			}
		} catch (Exception e) {
			logger.error("Failed to download directory", e);
		} finally {
			remoteFs.changeDirectory(current);
		}

		return true;
	}

	/**
	 * 重命名文件或目录
	 *
	 * @param oldPathname 旧文件名（相对路径）
	 * @param newFilename 新文件名（相对路径）
	 */
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

	/**
	 * 删除文件或目录
	 *
	 * @param pathname 文件或目录的绝对路径
	 */
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

	/**
	 * 创建目录
	 *
	 * @param pathname 目录的路径
	 */
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

	/**
	 * 登出FTP服务器
	 */
	public void logout() {
		try {
			ftpCommands.logout();
		} catch (IOException e) {
			logger.error("Failed to logout", e);
		}
	}

	/**
	 * 关闭FTP客户端
	 */
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