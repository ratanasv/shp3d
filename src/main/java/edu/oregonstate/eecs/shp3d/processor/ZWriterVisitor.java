package edu.oregonstate.eecs.shp3d.processor;

import java.io.File;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class ZWriterVisitor extends SHPWriterVisitor implements PipelineElementVisitor {
	
	ZWriterVisitor(File file) {
		super(file);
	}
	
	
	private static Coordinate[] addZWithRepair(Coordinate[] source) {
		final int length = source.length;
		Coordinate[] output; 
		if (source[0] != source[length-1]) {
			output = new Coordinate[length+1];
			output[length] = new Coordinate(source[0].x, source[0].y, 3.14f);
		} else {
			output = new Coordinate[length];
		}
		for (int i=0; i<length; i++) {
			output[i] = new Coordinate(source[i].x, source[i].y, 3.14);
		}
		return output;
	}

	@Override
	SimpleFeatureType getOutputSchema(SimpleFeatureType sourceSchema) {
		return sourceSchema;
	}

	@Override
	String getTransactionLabel() {
		return "Writing Z-values";
	}

	@Override
	void writeToFeature(SimpleFeature sourceFeature, SimpleFeature outFeature) {
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory( null );

        MultiPolygon sourceMultiPolygon = (MultiPolygon)sourceFeature.getDefaultGeometry();
        Polygon outPolygons[] = new Polygon[sourceMultiPolygon.getNumGeometries()];
        for (int i=0; i<sourceMultiPolygon.getNumGeometries(); i++) {
                Geometry sourceGeometry = sourceMultiPolygon.getGeometryN(i);
                Coordinate[] sourceCoords = sourceGeometry.getCoordinates();
                Coordinate[] outCoords = addZWithRepair(sourceCoords);

                LinearRing ring = geometryFactory.createLinearRing( outCoords );
                outPolygons[i] = geometryFactory.createPolygon(ring, null );

        }
        MultiPolygon outMultiPolygon = geometryFactory.createMultiPolygon(outPolygons);
        outFeature.setDefaultGeometry(outMultiPolygon);
	}

}
