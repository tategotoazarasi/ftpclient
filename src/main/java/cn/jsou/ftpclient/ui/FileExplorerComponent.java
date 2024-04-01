package cn.jsou.ftpclient.ui;

import cn.jsou.ftpclient.ftp.FtpClient;
import cn.jsou.ftpclient.utils.TimeUtil;
import cn.jsou.ftpclient.vfs.File;
import cn.jsou.ftpclient.vfs.FileSystemProvider;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class FileExplorerComponent extends JPanel {
	private final MainFrame mainFrame;
	private final JLabel currentPathLabel = new JLabel(" ");
	JButton btnGoUp           = new JButton();
	JButton btnNewFolder      = new JButton();
	JButton btnDelete         = new JButton();
	JButton btnUploadDownload = new JButton();
	JButton btnRefresh        = new JButton();
	private FileSystemProvider    fileSystemProvider;
	private JTable                fileTable;
	private String                currentPath;
	private FtpClient             ftpClient;
	private boolean               isRemote = false;
	private FileExplorerComponent peer;

	public FileExplorerComponent(FileSystemProvider fileSystemProvider,
	                             String initialPath,
	                             boolean isRemote,
	                             MainFrame mainFrame) {
		this.fileSystemProvider = fileSystemProvider;
		this.currentPath        = initialPath;
		initUI();
		updateFileList(initialPath);
		this.isRemote  = isRemote;
		this.mainFrame = mainFrame;
	}

	private void initUI() {
		setLayout(new BorderLayout());

		// 创建北部面板，包括工具栏和路径标签
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());

		// 创建按钮并添加到工具栏
		JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

		btnGoUp.setIcon(SvgIconLoader.loadSvgIcon("/up-icon.svg", 24));
		btnGoUp.setToolTipText("上一级");
		btnNewFolder.setIcon(SvgIconLoader.loadSvgIcon("/new-folder-icon.svg", 24));
		btnNewFolder.setToolTipText("新建目录");
		btnDelete.setIcon(SvgIconLoader.loadSvgIcon("/delete-icon.svg", 24));
		btnDelete.setToolTipText("删除");
		btnUploadDownload.setIcon(SvgIconLoader.loadSvgIcon("/upload-download-icon.svg", 24));
		btnUploadDownload.setToolTipText("上传/下载");
		btnRefresh.setIcon(SvgIconLoader.loadSvgIcon("/refresh-icon.svg", 24));
		btnRefresh.setToolTipText("刷新");

		// 添加按钮到工具栏
		toolBar.add(btnGoUp);
		toolBar.add(btnNewFolder);
		toolBar.add(btnDelete);
		toolBar.add(btnUploadDownload);
		toolBar.add(btnRefresh);

		btnGoUp.addActionListener(e -> {
			if (currentPath != null && !currentPath.isEmpty()) {
				// 使用Path类处理路径
				Path currentDirectory = Paths.get(currentPath);
				Path parentDirectory  = currentDirectory.getParent(); // 获取父目录

				if (parentDirectory != null) {
					// 更新当前路径
					currentPath = parentDirectory.toString();
					// 刷新文件列表以显示父目录的内容
					updateFileList(currentPath);
				}
			}
		});
		btnNewFolder.addActionListener(e -> {
			String newFolderName = promptForName("输入新目录的名称:", "新建目录");
			if (newFolderName != null && !newFolderName.trim().isEmpty()) {
				// 检查名称合法性（这里简单检查，根据实际需求调整）
				if (newFolderName.contains("/") || newFolderName.contains("\\")) {
					JOptionPane.showMessageDialog(this, "目录名称不能包含 / 或 \\。", "错误", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// 构建新目录的完整路径
				Path newPath = Paths.get(currentPath, newFolderName);

				// 检查目录是否已存在
				if (fileSystemProvider.isDirectory(newPath.toString())) {
					JOptionPane.showMessageDialog(this, "目录已存在。", "错误", JOptionPane.ERROR_MESSAGE);
					return;
				}

				try {
					// 尝试创建新目录
					fileSystemProvider.mkDir(newPath.toString());
					// 刷新当前目录的视图
					updateFileList(currentPath);
				} catch (Exception ex) {
					// 处理创建目录过程中可能出现的异常
					JOptionPane.showMessageDialog(this,
					                              "创建目录失败: " + ex.getMessage(),
					                              "错误",
					                              JOptionPane.ERROR_MESSAGE);
				}
			} else if (newFolderName != null) {
				// 用户输入为空，但不是点击取消，给出提示
				JOptionPane.showMessageDialog(this, "目录名称不能为空。", "警告", JOptionPane.WARNING_MESSAGE);
			}
			// 如果用户点击取消，newFolderName将为null，这里不做处理即可
		});
		btnDelete.addActionListener(e -> {
			int[] selectedRows = fileTable.getSelectedRows();
			// 直接传递选中的行到公共方法中
			deleteSelectedItems(selectedRows);
		});
		btnUploadDownload.addActionListener(e -> {
			uploadDownloadSelectedFiles();
			peer.refresh();
		});
		btnRefresh.addActionListener(e -> refresh());

		// 将工具栏添加到北部面板的北部
		northPanel.add(toolBar, BorderLayout.NORTH);

		// 创建显示当前路径的面板
		JPanel pathPanel = new JPanel(new BorderLayout());
		pathPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // 添加一些边距
		currentPathLabel.setText(currentPath); // 初始时设置为当前路径
		pathPanel.add(currentPathLabel, BorderLayout.CENTER);
		// 将路径面板添加到北部面板的中部
		northPanel.add(pathPanel, BorderLayout.SOUTH);
		// 将北部面板添加到主面板的北部
		add(northPanel, BorderLayout.NORTH);

		fileTable = new JTable() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false; // 使表格不可编辑
			}
		};
		fileTable.setShowGrid(false); // 去掉网格线
		fileTable.setIntercellSpacing(new Dimension(0, 0)); // 去掉单元格间距
		JScrollPane scrollPane = new JScrollPane(fileTable);
		add(scrollPane, BorderLayout.CENTER);
		addTableMouseListener(); // 添加鼠标事件监听器

		// 在这里初始化右键菜单
		JPopupMenu popupMenu = createTablePopupMenu();
		fileTable.addMouseListener(new MouseAdapter() {
			// 保留双击事件的处理
			@Override
			public void mouseClicked(MouseEvent e) {
				// 双击事件处理代码...
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		fileTable.getSelectionModel().addListSelectionListener(e -> {
			// 当表格的选择状态改变时，更新按钮的启用/禁用状态
			if (!e.getValueIsAdjusting()) {
				updateButtonStates();
			}
		});

		updateButtonStates();
	}

	public void refresh() {
		updateFileList(currentPath);
	}

	public void updateFileList(String path) {
		String[] columnNames = {"Name", "Size", "Creation Time", "Modified Time"};
		DefaultTableModel model       = new DefaultTableModel(columnNames, 0);

		java.util.List<String> directories = fileSystemProvider.getDirectories(path);
		directories.forEach(dir -> model.addRow(new Object[]{dir, "", "", ""}));

		List<File> files = fileSystemProvider.getFiles(path);
		files.forEach(file -> model.addRow(new Object[]{
				file.getName(),
				FileUtils.byteCountToDisplaySize(file.getSize()),
				TimeUtil.formatRelativeTime(file.getCreatedTime()),
				TimeUtil.formatRelativeTime(file.getModifiedTime())
		}));

		fileTable.setModel(model);
		currentPath = path;
		// 在文件列表更新后，设置标签以显示新的当前路径
		currentPathLabel.setText(path);

		fileTable.getColumnModel().getColumn(0).setCellRenderer(new FileCellRenderer());

		// 创建行排序器并为表格设置行排序器
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(fileTable.getModel());
		fileTable.setRowSorter(sorter);
		fileTable.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int col = fileTable.columnAtPoint(e.getPoint());
				if (col >= 0) {
					// 检查当前列的排序状态
					List<? extends RowSorter.SortKey> sortKeys = sorter.getSortKeys();
					if (!sortKeys.isEmpty() && sortKeys.get(0).getColumn() == col) {
						// 如果当前列已经是主排序键，反转排序顺序
						boolean ascending = sortKeys.get(0).getSortOrder() == SortOrder.ASCENDING;
						sorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(col,
						                                                                   ascending ?
						                                                                   SortOrder.DESCENDING :
						                                                                   SortOrder.ASCENDING)));
					} else {
						// 否则，设置为升序排序
						sorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(col, SortOrder.ASCENDING)));
					}
				}
			}
		});

		this.repaint();
	}

	private void addTableMouseListener() {
		fileTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) { // 双击事件
					int viewRow = fileTable.rowAtPoint(e.getPoint());
					if (viewRow >= 0) {
						int    modelRow = fileTable.convertRowIndexToModel(viewRow); // 转换为模型中的索引
						String name     = (String) fileTable.getModel().getValueAt(modelRow, 0); // 使用模型索引获取数据
						String newPath  = currentPath + (currentPath.endsWith("/") ? "" : '/') + name;
						if (fileSystemProvider.isDirectory(newPath)) {
							updateFileList(newPath);
							currentPath = newPath;
						}
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				showPopupMenu(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				showPopupMenu(e);
			}

			private void showPopupMenu(MouseEvent e) {
				if (e.isPopupTrigger()) {
					// 在弹出菜单之前，确保点击的行被选中
					int rowAtPoint = fileTable.rowAtPoint(e.getPoint());
					if (rowAtPoint > -1 && rowAtPoint < fileTable.getRowCount()) {
						fileTable.setRowSelectionInterval(rowAtPoint, rowAtPoint);
					} else {
						fileTable.clearSelection();
					}

					// 获取弹出菜单并显示
					JPopupMenu popupMenu = createTablePopupMenu();
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}

	private String promptForName(String message, String title) {
		return JOptionPane.showInputDialog(this, message, title, JOptionPane.PLAIN_MESSAGE);
	}

	public void setPeer(FileExplorerComponent peer) {
		this.peer = peer;
	}

	private void updateButtonStates() {
		// 检查表格中是否有选中的行
		boolean isRowSelected = fileTable.getSelectedRow() != -1;

		// 根据是否有行被选中来启用或禁用按钮
		btnUploadDownload.setEnabled(isRowSelected);
		btnDelete.setEnabled(isRowSelected);
	}

	public void setFtpClient(FtpClient client) {
		this.ftpClient = client;
	}

	public void setFileSystemProvider(FileSystemProvider fsp) {
		this.fileSystemProvider = fsp;
	}

	public String getCurrentPath() {
		return currentPath;
	}

	private void uploadDownloadSelectedFiles() {
		if (ftpClient == null) {
			JOptionPane.showMessageDialog(this, "请先连接到FTP服务器", "错误", JOptionPane.ERROR_MESSAGE);
			return;
		}

		int[] selectedRows = fileTable.getSelectedRows();
		for (int viewRowIndex : selectedRows) {
			int    modelRowIndex = fileTable.convertRowIndexToModel(viewRowIndex);
			String fileName      = (String) fileTable.getModel().getValueAt(modelRowIndex, 0);

			// Construct the path for the file or directory to be uploaded or downloaded
			Path filePath = Paths.get(currentPath, fileName);

			// Check if the selected item is a directory or a file
			if (!isRemote) {
				java.io.File localFile = filePath.toFile();
				if (localFile.isDirectory()) {
					// Upload the directory if isRemote is false and the selected item is a directory
					uploadDirectory(localFile);
				} else {
					// Otherwise, upload the file as usual
					uploadFile(fileName);
				}
			} else {
				// For downloading (when isRemote is true), handle as in the original method
				downloadFile(fileName);
			}
		}
	}

	private void uploadDirectory(java.io.File directory) {
		// Assume that the existence of the directory and the decision to overwrite are handled elsewhere

		// Perform the directory upload operation
		boolean success = ftpClient.uploadDirectory(directory);

		// Show the result of the directory upload
		showTransferResult(success, "上传目录", directory.getName());
	}

	private void uploadFile(String fileName) {
		Path         filePath     = Paths.get(currentPath, fileName);
		java.io.File fileToUpload = filePath.toFile();

		// 检查远程是否存在同名文件，如果存在，询问是否覆盖
		/*if (ftpClient.checkFileExists(fileName) && !userConfirmsOverwrite(fileName)) {
			return;
		}*/

		// 执行上传操作
		if (!fileToUpload.isDirectory() && fileToUpload.exists() && fileToUpload.isFile()) {
			boolean success = ftpClient.uploadFile(fileToUpload);
			showTransferResult(success, "上传", fileName);
		}
	}

	private void downloadFile(String fileName) {
		java.io.File localFile = new java.io.File(peer.getCurrentPath(), fileName);

		// 检查本地是否存在同名文件，如果存在，询问是否覆盖
		if (localFile.exists() && !userConfirmsOverwrite(fileName)) {
			return;
		}

		// 执行下载操作
		boolean success = ftpClient.downloadFile(fileName, localFile);
		showTransferResult(success, "下载", fileName);
	}

	private boolean userConfirmsOverwrite(String fileName) {
		int result = JOptionPane.showConfirmDialog(this,
		                                           "目标位置已存在文件: " + fileName + "。是否覆盖？",
		                                           "文件已存在", JOptionPane.YES_NO_OPTION,
		                                           JOptionPane.QUESTION_MESSAGE);
		return result == JOptionPane.YES_OPTION;
	}

	private void showTransferResult(boolean success, String action, String fileName) {
		if (success) {
			JOptionPane.showMessageDialog(this,
			                              fileName + " " + action + "成功",
			                              "成功", JOptionPane.INFORMATION_MESSAGE);
			refresh(); // 刷新视图
		} else {
			JOptionPane.showMessageDialog(this,
			                              fileName + " " + action + "失败",
			                              "错误", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void deleteSelectedItems(int[] selectedRows) {
		if (selectedRows.length == 0) {
			JOptionPane.showMessageDialog(this, "没有选中任何项。", "错误", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// 根据选中的行数显示不同的确认消息
		String
				message =
				selectedRows.length == 1 ?
				"确定要删除选中的项吗？" :
				"确定要删除选中的 " + selectedRows.length + " 项吗？";

		int confirmation = JOptionPane.showConfirmDialog(this, message, "确认删除", JOptionPane.YES_NO_OPTION);
		if (confirmation == JOptionPane.YES_OPTION) {
			for (int viewRowIndex : selectedRows) {
				int    modelRowIndex = fileTable.convertRowIndexToModel(viewRowIndex);
				String fileName      = (String) fileTable.getModel().getValueAt(modelRowIndex, 0);
				String filePath      = Paths.get(currentPath, fileName).toString();
				fileSystemProvider.delete(filePath);
			}
			refresh();
		}
	}

	private JPopupMenu createTablePopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();

		JMenuItem menuItemRename = new JMenuItem("重命名");
		menuItemRename.setIcon(SvgIconLoader.loadSvgIcon("/rename-icon.svg", 16)); // 设定合适的大小
		menuItemRename.setToolTipText("重命名选定的文件或目录");

		JMenuItem menuItemDelete = new JMenuItem("删除");
		menuItemDelete.setIcon(SvgIconLoader.loadSvgIcon("/delete-icon.svg", 16)); // 设定合适的大小
		menuItemDelete.setToolTipText("删除选定的文件或目录");

		JMenuItem menuItemUploadDownload = new JMenuItem("上传/下载");
		menuItemUploadDownload.setIcon(SvgIconLoader.loadSvgIcon("/upload-download-icon.svg", 16)); // 设定合适的大小
		menuItemUploadDownload.setToolTipText("上传或下载文件");

		// 为菜单项添加动作监听器
		menuItemRename.addActionListener(e -> {
			int selectedRow = fileTable.getSelectedRow();
			if (selectedRow == -1) {
				JOptionPane.showMessageDialog(this, "没有选中任何项。", "错误", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String oldFileName = (String) fileTable.getValueAt(selectedRow, 0);
			String oldFilePath = Paths.get(currentPath, oldFileName).toString();

			String newFilename = promptForName("请输入新的文件名:", "重命名");
			if (newFilename != null && !newFilename.trim().isEmpty()) {
				fileSystemProvider.rename(oldFilePath, newFilename);
				refresh();
			} else if (newFilename != null) {
				JOptionPane.showMessageDialog(this, "文件名不能为空。", "警告", JOptionPane.WARNING_MESSAGE);
			}
		});

		menuItemDelete.addActionListener(e -> {
			int selectedRow = fileTable.getSelectedRow();
			if (selectedRow != -1) {
				// 对于 menuItemDelete，总是只有一行被选中
				deleteSelectedItems(new int[]{selectedRow});
			} else {
				JOptionPane.showMessageDialog(this, "没有选中任何项。", "错误", JOptionPane.ERROR_MESSAGE);
			}
		});
		menuItemUploadDownload.addActionListener(e -> {
			uploadDownloadSelectedFiles();
			peer.refresh();
		});


		// 将菜单项添加到弹出菜单
		popupMenu.add(menuItemRename);
		popupMenu.add(menuItemDelete);
		popupMenu.add(menuItemUploadDownload);

		return popupMenu;
	}
}
