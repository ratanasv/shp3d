package edu.oregonstate.eecs.shp3d;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileHeader;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.MultiPolygon;

import edu.oregonstate.eecs.shp3d.dialog.InputSHPFileDialog;
import edu.oregonstate.eecs.shp3d.processor.GeometryPrinterVisitor;
import edu.oregonstate.eecs.shp3d.processor.Pipeliner;
import edu.oregonstate.eecs.shp3d.processor.SHPUtil;

public class SHPInfo {
	private static Logger logger = LogManager.getLogger();
	
	private static void printFeaturesAttributes(SimpleFeatureCollection fsShape) {
		FeatureIterator<SimpleFeature> iterator = fsShape.features();
		try {
			while( iterator.hasNext() ){
				SimpleFeature feature = iterator.next();
				System.out.print(feature.getID() + "\t");
				for (int i = 0; i < feature.getAttributeCount(); i++) {
					Object attribute = feature.getAttribute( i );
					if (!(attribute instanceof MultiPolygon)) {
						System.out.print(attribute + "\t");
					}
				}
				System.out.println();
			}
		}
		finally {
			iterator.close();
		}
	}
	
	private static void printFeaturesGeometry(SimpleFeatureSource source) throws IOException {
		
		Pipeliner pipe = new Pipeliner(source);
		pipe.start(new GeometryPrinterVisitor());
	}
	
	private static void printShapefileHeader(File blah) 
			throws ShapefileException, MalformedURLException, IOException {

		ShapefileHeader header = SHPUtil.getShapefileHeader(blah);
		System.out.println("shp header = " + header.toString());
	}

	public static void main(String[] args) throws IOException {
		System.out.println( "SHPInfo..." );
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
		
		SimpleFeatureSource source = SHPUtil.getSource(existingSHPFile);
		SimpleFeatureCollection collection = source.getFeatures();

		if (Argument.PRINT_SCHEMA.isActive()) {
			SimpleFeatureType featureType = (SimpleFeatureType) source.getSchema();
			System.out.println("FeatureType (schema) =" + featureType);
		}
		
		if (Argument.PRINT_HEADER.isActive()) {
			printShapefileHeader(existingSHPFile);
		}
		
		if (Argument.PRINT_ATTRIBUTE.isActive()) {
			printFeaturesAttributes(collection);
		}
		
		if (Argument.PRINT_GEOMETRY.isActive()) {
			printFeaturesGeometry(source);
		}
	}

}
