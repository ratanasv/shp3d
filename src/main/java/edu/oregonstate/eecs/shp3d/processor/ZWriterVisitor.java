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
	private double minZ = Double.MAX_VALUE;
	private double maxZ = Double.MIN_VALUE;

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

	private Coordinate[] addZ(Coordinate[] source) {
		final int length = source.length;
		Coordinate[] output = new Coordinate[length];
		for (int i=0; i<length; i++) {
			final double longtitude = source[i].x;
			final double latitude = source[i].y;
			float height = heightField.heightAt(longtitude, latitude);
			output[i] = new Coordinate(longtitude, latitude, height);
			if (height > maxZ) {
				maxZ = height;
			}
			if (height < minZ) {
				minZ = height;
			}
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
		if (sourceMultiPolygon.getNumGeometries() != 1) {
			throw new RuntimeException("wtf");
		}
		for (int i=0; i<sourceMultiPolygon.getNumGeometries(); i++) {
			Polygon sourcePolygon = (Polygon) sourceMultiPolygon.getGeometryN(i);

			Coordinate[] sourceExteriorCoord = sourcePolygon.getExteriorRing().getCoordinates();
			Coordinate[] outExteriorCoord = addZ(sourceExteriorCoord);

			LinearRing[] outInteriorRings = addZInteriorRings(geometryFactory, sourcePolygon);

			LinearRing outExteriorRing = geometryFactory.createLinearRing( outExteriorCoord );
			outPolygons[i] = geometryFactory.createPolygon(outExteriorRing, outInteriorRings );
		}
		MultiPolygon outMultiPolygon = geometryFactory.createMultiPolygon(outPolygons);
		outFeature.setDefaultGeometry(outMultiPolygon);
	}

	private LinearRing[] addZInteriorRings(GeometryFactory geometryFactory,
			Polygon sourcePolygon) {
		final int numInteriorRing = sourcePolygon.getNumInteriorRing();
		LinearRing[] outInteriorRings = new LinearRing[numInteriorRing];

		for (int j=0; j<numInteriorRing; j++) {
			Coordinate[] sourceInteriorCoords = sourcePolygon.getInteriorRingN(j).getCoordinates();
			Coordinate[] outInteriorCoords = addZ(sourceInteriorCoords);
			outInteriorRings[j] = geometryFactory.createLinearRing(outInteriorCoords);
		}
		return outInteriorRings;
	}
	
	public double getMinZ() {
		if (this.minZ == Double.MAX_VALUE) {
			throw new RuntimeException("minZ has not been set yet.");
		}
		return this.minZ;
	}
	
	public double getMaxZ() {
		if (this.maxZ == Double.MIN_VALUE) {
			throw new RuntimeException("maxZ has not been set yet.");
		}
		return this.maxZ;
	}

}
