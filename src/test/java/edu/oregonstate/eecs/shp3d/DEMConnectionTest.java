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

		String urlString = DEMQueryBuilder.startBuilding(MIKE_DEM)
				.withLat1(40.95f)
				.withLat2(41.0f)
				.withLng1(-122.95f)
				.withLng2(-123.0f)
				.withNumLats(numLats)
				.withNumLngs(numLngs)
			.build();

		DEMConnection connection = new DEMConnection(urlString);
		DEMConnection.XTRHeader header = connection.getXTRHeader();
		Assert.assertEquals(numLats, header.numLats);
		Assert.assertEquals(numLngs, header.numLngs);

		for (int i=0; i<header.numLats; i++) {
			for (int j=0; j<header.numLngs; j++) {
				System.out.print(String.valueOf(connection.heightAtIndex(i, j)) + " ");
			}
			System.out.println("");
		}
	}
}
