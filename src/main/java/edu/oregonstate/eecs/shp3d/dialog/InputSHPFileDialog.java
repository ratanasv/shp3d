package edu.oregonstate.eecs.shp3d.dialog;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class InputSHPFileDialog {
	private final File file;
	public InputSHPFileDialog() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("Shapefile", "shp"));
		int result = fileChooser.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
		} else {
			throw new RuntimeException("no valid input shapefile");
		}
	}
	
	public File getFile() {
		return file;
	}
}
