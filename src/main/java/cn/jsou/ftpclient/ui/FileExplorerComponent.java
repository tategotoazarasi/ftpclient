package cn.jsou.ftpclient.ui;

import cn.jsou.ftpclient.vfs.File;
import cn.jsou.ftpclient.vfs.FileSystemProvider;

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
		fileTable = new JTable();
		JScrollPane scrollPane = new JScrollPane(fileTable);
		add(scrollPane, BorderLayout.CENTER);
	}

	public void updateFileList(String path) {
		String[]          columnNames = {"Name", "Size", "Created Time", "Modified Time"};
		DefaultTableModel model       = new DefaultTableModel(columnNames, 0);

		java.util.List<String> directories = fileSystemProvider.getDirectories(path);
		directories.forEach(dir -> model.addRow(new Object[]{dir, "", "", ""}));

		List<File> files = fileSystemProvider.getFiles(path);
		files.forEach(file -> model.addRow(new Object[]{
				file.getName(),
				file.getSize() + " bytes",
				dateTimeFormatter.format(file.getCreatedTime()),
				dateTimeFormatter.format(file.getModifiedTime())
		}));

		fileTable.setModel(model);
		currentPath = path;
	}
}
