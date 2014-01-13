package edu.oregonstate.eecs.processor;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

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
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public final class SHPUtil {
	
	public static SimpleFeatureSource getSource(File shpFile) throws IOException {
		ShapefileDataStore store = new ShapefileDataStore(shpFile.toURI().toURL());
		String name = store.getTypeNames()[0];
		SimpleFeatureSource source = store.getFeatureSource(name);
		return source;
	}
	
	public static Map<String, Serializable> getDataStoreParams(File newSHPFile)
			throws MalformedURLException {
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", newSHPFile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);
		return params;
	}
	
	public static void reprojectToLatLong(
			final SimpleFeatureSource source,
			File file) throws NoSuchAuthorityCodeException, FactoryException, IOException {
		
		final SimpleFeatureType schema = source.getSchema();
		final SimpleFeatureCollection featureCollection = source.getFeatures();
		
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
	
	public static ShapefileHeader getShapefileHeader(File blah) 
			throws ShapefileException, MalformedURLException, IOException {
		
		ShapefileReader reader = new ShapefileReader(new ShpFiles(blah), true, false, 
				null);
		ShapefileHeader header = reader.getHeader();
		reader.close();
		return header;
	}
	
	private static Coordinate[] addZWithRepair(Coordinate[] source) {
		final int length = source.length;
		Coordinate[] output; 
		if (source[0] != source[length-1]) {
			output = new Coordinate[length+1];
			output[length] = new Coordinate(source[0].x, source[0].y, 3.14f);
		} else {
			output = new Coordinate[length];
		}
		for (int i=0; i<length; i++) {
			output[i] = new Coordinate(source[i].x, source[i].y, 3.14);
		}
		return output;
	}
	
	public static SimpleFeatureCollection fillWithBogusZ(
			final SimpleFeatureSource source, 
			final File outputFile) throws IOException {
		
		final SimpleFeatureType schema = source.getSchema();
		final SimpleFeatureCollection collection = source.getFeatures();
		
		DataStoreFactorySpi factory = new ShapefileDataStoreFactory();
		DataStore dataStore = factory.createNewDataStore(getDataStoreParams(outputFile));
		dataStore.createSchema(schema);
		String[] typeNames = dataStore.getTypeNames();
		
		Transaction transaction = new DefaultTransaction("Write Z-values");
		FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
				dataStore.getFeatureWriterAppend(typeNames[0], transaction);
		SimpleFeatureIterator iterator = collection.features();
		
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory( null );	
		try {
			while( iterator.hasNext() ){
				SimpleFeature sourceFeature = iterator.next();
				SimpleFeature outFeature = writer.next();
				outFeature.setAttributes(sourceFeature.getAttributes());

				MultiPolygon sourceMultiPolygon = (MultiPolygon)sourceFeature.getDefaultGeometry();
				Polygon outPolygons[] = new Polygon[sourceMultiPolygon.getNumGeometries()];
				for (int i=0; i<sourceMultiPolygon.getNumGeometries(); i++) {
					Geometry sourceGeometry = sourceMultiPolygon.getGeometryN(i);
					Coordinate[] sourceCoords = sourceGeometry.getCoordinates();
					Coordinate[] outCoords = addZWithRepair(sourceCoords);

					LinearRing ring = geometryFactory.createLinearRing( outCoords );
					outPolygons[i] = geometryFactory.createPolygon(ring, null );

				}
				MultiPolygon outMultiPolygon = geometryFactory.createMultiPolygon(outPolygons);
				outFeature.setDefaultGeometry(outMultiPolygon);
				writer.write();
			}
			writer.close();
			transaction.commit();
			JOptionPane.showMessageDialog(null, "BogusZ complete");
		}
		finally {
			writer.close();
			iterator.close();
			transaction.close();
			dataStore.dispose();
		}

		return collection;
	}
}
