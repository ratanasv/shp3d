package edu.oregonstate.eecs.processor;

public class PostTraversal implements PipelineElement {

	@Override
	public void accept(PipelineElementVisitor visitor) {
		visitor.visit(this);
	}

}
