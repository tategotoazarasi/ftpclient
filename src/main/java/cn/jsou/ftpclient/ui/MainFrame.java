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

public class MainFrame extends JFrame {
	private static final Logger     logger = LogManager.getLogger(MainFrame.class);
	private              JTextField serverAddressField;

	private JTextField            portField;
	private JTextField            usernameField;
	private JPasswordField        passwordField;
	private JButton               connectButton;
	// 使用FileExplorerComponent替代原来的JList
	private FileExplorerComponent localFileExplorer;
	private FileExplorerComponent serverFileExplorer;
	private FtpClient ftpClient;

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

	private void initUI() {
		// Create the top panel for server address, username, password, and connect button
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

		connectButton.addActionListener(e -> connectToFtp());

		topPanel.add(new JLabel("服务器地址:"));
		topPanel.add(serverAddressField);
		topPanel.add(new JLabel("端口:"));
		topPanel.add(portField);
		topPanel.add(new JLabel("用户名:"));
		topPanel.add(usernameField);
		topPanel.add(new JLabel("密码:"));
		topPanel.add(passwordField);
		topPanel.add(connectButton);

		// Create the main split pane to show local and server files
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		// 初始化本地和服务器的FileExplorerComponent实例
		localFileExplorer  =
				new FileExplorerComponent(new NativeFileSystemProvider(), System.getProperty("user.home"), false, this);
		serverFileExplorer = new FileExplorerComponent(new VirtualFileSystem(null), "/", true, this);
		localFileExplorer.setPeer(serverFileExplorer);
		serverFileExplorer.setPeer(localFileExplorer);

		splitPane.setLeftComponent(localFileExplorer);
		splitPane.setRightComponent(serverFileExplorer);
		splitPane.setDividerLocation(400);

		// Add top panel and split pane to the frame
		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(splitPane, BorderLayout.CENTER);
	}

	private void connectToFtp() {
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
