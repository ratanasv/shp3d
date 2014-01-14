package edu.oregonstate.eecs.processor;

import org.opengis.feature.simple.SimpleFeatureType;

class PreTraversal implements PipelineElement {
	private final SimpleFeatureType schema;
	
	PreTraversal(final SimpleFeatureType source) {
		this.schema = source;
	}

	@Override
	public void accept(PipelineElementVisitor visitor) {
		visitor.visit(this);
	}

	SimpleFeatureType getSchema() {
		return schema;
	}
	
}
