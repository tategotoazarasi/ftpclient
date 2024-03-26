package cn.jsou.ftpclient.ui;

import cn.jsou.ftpclient.ftp.FtpClient;
import cn.jsou.ftpclient.utils.TimeUtils;
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
	JButton btnGoUp           = new JButton();
	JButton btnNewFolder      = new JButton();
	JButton btnDelete         = new JButton();
	JButton btnUploadDownload = new JButton();
	JButton btnRefresh        = new JButton();
	private boolean               isRemote = false;
	private FileSystemProvider    fileSystemProvider;
	private JTable                fileTable;
	private String                currentPath;
	private FtpClient             ftpClient;
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
			// 弹出输入对话框要求用户输入新目录的名称
			String
					newFolderName =
					JOptionPane.showInputDialog(this, "输入新目录的名称:", "新建目录", JOptionPane.PLAIN_MESSAGE);

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
		btnDelete.addActionListener(e -> {});
		btnUploadDownload.addActionListener(e -> {});
		btnRefresh.addActionListener(e -> refresh());

		// 将工具栏添加到主面板的顶部
		add(toolBar, BorderLayout.NORTH);

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
				TimeUtils.formatRelativeTime(file.getCreatedTime()),
				TimeUtils.formatRelativeTime(file.getModifiedTime())
		}));

		fileTable.setModel(model);
		currentPath = path;

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
		menuItemRename.addActionListener(e -> {});
		menuItemDelete.addActionListener(e -> {});
		menuItemUploadDownload.addActionListener(e -> {
			if (ftpClient == null) {
				JOptionPane.showMessageDialog(this, "请先连接到FTP服务器", "错误", JOptionPane.ERROR_MESSAGE);
				return;
			}
			int selectedRow = fileTable.getSelectedRow();
			if (!isRemote) {
				// 获取选中的文件名
				if (selectedRow != -1) {
					String fileName = (String) fileTable.getValueAt(selectedRow, 0);
					Path   filePath = Paths.get(currentPath, fileName);

					// 检查文件是否存在
					java.io.File fileToUpload = filePath.toFile();
					if (fileToUpload.exists() && fileToUpload.isFile()) {
						// 在这里执行上传操作，假设有一个ftpClient对象可用
						boolean success = ftpClient.uploadFile(fileToUpload);
						if (success) {
							ftpClient.dataServer.waitHandlerComplete();
							peer.refresh();
							JOptionPane.showMessageDialog(this,
							                              "文件上传成功",
							                              "成功",
							                              JOptionPane.INFORMATION_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(this, "文件上传失败", "错误", JOptionPane.ERROR_MESSAGE);
						}
					} else {
						JOptionPane.showMessageDialog(this, "选中的文件不存在", "错误", JOptionPane.ERROR_MESSAGE);
					}
				}
			} else {
				// 获取选中的文件名
				if (selectedRow != -1) {
					// 获取选中的文件名
					if (selectedRow != -1) {
						String fileName = (String) fileTable.getValueAt(selectedRow, 0);

						// 目标下载路径，这里假设peer代表的是本地文件系统
						java.io.File localFile = peer.getCurrentPath() != null ?
						                         Paths.get(peer.getCurrentPath(), fileName).toFile() :
						                         new java.io.File(fileName);

						if (localFile.exists()) {
							// 如果文件已存在，询问用户是否覆盖
							int result = JOptionPane.showConfirmDialog(this,
							                                           "文件已存在。是否覆盖？", "文件存在",
							                                           JOptionPane.YES_NO_OPTION,
							                                           JOptionPane.QUESTION_MESSAGE);

							if (result == JOptionPane.NO_OPTION) {
								// 如果用户选择不覆盖，取消操作
								return;
							}
							// 否则继续执行下载操作
						}

						// 在这里执行下载操作，假设有一个ftpClient对象可用
						boolean success = ftpClient.downloadFile(fileName, localFile);
						if (success) {
							ftpClient.dataServer.waitHandlerComplete();
							peer.refresh();
							JOptionPane.showMessageDialog(this,
							                              "文件下载成功",
							                              "成功",
							                              JOptionPane.INFORMATION_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(this, "文件下载失败", "错误", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		});


		// 将菜单项添加到弹出菜单
		popupMenu.add(menuItemRename);
		popupMenu.add(menuItemDelete);
		popupMenu.add(menuItemUploadDownload);

		return popupMenu;
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
}
