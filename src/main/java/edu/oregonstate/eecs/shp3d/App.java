package edu.oregonstate.eecs.shp3d;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;

public class App  {
	static String changeExtension(String shpFile, String extension) {
		if (extension.indexOf(".") == -1) {
			throw new IllegalArgumentException("does not contain a period");
		}
		return FilenameUtils.removeExtension(shpFile).concat(extension);
	}
	

	public static void main( String[] args ) {
		System.out.println( "shp3d..." );
		try {
			ArgumentManager.Init(args);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
	}
}
