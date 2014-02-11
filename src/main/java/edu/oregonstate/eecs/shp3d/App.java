package edu.oregonstate.eecs.shp3d;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.data.shapefile.shp.ShapefileHeader;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;


import edu.oregonstate.eecs.shp3d.cppinterface.CallCppProcess;
import edu.oregonstate.eecs.shp3d.dialog.InputSHPFileDialog;
import edu.oregonstate.eecs.shp3d.dialog.OutputSHPFileDialog;
import edu.oregonstate.eecs.shp3d.processor.DEMConnection;
import edu.oregonstate.eecs.shp3d.processor.DEMHeightField;
import edu.oregonstate.eecs.shp3d.processor.DEMQueryBuilder;
import edu.oregonstate.eecs.shp3d.processor.HeightField;
import edu.oregonstate.eecs.shp3d.processor.SHPUtil;

public class App  {
	private static Logger logger = LogManager.getLogger();
	private static final int numLats = 2048;
	private static final int numLngs = 2048;
	
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
	
	static DEMConnection demConnectionFactory(ShapefileHeader header) throws IOException {
		final int resolution;
		if (Argument.DEM_RESOLUTION.isActive()) {
			resolution = Integer.parseInt(Argument.DEM_RESOLUTION.getValue());
		} else {
			resolution = 2048;
		}
		String urlString = DEMQueryBuilder.startBuilding(DEMConnection.Server.MIKES_DEM.getURL())
				.withLat1((float) header.minY())
				.withLat2((float) header.maxY())
				.withLng1((float) header.minX())
				.withLng2((float) header.maxX())
				.withNumLats(resolution)
				.withNumLngs(resolution)
			.build();
		logger.info("Connecting to DEM server with Query {}. This will take awhile...", 
			urlString);
		DEMConnection connection = new DEMConnection(urlString);
		return connection;
	}
	
	private static void writeToFile(File outFile, byte[] data) throws IOException {
		FileOutputStream out = new FileOutputStream(outFile);
		out.write(data);
		out.close();
	}
	


	public static void main( String[] args ) 
			throws IOException, NoSuchAuthorityCodeException, FactoryException, URISyntaxException 
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
		
		final String fullPathBaseName = FilenameUtils.getFullPath(newSHPFile.toString()) + 
				FilenameUtils.getBaseName(existingSHPFile.toString());
		final String latlongFileString = fullPathBaseName + "Latlong" + ".shp";
		
		
		File latlongFile = new File(latlongFileString);
		logger.info("Projecting to Lat/Long...");
		SHPUtil.reprojectToLatLong(source, latlongFile);
		
		ShapefileHeader headerLatlong = SHPUtil.getShapefileHeader(latlongFile);
		final SimpleFeatureSource latlongSource = SHPUtil.getSource(latlongFile);
		
		DEMConnection connection = demConnectionFactory(headerLatlong);
		logger.info("DEM data received, begin parsing...");
		HeightField heightField = new DEMHeightField(connection);
		
		
		SHPUtil.fillWithDEMHeights(latlongSource, headerLatlong, newSHPFile, heightField);
		
		File xtrFile = new File(fullPathBaseName + ".xtr");
		writeToFile(xtrFile, connection.getByteArray());
		
		final String fullPathNewBaseName = FilenameUtils.getFullPath(newSHPFile.toString()) + 
				FilenameUtils.getBaseName(newSHPFile.toString());
		CallCppProcess cppProcess = new CallCppProcess(fullPathNewBaseName + "Normals.jpg",
				xtrFile.toString(),
				fullPathNewBaseName + ".ttt", 
				newSHPFile.toString());
		cppProcess.run();
		logger.info("All done!");
	}



}
