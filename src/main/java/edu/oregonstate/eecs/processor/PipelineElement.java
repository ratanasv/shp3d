package edu.oregonstate.eecs.processor;

interface PipelineElement {
	void accept(PipelineElementVisitor visitor);
}
