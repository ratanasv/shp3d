package edu.oregonstate.eecs.shp3d;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.Geometry;

import com.vividsolutions.jts.geom.MultiPolygon;

public class App  {
	static String changeExtension(String shpFile, String extension) {
		if (extension.indexOf(".") == -1) {
			throw new IllegalArgumentException("does not contain a period");
		}
		return FilenameUtils.removeExtension(shpFile).concat(extension);
	}

	private static File getNewShapeFile(File twoDSHPFile) {
		String path = twoDSHPFile.getAbsolutePath();
		String newPath = path.substring(0, path.length() - 4) + "3D.shp";

		JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
		chooser.setDialogTitle("Save shapefile");
		chooser.setSelectedFile(new File(newPath));

		int returnVal = chooser.showSaveDialog(null);

		if (returnVal != JFileDataStoreChooser.APPROVE_OPTION) {
			throw new RuntimeException("output file is not valid");
		}

		File threeDSHPFile = chooser.getSelectedFile();
		if (threeDSHPFile.equals(twoDSHPFile)) {
			throw new RuntimeException("Error: cannot replace " + twoDSHPFile);
		}

		return threeDSHPFile;
	}

	private static File getExistingShapeFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("Shapefile", "shp"));
		int result = fileChooser.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			return f;
		} else {
			throw new RuntimeException("no valid input 2D shapefile");
		}
	}

	private static void printFeaturesAttributes(
			FeatureCollection<SimpleFeatureType, SimpleFeature> fsShape) 
	{
		FeatureIterator<SimpleFeature> iterator = fsShape.features();
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
	}


	public static void main( String[] args ) throws IOException {
		System.out.println( "shp3d..." );
		try {
			ArgumentManager.Init(args);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}


		File existingSHPFile = getExistingShapeFile();
		ShapefileDataStore store = new ShapefileDataStore(existingSHPFile.toURI().toURL());
		String name = store.getTypeNames()[0];
		FeatureSource<SimpleFeatureType, SimpleFeature> source = store.getFeatureSource(name);
		FeatureCollection<SimpleFeatureType, SimpleFeature> fsShape = source.getFeatures();

		// print out a feature type header and wait for user input
		SimpleFeatureType featureType = (SimpleFeatureType) source.getSchema();
		System.out.println("FID\t");
		System.out.println(featureType.toString());
		System.out.println();

		printFeaturesAttributes(fsShape);
		printFeaturesGeometry(fsShape);

		File newSHPFile = getNewShapeFile(existingSHPFile);

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		Map<String, Serializable> params = getDataStoreParams(newSHPFile);

		ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
		newDataStore.createSchema(featureType);

		/*
		 * You can comment out this line if you are using the createFeatureType method (at end of
		 * class file) rather than DataUtilities.createType
		 */
		//newDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);

		Transaction transaction = new DefaultTransaction("create");

		String typeName = store.getTypeNames()[0];
		SimpleFeatureSource featureSource = store.getFeatureSource(typeName);

		if (featureSource instanceof SimpleFeatureStore) {
			SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

			featureStore.setTransaction(transaction);
			try {
				featureStore.addFeatures(fsShape);
				transaction.commit();

			} catch (Exception problem) {
				problem.printStackTrace();
				transaction.rollback();

			} finally {
				transaction.close();
			}
		} else {
			System.out.println(typeName + " does not support read/write access");
			System.exit(1);
		}

	}

	private static Map<String, Serializable> getDataStoreParams(File newSHPFile)
			throws MalformedURLException {
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", newSHPFile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);
		return params;
	}

	private static void printFeaturesGeometry(
			FeatureCollection<SimpleFeatureType, SimpleFeature> fsShape) 
	{
		FeatureIterator<SimpleFeature> iterator = fsShape.features();
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
