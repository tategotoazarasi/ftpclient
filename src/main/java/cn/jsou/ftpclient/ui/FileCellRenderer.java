package cn.jsou.ftpclient.ui;

import cn.jsou.ftpclient.utils.SvgIconLoader;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * 自定义单元格渲染器类，用于在JTable中为文件和目录显示不同的图标
 */
public class FileCellRenderer extends DefaultTableCellRenderer {
	/**
	 * 目录的图标
	 */
	private Icon directoryIcon;
	/**
	 * 文件的图标
	 */
	private Icon fileIcon;

	/**
	 * 获取单元格渲染组件。此方法会根据单元格的内容调整显示的图标
	 *
	 * @param table      <code>JTable</code>实例，表示要渲染的表格
	 * @param value      单元格的值
	 * @param isSelected 指示该单元格是否被选中
	 * @param hasFocus   指示该单元格是否拥有焦点
	 * @param row        单元格所在的行
	 * @param column     单元格所在的列
	 *
	 * @return 返回配置好的渲染组件
	 */
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
			fileIcon      = SvgIconLoader.loadSvgIcon("/FileIcon.svg", rowHeight);
		}

		// 使用模型索引从模型中获取数据
		DefaultTableModel model     = (DefaultTableModel) table.getModel();
		String            sizeValue = (String) model.getValueAt(modelRow, 1); // 假设大小列是第2列

		// 根据模型中的数据设置图标
		setIcon(sizeValue.isEmpty() ? directoryIcon : fileIcon); // 如果大小为空，表示这是一个目录

		setText((String) value);

		return this;
	}
}