package cn.jsou.ftpclient.ui;

import cn.jsou.ftpclient.ftp.FtpClient;
import cn.jsou.ftpclient.vfs.NativeFileSystemProvider;
import cn.jsou.ftpclient.vfs.VirtualFileSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 主窗口类，提供了Java FTP客户端的图形用户界面
 */
public class MainFrame extends JFrame {
	private static final Logger                logger = LogManager.getLogger(MainFrame.class);
	/**
	 * 服务器地址输入字段
	 */
	private              JTextField            serverAddressField;
	/**
	 * 端口号输入字段
	 */
	private              JTextField            portField;
	/**
	 * 用户名输入字段
	 */
	private              JTextField            usernameField;
	/**
	 * 密码输入字段
	 */
	private              JPasswordField        passwordField;
	/**
	 * 连接按钮
	 */
	private              JButton               connectButton;
	/**
	 * 本地文件浏览组件
	 */
	private              FileExplorerComponent localFileExplorer;
	/**
	 * 服务器文件浏览组件
	 */
	private              FileExplorerComponent serverFileExplorer;
	/**
	 * FTP客户端实例
	 */
	private              FtpClient             ftpClient;

	/**
	 * 构造函数，初始化主窗口
	 */
	public MainFrame() {
		setTitle("Java FTP Client");
		setSize(800, 600);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		initUI();
		// 添加窗口监听器以处理窗口关闭事件
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// 在这里调用 ftpClient.logout()
				if (ftpClient != null) {
					ftpClient.logout();
					ftpClient.close();
				}
				System.exit(0); // 确保应用程序完全退出
			}
		});
	}

	/**
	 * 初始化用户界面组件
	 */
	private void initUI() {
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		serverAddressField = new JTextField(15);
		portField          = new JTextField(5);
		usernameField      = new JTextField(10);
		passwordField      = new JPasswordField(10);
		connectButton      = new JButton("Connect");

		//DEBUG
		serverAddressField.setText("172.17.0.2");
		portField.setText("21");
		usernameField.setText("user");
		passwordField.setText("password");
		//DEBUG

		connectButton.addActionListener(e -> initFtp());

		topPanel.add(new JLabel("服务器地址:"));
		topPanel.add(serverAddressField);
		topPanel.add(new JLabel("端口:"));
		topPanel.add(portField);
		topPanel.add(new JLabel("用户名:"));
		topPanel.add(usernameField);
		topPanel.add(new JLabel("密码:"));
		topPanel.add(passwordField);
		topPanel.add(connectButton);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		// 初始化本地和服务器的FileExplorerComponent实例
		localFileExplorer  =
				new FileExplorerComponent(new NativeFileSystemProvider(), System.getProperty("user.home"), false);
		serverFileExplorer = new FileExplorerComponent(new VirtualFileSystem(null), "/", true);
		localFileExplorer.setPeer(serverFileExplorer);
		serverFileExplorer.setPeer(localFileExplorer);

		splitPane.setLeftComponent(localFileExplorer);
		splitPane.setRightComponent(serverFileExplorer);
		splitPane.setDividerLocation(400);

		// Add top panel and split pane to the frame
		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(splitPane, BorderLayout.CENTER);
	}

	/**
	 * 初始化FTP客户端，连接到服务器并尝试登录
	 */
	private void initFtp() {
		String server   = serverAddressField.getText();
		String port     = portField.getText();
		String username = usernameField.getText();
		String password = new String(passwordField.getPassword());

		try {
			ftpClient = new FtpClient(server, port); // 假设FtpClient构造函数接受服务器地址
			boolean loginSuccess = ftpClient.login(username, password);

			if (loginSuccess) {
				JOptionPane.showMessageDialog(this, "登录成功！", "登录", JOptionPane.INFORMATION_MESSAGE);
				// 登录成功后的操作，例如更新界面显示服务器文件列表
				ftpClient.init();
				ftpClient.dataServer.waitHandlerComplete();
				serverFileExplorer.setFileSystemProvider(ftpClient.remoteFs);
				serverFileExplorer.updateFileList(ftpClient.remoteFs.getCurrentDirectoryPath());
				localFileExplorer.setFtpClient(ftpClient);
				serverFileExplorer.setFtpClient(ftpClient);
			} else {
				JOptionPane.showMessageDialog(this,
				                              "登录失败：用户名或密码错误。",
				                              "登录失败",
				                              JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception ex) {
			logger.error("错误：{}", ex.getMessage());
			JOptionPane.showMessageDialog(this, "错误：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}
	}
}
