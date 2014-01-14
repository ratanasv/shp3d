package edu.oregonstate.eecs.shp3d.processor;

interface PipelineElement {
	void accept(PipelineElementVisitor visitor);
}
