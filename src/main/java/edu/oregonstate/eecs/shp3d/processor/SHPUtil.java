package edu.oregonstate.eecs.shp3d.processor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileHeader;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;



public final class SHPUtil {
	
	public static SimpleFeatureSource getSource(File shpFile) throws IOException {
		ShapefileDataStore store = new ShapefileDataStore(shpFile.toURI().toURL());
		String name = store.getTypeNames()[0];
		SimpleFeatureSource source = store.getFeatureSource(name);
		return source;
	}
	

	
	public static void reprojectToLatLong(
			final SimpleFeatureSource source,
			File file) throws NoSuchAuthorityCodeException, FactoryException, IOException {
		
		Pipeliner.Start(source, new LatLongVisitor(file));
	}
	
	public static ShapefileHeader getShapefileHeader(File blah) 
			throws ShapefileException, MalformedURLException, IOException {
		
		ShapefileReader reader = new ShapefileReader(new ShpFiles(blah), true, false, 
				null);
		ShapefileHeader header = reader.getHeader();
		reader.close();
		return header;
	}
	
	
	
	public static void fillWithBogusZ(
			final SimpleFeatureSource source, 
			final File outputFile) throws IOException {
		Pipeliner.Start(source, new ZWriterVisitor(outputFile));
	}
}
