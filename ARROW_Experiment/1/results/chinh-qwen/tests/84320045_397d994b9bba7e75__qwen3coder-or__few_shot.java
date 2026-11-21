package wtf.java9.graphics_bounds;

import java.awt.Rectangle;
import org.junit.Test;
import static org.junit.Assert.*;

public class GraphicsBounds_ArrowTest {

	@Test
	public void testCreateBounds() {
		Rectangle bounds = GraphicsBounds.createBounds();
		assertNotNull(bounds);
		assertEquals(800, bounds.width);
		assertEquals(600, bounds.height);
	}
}
