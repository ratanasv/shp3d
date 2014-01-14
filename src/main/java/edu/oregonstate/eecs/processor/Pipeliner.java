package edu.oregonstate.eecs.processor;

import java.io.IOException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;


class Pipeliner {
	static void Start(final SimpleFeatureSource source, PipelineElementVisitor visitor) 
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
			PipelineElement postTraversal = new PostTraversal();
			postTraversal.accept(visitor);
			iterator.close();
		}
		
	}
}
