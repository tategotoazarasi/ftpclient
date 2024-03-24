package cn.jsou.ftpclient.ui;

import cn.jsou.ftpclient.ftp.FtpClient;
import cn.jsou.ftpclient.vfs.NativeFileSystemProvider;
import cn.jsou.ftpclient.vfs.VirtualFileSystem;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
	private JTextField serverAddressField;

	private JTextField            portField;
	private JTextField            usernameField;
	private JPasswordField        passwordField;
	private JButton               connectButton;
	// 使用FileExplorerComponent替代原来的JList
	private FileExplorerComponent localFileExplorer;
	private FileExplorerComponent serverFileExplorer;

	public MainFrame() {
		setTitle("Java FTP Client");
		setSize(800, 600);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		initUI();
	}

	private void initUI() {
		// Create the top panel for server address, username, password, and connect button
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		serverAddressField = new JTextField(15);
		portField          = new JTextField(5);
		usernameField      = new JTextField(10);
		passwordField      = new JPasswordField(10);
		connectButton      = new JButton("Connect");

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
		localFileExplorer  = new FileExplorerComponent(new NativeFileSystemProvider(), "/");
		serverFileExplorer = new FileExplorerComponent(new VirtualFileSystem(), "/");

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
			FtpClient ftpClient    = new FtpClient(server, port); // 假设FtpClient构造函数接受服务器地址
			boolean   loginSuccess = ftpClient.login(username, password);

			if (loginSuccess) {
				JOptionPane.showMessageDialog(this, "登录成功！", "登录", JOptionPane.INFORMATION_MESSAGE);
				// 登录成功后的操作，例如更新界面显示服务器文件列表
			} else {
				JOptionPane.showMessageDialog(this,
				                              "登录失败：用户名或密码错误。",
				                              "登录失败",
				                              JOptionPane.ERROR_MESSAGE);
			}
			ftpClient.init();
			ftpClient.dataServer.waitHandlerComplete();
			serverFileExplorer.setFileSystemProvider(ftpClient.remoteFs);
			serverFileExplorer.updateFileList(ftpClient.remoteFs.getCurrentDirectoryPath());
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "登录失败：" + ex.getMessage(), "登录失败", JOptionPane.ERROR_MESSAGE);
		}
	}
}
