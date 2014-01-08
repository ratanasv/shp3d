package edu.oregonstate.eecs.shp3d;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

final class ProjectionUtil {
	static CoordinateReferenceSystem parsePRJ(String prjFile) throws FactoryException {
		return CRS.parseWKT(prjFile);
	}
	
	static MathTransform getMathTransform(CoordinateReferenceSystem sourceCRS, 
		CoordinateReferenceSystem targetCRS) throws FactoryException 
	{
		return CRS.findMathTransform(sourceCRS, targetCRS);
	}
	
	static MathTransform getTransformToLatLong(CoordinateReferenceSystem sourceCRS) 
			throws FactoryException 
	{
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
		return CRS.findMathTransform(sourceCRS, targetCRS);
	}
	
	static double[] transformToLatLong(double[] inputCoords,
			CoordinateReferenceSystem sourceCRS) throws TransformException, FactoryException 
	{
		MathTransform mathTransform = getTransformToLatLong(sourceCRS);
		final int totalComponents = inputCoords.length;
		
		double latLongPoints[] = new double[totalComponents];
		mathTransform.transform(inputCoords, 0, latLongPoints, 0, totalComponents);
		
		return latLongPoints;
	}
	
}
