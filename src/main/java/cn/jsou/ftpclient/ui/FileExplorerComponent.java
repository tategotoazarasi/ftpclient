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
		});
	}

	public void setFileSystemProvider(FileSystemProvider fsp) {
		this.fileSystemProvider = fsp;
	}

	public String getCurrentPath() {
		return currentPath;
	}
}
