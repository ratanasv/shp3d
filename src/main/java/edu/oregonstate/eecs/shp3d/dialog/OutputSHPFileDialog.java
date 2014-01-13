package edu.oregonstate.eecs.shp3d.dialog;

import java.io.File;

import org.geotools.swing.data.JFileDataStoreChooser;

public final class OutputSHPFileDialog {
	private final File file;
	
	public OutputSHPFileDialog(File existingFile) {
		String path = existingFile.getAbsolutePath();
		String newPath = path.substring(0, path.length() - 4) + "3D.shp";

		JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
		chooser.setDialogTitle("Save shapefile");
		chooser.setSelectedFile(new File(newPath));

		int returnVal = chooser.showSaveDialog(null);

		if (returnVal != JFileDataStoreChooser.APPROVE_OPTION) {
			throw new RuntimeException("output file is not valid");
		}

		File threeDSHPFile = chooser.getSelectedFile();
		if (threeDSHPFile.equals(existingFile)) {
			throw new RuntimeException("Error: cannot replace " + existingFile);
		}

		file = threeDSHPFile;
	}
	
	public File getFile() {
		return file;
	}
}
