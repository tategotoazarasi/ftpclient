package cn.jsou.ftpclient.ui;

import cn.jsou.ftpclient.ftp.FtpClient;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
	private JTextField serverAddressField;

	private JTextField     portField;
	private JTextField     usernameField;
	private JPasswordField passwordField;
	private JButton        connectButton;
	private JList<String>  localFileList;
	private JList<String>  serverFileList;

	public MainFrame() {
		setTitle("Java FTP Client");
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		localFileList  = new JList<>();
		serverFileList = new JList<>();

		// For demonstration, add some sample items to the lists
		localFileList.setListData(new String[]{"Local File 1", "Local File 2"});
		serverFileList.setListData(new String[]{"Server File 1", "Server File 2"});

		JScrollPane localScrollPane  = new JScrollPane(localFileList);
		JScrollPane serverScrollPane = new JScrollPane(serverFileList);
		localScrollPane.setBorder(BorderFactory.createTitledBorder("Local Files"));
		serverScrollPane.setBorder(BorderFactory.createTitledBorder("Server Files"));

		splitPane.setLeftComponent(localScrollPane);
		splitPane.setRightComponent(serverScrollPane);
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
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "登录失败：" + ex.getMessage(), "登录失败", JOptionPane.ERROR_MESSAGE);
		}
	}
}
