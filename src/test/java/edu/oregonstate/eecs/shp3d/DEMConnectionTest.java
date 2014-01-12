package edu.oregonstate.eecs.shp3d;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class DEMConnectionTest {
	private static final String MIKE_DEM = "http://maverick.coas.oregonstate.edu:11300/" +
			"terrainextraction.ashx?";
	@Test
	public void testConnection() throws IOException {
		final int numLats = 10;
		final int numLngs = 10;
		final float minLat = 40.95f;
		final float maxLat = 41.0f;
		final float minLng = -123.0f;
		final float maxLng = -122.95f;
		final float epsilon = 1e-8f;
		

		String urlString = DEMQueryBuilder.startBuilding(MIKE_DEM)
				.withLat1(minLat)
				.withLat2(maxLat)
				.withLng1(minLng)
				.withLng2(maxLng)
				.withNumLats(numLats)
				.withNumLngs(numLngs)
			.build();

		DEMConnection connection = new DEMConnection(urlString);
		DEMConnection.XTRHeader header = connection.getXTRHeader();
		Assert.assertEquals(numLats, header.numLats);
		Assert.assertEquals(numLngs, header.numLngs);
		Assert.assertEquals(minLat, header.minLat, epsilon);
		Assert.assertEquals(maxLat, header.maxLat, epsilon);
		Assert.assertEquals(minLng, header.minLng, epsilon);
		Assert.assertEquals(maxLng, header.maxLng, epsilon);

		for (int i=0; i<header.numLats; i++) {
			for (int j=0; j<header.numLngs; j++) {
				System.out.print(String.valueOf(connection.heightAtIndex(i, j)) + " ");
			}
			System.out.println("");
		}
	}
}
