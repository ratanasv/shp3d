package edu.oregonstate.eecs.shp3d.processor;

import org.junit.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import edu.oregonstate.eecs.shp3d.processor.DEMQueryBuilder;

public class DEMQueryBuilderTest {
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Test
	public void testBuilder() {
		String actual = DEMQueryBuilder.startBuilding("http://foo?")
				.withLat1(0)
				.withLat2(1)
				.withLng1(2)
				.withLng2(3)
				.withNumLats(4)
				.withNumLngs(5)
			.build();
		Assert.assertEquals("http://foo?numlats=4&numlngs=5&lat1=0.0&lat2=1.0&lng1=2.0&lng2=3.0", 
				actual);
		
		exception.expect(IllegalStateException.class);
		actual = DEMQueryBuilder.startBuilding("http://foo?")
				.withLat1(0)
				.withLat2(1)
				.withLng1(2)
				.withLng2(3)
				.withNumLats(4)
			.build();
		
		exception.expect(IllegalArgumentException.class);
		actual = DEMQueryBuilder.startBuilding("http://foo?")
				.withLat1(0)
				.withLat2(1)
				.withLng1(2)
				.withLng2(3)
				.withNumLats(4)
			.build();
	}
}
