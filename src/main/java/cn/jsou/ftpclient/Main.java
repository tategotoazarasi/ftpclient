package cn.jsou.ftpclient;

import cn.jsou.ftpclient.ui.MainFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

import static javax.swing.UIManager.setLookAndFeel;

public class Main {
	private static final Logger logger = LogManager.getLogger(Main.class);

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (UnsupportedLookAndFeelException |
			         ClassNotFoundException |
			         InstantiationException |
			         IllegalAccessException e) {
				logger.error("Unsupported Look and Feel: {}", e.getMessage());
			}

			MainFrame frame = new MainFrame();
			frame.setVisible(true);
		});
	}
}