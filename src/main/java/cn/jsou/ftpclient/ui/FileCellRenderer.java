package cn.jsou.ftpclient.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class FileCellRenderer extends DefaultTableCellRenderer {
	private Icon directoryIcon;
	private Icon fileIcon;

	@Override
	public Component getTableCellRendererComponent(JTable table,
	                                               Object value,
	                                               boolean isSelected,
	                                               boolean hasFocus,
	                                               int row,
	                                               int column) {
		// 调用超类获取默认的渲染组件
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		// 使用视图索引转换为模型索引
		int modelRow = table.convertRowIndexToModel(row);

		// 获取行高用于调整图标大小
		int rowHeight = table.getRowHeight();

		// 根据行高动态加载和调整图标
		if (directoryIcon == null || fileIcon == null) {
			directoryIcon = SvgIconLoader.loadSvgIcon("/FolderIcon.svg", rowHeight);
			fileIcon = SvgIconLoader.loadSvgIcon("/FileIcon.svg", rowHeight);
		}

		// 使用模型索引从模型中获取数据
		DefaultTableModel model     = (DefaultTableModel) table.getModel();
		String sizeValue = (String) model.getValueAt(modelRow, 1); // 假设大小列是第2列

		// 根据模型中的数据设置图标
		setIcon(sizeValue.isEmpty() ? directoryIcon : fileIcon); // 如果大小为空，表示这是一个目录

		setText((String) value);

		return this;
	}
}