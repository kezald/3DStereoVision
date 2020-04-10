package imgdlg;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MyMenuBar extends JMenuBar {
	JMenu menuHeatAlgorithmControl;
	// JMenuItem menuItemCustomAlgorithm;
	// JMenuItem menuItemDefaultAlgorithm;
	JMenuItem menuItemChooseImage;

	public MyMenuBar() {
		initComponents();
		loadComponents();
	}

	private void initComponents() {
		menuHeatAlgorithmControl = new JMenu("File");
		menuItemChooseImage = new JMenuItem("Choose Image");
	}

	private void loadComponents() {
		menuHeatAlgorithmControl.add(menuItemChooseImage);
		this.add(menuHeatAlgorithmControl);
	}
}
