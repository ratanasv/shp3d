package edu.oregonstate.eecs.shp3d;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;
import org.opengis.geometry.Geometry;

import com.vividsolutions.jts.geom.MultiPolygon;

public class App  {
	static String changeExtension(String shpFile, String extension) {
		if (extension.indexOf(".") == -1) {
			throw new IllegalArgumentException("does not contain a period");
		}
		return FilenameUtils.removeExtension(shpFile).concat(extension);
	}
	

	public static void main( String[] args ) throws IOException {
		System.out.println( "shp3d..." );
		try {
			ArgumentManager.Init(args);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		URL shapeURL = null;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("Shapefile", "shp"));
		int result = fileChooser.showOpenDialog(null);
		 
		if (result == JFileChooser.APPROVE_OPTION) {
		    File f = fileChooser.getSelectedFile();
		    shapeURL = f.toURL();
		} else {
		    throw new RuntimeException("no valid input 2D shapefile");
		}
		
		ShapefileDataStore store = new ShapefileDataStore(shapeURL);
		String name = store.getTypeNames()[0];
		FeatureSource source = store.getFeatureSource(name);
		FeatureCollection fsShape = source.getFeatures();

		// print out a feature type header and wait for user input
		FeatureType ft = source.getSchema();
		System.out.println("FID\t");
		System.out.println(ft.toString());
		System.out.println();

		FeatureIterator<SimpleFeature> iterator = (SimpleFeatureIterator) fsShape.features();
		try {
			while( iterator.hasNext() ){
				SimpleFeature feature = iterator.next();
				System.out.print(feature.getID() + "\t");
				for (int i = 0; i < feature.getAttributeCount(); i++) {
					Object attribute = feature.getAttribute( i );
					if (!(attribute instanceof Geometry))
						System.out.print(attribute + "\t");
				}
				System.out.println();
			}
		}
		finally {
			iterator.close();
		}

		iterator = (SimpleFeatureIterator) fsShape.features();
		try {
			while( iterator.hasNext() ){
				SimpleFeature feature = iterator.next();
				System.out.print(feature.getID() + "\t");
				MultiPolygon geometry = (MultiPolygon)feature.getDefaultGeometry();
				System.out.println(geometry);
				System.out.println();
			}
		}
		finally {
			iterator.close();
		}

	}
}
