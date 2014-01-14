package edu.oregonstate.eecs.shp3d.processor;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.MultiPolygon;

class LatLongVisitor implements PipelineElementVisitor {
	private final File outputFile;
	private MathTransform transform;
	private FeatureWriter<SimpleFeatureType, SimpleFeature> writer;
	private Transaction transaction;
	private DataStore dataStore;
	
	LatLongVisitor(File file) {
		outputFile = file;
	}
	
	static Map<String, Serializable> getDataStoreParams(File newSHPFile)
			throws MalformedURLException {
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", newSHPFile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);
		return params;
	}
	
	@Override
	public void visit(PreTraversal element) {
		try {
			final SimpleFeatureType schema = element.getSchema();
			
			CoordinateReferenceSystem dataCRS = schema.getCoordinateReferenceSystem();
			CoordinateReferenceSystem worldCRS = DefaultGeographicCRS.WGS84;
			boolean lenient = true; // allow for some error due to different datums
			transform = CRS.findMathTransform(dataCRS, worldCRS, lenient);

			DataStoreFactorySpi factory = new ShapefileDataStoreFactory();
			dataStore = factory.createNewDataStore(getDataStoreParams(outputFile));
			SimpleFeatureType featureType = SimpleFeatureTypeBuilder.retype(schema, worldCRS);
			dataStore.createSchema(featureType);
			String[] typeNames = dataStore.getTypeNames();

			transaction = new DefaultTransaction("Reproject");
			writer = dataStore.getFeatureWriterAppend(typeNames[0], transaction);
		} catch (IOException error) {
			throw new RuntimeException(error.getMessage());
		} catch (FactoryException error) {
			throw new RuntimeException(error.getMessage());
		}

	}


	@Override
	public void visit(Traversal element) {
		try {
			SimpleFeature feature = element.getFeature();
			SimpleFeature copy = writer.next();
			copy.setAttributes(feature.getAttributes());
	
			MultiPolygon geometry = (MultiPolygon) feature.getDefaultGeometry();
			MultiPolygon geometry2 = (MultiPolygon) JTS.transform(geometry, transform);
	
			copy.setDefaultGeometry(geometry2);
			writer.write();
		} catch (IOException error) {
			throw new RuntimeException(error.getMessage());
		} catch (MismatchedDimensionException error) {
			throw new RuntimeException(error.getMessage());
		} catch (TransformException error) {
			throw new RuntimeException(error.getMessage());
		}
		
	}

	@Override
	public void visit(PostTraversal element) {
		try {
			writer.close();
			transaction.commit();
			transaction.close();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			dataStore.dispose();
		}
	}

}
