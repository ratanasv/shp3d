package edu.oregonstate.eecs.shp3d.processor;

public interface PipelineElementVisitor {
	void visit(PreTraversal element);
	void visit(Traversal element);
	void visit(PostTraversal element);
}
