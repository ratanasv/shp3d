package edu.oregonstate.eecs.processor;

interface PipelineElementVisitor {
	void visit(PreTraversal element);
	void visit(Traversal element);
	void visit(PostTraversal element);
}
