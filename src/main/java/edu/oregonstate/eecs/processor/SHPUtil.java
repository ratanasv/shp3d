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
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.MultiPolygon;

public final class SHPUtil {
	
	public static SimpleFeatureSource getSource(File shpFile) throws IOException {
		ShapefileDataStore store = new ShapefileDataStore(shpFile.toURI().toURL());
		String name = store.getTypeNames()[0];
		SimpleFeatureSource source = store.getFeatureSource(name);
		return source;
	}
	
	private static Map<String, Serializable> getDataStoreParams(File newSHPFile)
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
}
