package cn.jsou.ftpclient.ui;

import cn.jsou.ftpclient.utils.TimeUtils;
import cn.jsou.ftpclient.vfs.File;
import cn.jsou.ftpclient.vfs.FileSystemProvider;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class FileExplorerComponent extends JPanel {
	private FileSystemProvider fileSystemProvider;
	private JTable             fileTable;
	private String             currentPath;

	public FileExplorerComponent(FileSystemProvider fileSystemProvider, String initialPath) {
		this.fileSystemProvider = fileSystemProvider;
		this.currentPath        = initialPath;
		initUI();
		updateFileList(initialPath);
	}

	private void initUI() {
		setLayout(new BorderLayout());

		// 创建按钮并添加到工具栏
		JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JButton btnGoUp = new JButton();
		btnGoUp.setIcon(SvgIconLoader.loadSvgIcon("/up-icon.svg", 24));
		btnGoUp.setToolTipText("上一级");

		JButton btnNewFolder = new JButton();
		btnNewFolder.setIcon(SvgIconLoader.loadSvgIcon("/new-folder-icon.svg", 24));
		btnNewFolder.setToolTipText("新建目录");

		JButton btnRename = new JButton();
		btnRename.setIcon(SvgIconLoader.loadSvgIcon("/rename-icon.svg", 24));
		btnRename.setToolTipText("重命名");

		JButton btnDelete = new JButton();
		btnDelete.setIcon(SvgIconLoader.loadSvgIcon("/delete-icon.svg", 24));
		btnDelete.setToolTipText("删除");

		JButton btnUploadDownload = new JButton();
		btnUploadDownload.setIcon(SvgIconLoader.loadSvgIcon("/upload-download-icon.svg", 24));
		btnUploadDownload.setToolTipText("上传/下载");

		JButton btnRefresh = new JButton();
		btnRefresh.setIcon(SvgIconLoader.loadSvgIcon("/refresh-icon.svg", 24));
		btnRefresh.setToolTipText("刷新");

		// 添加按钮到工具栏
		toolBar.add(btnGoUp);
		toolBar.add(btnNewFolder);
		toolBar.add(btnRename);
		toolBar.add(btnDelete);
		toolBar.add(btnUploadDownload);
		toolBar.add(btnRefresh);

		// 添加动作监听器 - 暂时为空，等待实现
		btnGoUp.addActionListener(e -> {});
		btnNewFolder.addActionListener(e -> {});
		btnRename.addActionListener(e -> {});
		btnDelete.addActionListener(e -> {});
		btnUploadDownload.addActionListener(e -> {});

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
		this.repaint();
	}

	private void addTableMouseListener() {
		fileTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) { // 双击事件
					int row = fileTable.rowAtPoint(e.getPoint());
					if (row >= 0) {
						String name = (String) fileTable.getModel().getValueAt(row, 0); // 获取双击的行的“Name”列的值
						String
								newPath =
								currentPath + (currentPath.endsWith("/") ? "" : '/') + name; // 假设为简单路径拼接，根据您的实际情况调整
						// 判断是否为目录
						if (fileSystemProvider.isDirectory(newPath)) {
							updateFileList(newPath); // 更新文件列表为新的目录路径
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
		menuItemRename.addActionListener(e -> {});
		menuItemDelete.addActionListener(e -> {});

		// 将菜单项添加到弹出菜单
		popupMenu.add(menuItemRename);
		popupMenu.add(menuItemDelete);
		popupMenu.add(menuItemUploadDownload);

		return popupMenu;
	}

	public void setFileSystemProvider(FileSystemProvider fsp) {
		this.fileSystemProvider = fsp;
	}

	public String getCurrentPath() {
		return currentPath;
	}

}
