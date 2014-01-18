package edu.oregonstate.eecs.shp3d.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileHeader;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import edu.oregonstate.eecs.shp3d.App;



public final class SHPUtil {
	private static Logger logger = LogManager.getLogger();
	private static final int numLats = 2048;
	private static final int numLngs = 2048;
	
	public static SimpleFeatureSource getSource(File shpFile) throws IOException {
		ShapefileDataStore store = new ShapefileDataStore(shpFile.toURI().toURL());
		String name = store.getTypeNames()[0];
		SimpleFeatureSource source = store.getFeatureSource(name);
		return source;
	}
	

	
	public static void reprojectToLatLong(
			final SimpleFeatureSource source,
			File file) throws NoSuchAuthorityCodeException, FactoryException, IOException {
		
		Pipeliner pipe = new Pipeliner(source);
		pipe.start(new LatLongVisitor(file));
	}
	
	public static ShapefileHeader getShapefileHeader(File blah) 
			throws ShapefileException, MalformedURLException, IOException {
		
		ShapefileReader reader = new ShapefileReader(new ShpFiles(blah), true, false, 
				null);
		ShapefileHeader header = reader.getHeader();
		reader.close();
		return header;
	}
	

	static DEMConnection demConnectionFactory(ShapefileHeader header) throws IOException {
		String urlString = DEMQueryBuilder.startBuilding(DEMConnection.Server.MIKES_DEM.getURL())
				.withLat1((float) header.minY())
				.withLat2((float) header.maxY())
				.withLng1((float) header.minX())
				.withLng2((float) header.maxX())
				.withNumLats(numLats)
				.withNumLngs(numLngs)
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
	
	
	public static void fillWithDEMHeights(
			final SimpleFeatureSource source, 
			final ShapefileHeader header,
			final File outputFile) throws IOException {
		
		Pipeliner pipe = new Pipeliner(source);
		DEMConnection connection = demConnectionFactory(header);
		logger.info("DEM data received, begin parsing...");
		HeightField heightField = new DEMHeightField(connection);
		ZWriterVisitor visitor = new ZWriterVisitor(header, outputFile, heightField);
		pipe.start(visitor);
		ShapefileHeaderZRepair zHeaderRepair = new ShapefileHeaderZRepair.Builder()
				.withMinZ(visitor.getMinZ())
				.withMaxZ(visitor.getMaxZ())
			.build();
		zHeaderRepair.writeToFile(outputFile);
		File shxFile = new File(App.changeExtension(outputFile.toString(), ".shx"));
		zHeaderRepair.writeToFile(shxFile);
		
		File xtrFile = new File(App.changeExtension(outputFile.toString(), ".xtr"));
		writeToFile(xtrFile, connection.getByteArray());
	}
}
