package edu.oregonstate.eecs.shp3d.processor;

import java.io.IOException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;


public class Pipeliner {
	private final SimpleFeatureSource source;
	
	public Pipeliner(SimpleFeatureSource source) {
		this.source = source;
	}
	
	
	
	public void start(PipelineElementVisitor visitor) 
			throws IOException {

		PipelineElement preTraversal = new PreTraversal(source.getSchema());
		preTraversal.accept(visitor);
		
		final SimpleFeatureCollection featureCollection = source.getFeatures();
		SimpleFeatureIterator iterator = featureCollection.features();

		try {
			while (iterator.hasNext()) {
				SimpleFeature feature = iterator.next();
				PipelineElement traversal = new Traversal(feature);
				traversal.accept(visitor);
			}
			
		} finally {
			PipelineElement postTraversal = new PostTraversal(iterator);
			postTraversal.accept(visitor);
			iterator.close();
		}
		
	}
}
