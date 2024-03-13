package cn.jsou.ftpclient;

import cn.jsou.ftpclient.ui.MainFrame;

import javax.swing.*;

public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			MainFrame frame = new MainFrame();
			frame.setVisible(true);
		});
	}
}