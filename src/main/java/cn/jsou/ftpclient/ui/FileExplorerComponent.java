package cn.jsou.ftpclient.ui;

import cn.jsou.ftpclient.utils.TimeUtils;
import cn.jsou.ftpclient.vfs.File;
import cn.jsou.ftpclient.vfs.FileSystemProvider;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FileExplorerComponent extends JPanel {
	private static final DateTimeFormatter  dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private final        FileSystemProvider fileSystemProvider;
	private              JTable             fileTable;
	private              String             currentPath;

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
	}


	public void updateFileList(String path) {
		String[] columnNames = {"Name", "Size", "Modified Time"};
		DefaultTableModel model       = new DefaultTableModel(columnNames, 0);

		java.util.List<String> directories = fileSystemProvider.getDirectories(path);
		directories.forEach(dir -> model.addRow(new Object[]{dir, "", "", ""}));

		List<File> files = fileSystemProvider.getFiles(path);
		files.forEach(file -> model.addRow(new Object[]{
				file.getName(),
				FileUtils.byteCountToDisplaySize(file.getSize()),
				//TimeUtils.formatRelativeTime(file.getCreatedTime()),
				TimeUtils.formatRelativeTime(file.getModifiedTime())
		}));

		fileTable.setModel(model);
		currentPath = path;

		fileTable.getColumnModel().getColumn(0).setCellRenderer(new FileCellRenderer());
	}
}
