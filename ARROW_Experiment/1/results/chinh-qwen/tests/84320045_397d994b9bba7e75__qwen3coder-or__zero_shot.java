package wtf.java9.graphics_bounds;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;

import org.junit.Test;
import static org.junit.Assert.*;

public class GraphicsBounds_ArrowTest {

	@Test
	public void testCreateBounds() {
		Rectangle bounds = GraphicsBounds.createBounds();
		
		// Verify that the returned rectangle has the expected dimensions
		assertEquals(800, bounds.width);
		assertEquals(600, bounds.height);
		assertEquals(0, bounds.x);
		assertEquals(0, bounds.y);
	}
	
	@Test
	public void testCreateBoundsReturnsNewInstance() {
		Rectangle bounds1 = GraphicsBounds.createBounds();
		Rectangle bounds2 = GraphicsBounds.createBounds();
		
		// Verify that we get different instances each time
		assertNotSame(bounds1, bounds2);
	}
	
	@Test
	public void testCreateBoundsConsistency() {
		Rectangle bounds1 = GraphicsBounds.createBounds();
		Rectangle bounds2 = GraphicsBounds.createBounds();
		
		// Verify that the results are consistent
		assertEquals(bounds1.width, bounds2.width);
		assertEquals(bounds1.height, bounds2.height);
		assertEquals(bounds1.x, bounds2.x);
		assertEquals(bounds1.y, bounds2.y);
	}
}
