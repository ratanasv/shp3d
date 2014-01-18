package edu.oregonstate.eecs.shp3d;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.data.shapefile.shp.ShapefileHeader;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;


import edu.oregonstate.eecs.shp3d.dialog.InputSHPFileDialog;
import edu.oregonstate.eecs.shp3d.dialog.OutputSHPFileDialog;
import edu.oregonstate.eecs.shp3d.processor.SHPUtil;

public class App  {
	private static Logger logger = LogManager.getLogger();
	
	public static String changeExtension(String shpFile, String extension) {
		if (extension.indexOf(".") == -1) {
			throw new IllegalArgumentException("does not contain a period");
		}
		return FilenameUtils.removeExtension(shpFile).concat(extension);
	}
	
	
	static String concatFilename(String base, String extra) {
		return FilenameUtils.getFullPath(base) + FilenameUtils.getBaseName(base) + extra +
				"." + FilenameUtils.getExtension(base);
	}


	public static void main( String[] args ) 
			throws IOException, NoSuchAuthorityCodeException, FactoryException 
	{
		System.out.println( "shp3d..." );
		try {
			Argument.init(args);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}

		File existingSHPFile;
		if (Argument.SHP_IN.isActive()) {
			existingSHPFile = new File(Argument.SHP_IN.getValue());
		} else {
			InputSHPFileDialog inputDialog = new InputSHPFileDialog();
			existingSHPFile = inputDialog.getFile();
		}
		logger.info("Input SHP file = {} ", existingSHPFile);

		File newSHPFile;
		if (Argument.SHP_OUT.isActive()) {
			newSHPFile = new File(Argument.SHP_OUT.getValue());
		} else {
			OutputSHPFileDialog outputDialog = new OutputSHPFileDialog(existingSHPFile);
			newSHPFile = outputDialog.getFile();
		}
		logger.info("Output SHP file = {} ", newSHPFile);
		
		final SimpleFeatureSource source = SHPUtil.getSource(existingSHPFile);
		
		
		String latlongFileString = FilenameUtils.getFullPath(newSHPFile.toString()) + 
				FilenameUtils.getBaseName(existingSHPFile.toString()) + 
				"Latlong" + ".shp";
		
		File latlongFile = new File(latlongFileString);
		logger.info("Projecting to Lat/Long...");
		SHPUtil.reprojectToLatLong(source, latlongFile);
		
		ShapefileHeader headerLatlong = SHPUtil.getShapefileHeader(latlongFile);
		final SimpleFeatureSource latlongSource = SHPUtil.getSource(latlongFile);
		SHPUtil.fillWithBogusZ(latlongSource, headerLatlong, newSHPFile);
		logger.info("All done!");
	}



}
