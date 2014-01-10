package edu.oregonstate.eecs.shp3d;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit test for simple App.
 */
public class AppTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
    public void testChangeExtension() {
        Assert.assertEquals("asdf.prj", App.changeExtension("asdf.shp", ".prj"));
        Assert.assertEquals("asdf.prj", App.changeExtension("asdf", ".prj"));
        
        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals("asdf.prj", App.changeExtension("asdf", "prj"));
    }

}
