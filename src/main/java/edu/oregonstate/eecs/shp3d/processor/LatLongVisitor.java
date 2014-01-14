package edu.oregonstate.eecs.shp3d.processor;

import java.io.File;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.MultiPolygon;

class LatLongVisitor extends SHPWriterVisitor implements PipelineElementVisitor {
	private MathTransform transform;
	
	LatLongVisitor(File file) {
		super(file);
	}

	@Override
	SimpleFeatureType getOutputSchema(SimpleFeatureType sourceSchema) {
		CoordinateReferenceSystem dataCRS = sourceSchema.getCoordinateReferenceSystem();
		CoordinateReferenceSystem worldCRS = DefaultGeographicCRS.WGS84;
		SimpleFeatureType outSchema = SimpleFeatureTypeBuilder.retype(sourceSchema, worldCRS);
		boolean lenient = true;
		try {
			transform = CRS.findMathTransform(dataCRS, worldCRS, lenient);
		} catch (FactoryException e) {
			throw new RuntimeException(e.getMessage());
		}
		return outSchema;
	}

	@Override
	String getTransactionLabel() {
		return "LatLong Projection";
	}

	@Override
	void writeToFeature(SimpleFeature sourceFeature, SimpleFeature outFeature) {
        outFeature.setAttributes(sourceFeature.getAttributes());

        MultiPolygon geometry = (MultiPolygon) sourceFeature.getDefaultGeometry();
        MultiPolygon geometry2;
		try {
			geometry2 = (MultiPolygon) JTS.transform(geometry, transform);
		} catch (MismatchedDimensionException | TransformException e) {
			throw new RuntimeException(e.getMessage());
		}

        outFeature.setDefaultGeometry(geometry2);
	}

}
