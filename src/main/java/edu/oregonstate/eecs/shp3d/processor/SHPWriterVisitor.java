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
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;


abstract class SHPWriterVisitor implements PipelineElementVisitor {
	protected final File outputFile;
	protected FeatureWriter<SimpleFeatureType, SimpleFeature> writer;
	protected Transaction transaction;
	protected DataStore dataStore;
	
	SHPWriterVisitor(File file) {
		outputFile = file;
	}
	
	protected static Map<String, Serializable> getDataStoreParams(File newSHPFile)
			throws MalformedURLException {
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", newSHPFile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);
		return params;
	}
	
	abstract SimpleFeatureType getOutputSchema(SimpleFeatureType sourceSchema);
	abstract String getTransactionLabel();
	abstract void writeToFeature(SimpleFeature sourceFeature, SimpleFeature outFeature);

	@Override
	public void visit(PreTraversal element) {
		
		try {			
			final SimpleFeatureType sourceSchema = element.getSchema();
			DataStoreFactorySpi factory = new ShapefileDataStoreFactory();
			dataStore = factory.createNewDataStore(getDataStoreParams(outputFile));
			dataStore.createSchema(getOutputSchema(sourceSchema));
			String[] typeNames = dataStore.getTypeNames();
			
			transaction = new DefaultTransaction(getTransactionLabel());
			writer = dataStore.getFeatureWriterAppend(typeNames[0], transaction);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		

	}

	@Override
	public void visit(Traversal element) {
		try {
			SimpleFeature sourceFeature = element.getFeature();
			SimpleFeature outFeature = writer.next();
			outFeature.setAttributes(sourceFeature.getAttributes());
			writeToFeature(sourceFeature, outFeature);
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
