package edu.oregonstate.eecs.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import edu.oregonstate.eecs.processor.DEMHeightField;

public class DEMHeightFieldTest {
	private static Logger logger = LogManager.getLogger();
	@Test
	public void testHeightField() throws IOException {
		final int numLats = 5;
		final int numLngs = 5;
		final float minLat = 40.95f;
		final float maxLat = 41.0f;
		final float minLng = -123.0f;
		final float maxLng = -122.95f;
		final float epsilon = 1e-8f;
		

		String urlString = DEMQueryBuilder.startBuilding(DEMConnection.Server.MIKES_DEM.getURL())
				.withLat1(minLat)
				.withLat2(maxLat)
				.withLng1(minLng)
				.withLng2(maxLng)
				.withNumLats(numLats)
				.withNumLngs(numLngs)
			.build();
		logger.info("request = {}", urlString);
		DEMConnection connection = new DEMConnection(urlString);
		DEMHeightField heights = new DEMHeightField(connection);
		DEMHeightField.XTRHeader header = heights.getXTRHeader();
		Assert.assertEquals(numLats, header.numLats);
		Assert.assertEquals(numLngs, header.numLngs);
		Assert.assertEquals(minLat, header.minLat, epsilon);
		Assert.assertEquals(maxLat, header.maxLat, epsilon);
		Assert.assertEquals(minLng, header.minLng, epsilon);
		Assert.assertEquals(maxLng, header.maxLng, epsilon);

		for (int i=0; i<header.numLats; i++) {
			for (int j=0; j<header.numLngs; j++) {
				Assert.assertEquals(2000.0f, heights.heightAtIndex(i, j), 1000.0f);
			}
		}
	}
}
