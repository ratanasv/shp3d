package edu.oregonstate.eecs.shp3d;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import edu.oregonstate.eecs.processor.SHPUtil;
import edu.oregonstate.eecs.shp3d.dialog.InputSHPFileDialog;
import edu.oregonstate.eecs.shp3d.dialog.OutputSHPFileDialog;

public class App  {
	private static Logger logger = LogManager.getLogger();
	
	static String changeExtension(String shpFile, String extension) {
		if (extension.indexOf(".") == -1) {
			throw new IllegalArgumentException("does not contain a period");
		}
		return FilenameUtils.removeExtension(shpFile).concat(extension);
	}


	private static SimpleFeatureCollection fillWithBogusZ(
			SimpleFeatureCollection fsShape, final SimpleFeatureType type) 
	{
		FeatureIterator<SimpleFeature> iterator = fsShape.features();
		SimpleFeatureCollection collection = new DefaultFeatureCollection(null, null);
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory( null );	
		try {
			while( iterator.hasNext() ){
				SimpleFeature feature = iterator.next();
				System.out.print(feature.getID() + "\t");
				MultiPolygon multiGeometry = (MultiPolygon)feature.getDefaultGeometry();
				Polygon polygons[] = new Polygon[multiGeometry.getNumGeometries()];
				for (int i=0; i<multiGeometry.getNumGeometries(); i++) {
					com.vividsolutions.jts.geom.Geometry geometry = multiGeometry.getGeometryN(i);
					Coordinate[] twoDCoords = geometry.getCoordinates();
					Coordinate[] threeDCoords = new Coordinate[twoDCoords.length];
					for (int j=0; j<twoDCoords.length; j++) {
						threeDCoords[j] = new Coordinate(twoDCoords[j].x, twoDCoords[j].y, 3.14);
					}
					LinearRing ring = geometryFactory.createLinearRing( threeDCoords );
					polygons[i] = geometryFactory.createPolygon(ring, null );
				}
				MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(polygons);
				featureBuilder.add(multiPolygon);
				featureBuilder.buildFeature(feature.getID());
			}
		}
		finally {
			iterator.close();
		}

		return collection;
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
		SHPUtil.reprojectToLatLong(source, newSHPFile);
		
	}



}
