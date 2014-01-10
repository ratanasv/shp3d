package edu.oregonstate.eecs.shp3d;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileHeader;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

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

	private static void printFeaturesAttributes(SimpleFeatureCollection fsShape) {
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

	private static Map<String, Serializable> getDataStoreParams(File newSHPFile)
			throws MalformedURLException {
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", newSHPFile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);
		return params;
	}

	private static void reprojectToLatLong(final SimpleFeatureType schema,
			SimpleFeatureCollection featureCollection,
			File file) throws NoSuchAuthorityCodeException, FactoryException, IOException {
		
		CoordinateReferenceSystem dataCRS = schema.getCoordinateReferenceSystem();
		CoordinateReferenceSystem worldCRS = DefaultGeographicCRS.WGS84;
		boolean lenient = true; // allow for some error due to different datums
		MathTransform transform = CRS.findMathTransform(dataCRS, worldCRS, lenient);

		DataStoreFactorySpi factory = new ShapefileDataStoreFactory();
		DataStore dataStore = factory.createNewDataStore(getDataStoreParams(file));
		SimpleFeatureType featureType = SimpleFeatureTypeBuilder.retype(schema, worldCRS);
		dataStore.createSchema(featureType);
		String[] typeNames = dataStore.getTypeNames();

		Transaction transaction = new DefaultTransaction("Reproject");
		FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
				dataStore.getFeatureWriterAppend(typeNames[0], transaction);
		SimpleFeatureIterator iterator = featureCollection.features();
		try {
			while (iterator.hasNext()) {
				// copy the contents of each feature and transform the geometry
				SimpleFeature feature = iterator.next();
				SimpleFeature copy = writer.next();
				copy.setAttributes(feature.getAttributes());

				MultiPolygon geometry = (MultiPolygon) feature.getDefaultGeometry();
				MultiPolygon geometry2 = (MultiPolygon) JTS.transform(geometry, transform);

				copy.setDefaultGeometry(geometry2);
				writer.write();
			}
			writer.close();
			transaction.commit();
			JOptionPane.showMessageDialog(null, "Export to shapefile complete");
		} catch (Exception problem) {
			problem.printStackTrace();
			transaction.rollback();
			JOptionPane.showMessageDialog(null, "Export to shapefile failed");
		} finally {
			writer.close();
			iterator.close();
			transaction.close();
			dataStore.dispose();
		}
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

	private static void printFeaturesGeometry(SimpleFeatureCollection fsShape) {
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
	
	private static void getMinMaxSHPFile(File blah) 
			throws ShapefileException, MalformedURLException, IOException {
		
		ShapefileReader reader = new ShapefileReader(new ShpFiles(blah), true, false, 
				null);
		ShapefileHeader header = reader.getHeader();
		System.out.println(header.toString());
		reader.close();
	}


	public static void main( String[] args ) 
			throws IOException, NoSuchAuthorityCodeException, FactoryException 
	{
		System.out.println( "shp3d..." );
		try {
			Argument.ArgumentManager.init(args);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		if (Argument.CONNECT.isActive()) {
			new DEMConnection("foo");
			System.exit(0);
		}
		
		

		File existingSHPFile;
		if (Argument.SHP_IN.isActive()) {
			existingSHPFile = new File(Argument.SHP_IN.getValue());
		} else {
			existingSHPFile = getExistingShapeFile();
		}

		ShapefileDataStore store = new ShapefileDataStore(existingSHPFile.toURI().toURL());
		String name = store.getTypeNames()[0];
		SimpleFeatureSource source = store.getFeatureSource(name);
		SimpleFeatureCollection fsShape = source.getFeatures();

		// print out a feature type header and wait for user input
		SimpleFeatureType featureType = (SimpleFeatureType) source.getSchema();
		System.out.println(featureType.toString());
		System.out.println();

		if (Argument.VERBOSE.isActive()) {
			printFeaturesAttributes(fsShape);
			printFeaturesGeometry(fsShape);
		}

		File newSHPFile;
		if (Argument.SHP_OUT.isActive()) {
			newSHPFile = new File(Argument.SHP_OUT.getValue());
		} else {
			newSHPFile = getNewShapeFile(existingSHPFile);
		}
		//copySHPFile(newSHPFile, featureType, fsShape);
		reprojectToLatLong(featureType, fsShape, newSHPFile);
		getMinMaxSHPFile(newSHPFile);

	}



}
