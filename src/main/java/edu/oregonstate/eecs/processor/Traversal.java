package edu.oregonstate.eecs.processor;

import org.opengis.feature.simple.SimpleFeature;

public class Traversal implements PipelineElement {
	private final SimpleFeature feature;
	
	Traversal(SimpleFeature feature) {
		this.feature = feature;
	}
	
	SimpleFeature getFeature() {
		return feature;
	}

	@Override
	public void accept(PipelineElementVisitor visitor) {
		visitor.visit(this);
	}

}
