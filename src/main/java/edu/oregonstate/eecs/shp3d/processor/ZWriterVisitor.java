package edu.oregonstate.eecs.shp3d.processor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileHeader;
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
	private final ShapefileHeader header;
	private static Logger logger = LogManager.getLogger();
	private final HeightField heightField;
	private final int numLats = 2048;
	private final int numLngs = 2048;

	ZWriterVisitor(ShapefileHeader header, File file) 
			throws ShapefileException, MalformedURLException, IOException {
		
		super(file);
		this.header = header;
		heightField = heightFieldFactory();
	}

	private HeightField heightFieldFactory() throws IOException {
		String urlString = DEMQueryBuilder.startBuilding(DEMConnection.Server.MIKES_DEM.getURL())
				.withLat1((float) header.minY())
				.withLat2((float) header.maxY())
				.withLng1((float) header.minX())
				.withLng2((float) header.maxX())
				.withNumLats(numLats)
				.withNumLngs(numLngs)
			.build();
		logger.info("Connecting to DEM server with Query {}. This will take awhile...", 
			urlString);
		DEMConnection connection = new DEMConnection(urlString);
		logger.info("DEM data received, begin parsing...");
		return new DEMHeightField(connection);
	}

	private Coordinate[] addZWithRepair(Coordinate[] source) {
		final int length = source.length;
		Coordinate[] output; 
		if (!source[0].equals2D(source[length-1])) {
			final double longtitude = source[0].x;
			final double latitude = source[0].y;
			float heightAt0 = heightField.heightAt(longtitude, latitude);
			output = new Coordinate[length+1];
			output[length] = new Coordinate(longtitude, latitude, heightAt0);
		} else {
			output = new Coordinate[length];
		}
		for (int i=0; i<length; i++) {
			final double longtitude = source[i].x;
			final double latitude = source[i].y;
			float height = heightField.heightAt(longtitude, latitude);
			output[i] = new Coordinate(longtitude, latitude, height);
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

			if (sourceCoords.length != outCoords.length) {
				logger.error("Geometry does not form a closed loop {}. Data = {}", 
						sourceFeature.getID(), sourceCoords);
			}

			LinearRing ring = geometryFactory.createLinearRing( outCoords );
			outPolygons[i] = geometryFactory.createPolygon(ring, null );
		}
		MultiPolygon outMultiPolygon = geometryFactory.createMultiPolygon(outPolygons);
		outFeature.setDefaultGeometry(outMultiPolygon);
	}

}
