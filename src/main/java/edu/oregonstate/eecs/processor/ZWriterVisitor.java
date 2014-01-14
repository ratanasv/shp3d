package edu.oregonstate.eecs.processor;

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
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class ZWriterVisitor implements PipelineElementVisitor {
	private final File outputFile;
	private FeatureWriter<SimpleFeatureType, SimpleFeature> writer;
	private Transaction transaction;
	private DataStore dataStore;
	
	ZWriterVisitor(File file) {
		outputFile = file;
	}
	
	private static Map<String, Serializable> getDataStoreParams(File newSHPFile)
			throws MalformedURLException {
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", newSHPFile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);
		return params;
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

	@Override
	public void visit(PreTraversal element) {
		final SimpleFeatureType schema = element.getSchema();
		try {
			DataStoreFactorySpi factory = new ShapefileDataStoreFactory();
			dataStore = factory.createNewDataStore(getDataStoreParams(outputFile));
			dataStore.createSchema(schema);
			String[] typeNames = dataStore.getTypeNames();
			
			transaction = new DefaultTransaction("Write Z-values");
			writer = dataStore.getFeatureWriterAppend(typeNames[0], transaction);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		

	}

	@Override
	public void visit(Traversal element) {
		try {
			GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory( null );
			
			SimpleFeature sourceFeature = element.getFeature();
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
		} catch (IOException e){
			throw new RuntimeException(e.getMessage());
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
