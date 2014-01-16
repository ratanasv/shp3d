package edu.oregonstate.eecs.shp3d.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

public final class IncompleteRingVisitor implements PipelineElementVisitor {
	private static Logger logger = LogManager.getLogger();

	@Override
	public void visit(PreTraversal element) {

	}

	@Override
	public void visit(Traversal element) {
		SimpleFeature sourceFeature = element.getFeature();
		MultiPolygon sourceMultiPolygon = (MultiPolygon)sourceFeature.getDefaultGeometry();
		for (int i=0; i<sourceMultiPolygon.getNumGeometries(); i++) {
			Geometry sourceGeometry = sourceMultiPolygon.getGeometryN(i);
			Coordinate[] sourceCoords = sourceGeometry.getCoordinates();
			final int length = sourceCoords.length;
			if (!sourceCoords[0].equals2D(sourceCoords[length-1])) {
				logger.info("Geometry does not form a closed loop. {} part#{} coords={}", 
						sourceFeature.getID(), i, sourceCoords);
			}
		}

	}

	@Override
	public void visit(PostTraversal element) {

	}

}
