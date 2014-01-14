package edu.oregonstate.eecs.shp3d.processor;

import org.geotools.data.simple.SimpleFeatureIterator;

public class PostTraversal implements PipelineElement {
	private final SimpleFeatureIterator iterator;
	
	PostTraversal(SimpleFeatureIterator iterator) {
		this.iterator = iterator;
	}

	@Override
	public void accept(PipelineElementVisitor visitor) {
		visitor.visit(this);
	}
	
	public SimpleFeatureIterator getIterator() {
		return iterator;
	}

}
