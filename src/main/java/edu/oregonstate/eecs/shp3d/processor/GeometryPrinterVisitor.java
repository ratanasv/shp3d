package edu.oregonstate.eecs.shp3d.processor;

import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.MultiPolygon;

public class GeometryPrinterVisitor implements PipelineElementVisitor {

	@Override
	public void visit(PreTraversal element) {
		
	}

	@Override
	public void visit(Traversal element) {
		SimpleFeature feature = element.getFeature();
		System.out.print(feature.getID() + "\t");
		MultiPolygon geometry = (MultiPolygon)feature.getDefaultGeometry();
		System.out.println(geometry);
		System.out.println();

	}

	@Override
	public void visit(PostTraversal element) {
		
	}

}
