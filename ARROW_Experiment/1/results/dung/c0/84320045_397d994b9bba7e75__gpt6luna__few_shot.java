package wtf.java9.graphics_bounds;

import java.awt.Rectangle;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class GraphicsBounds_ArrowTest {

    @Test
    public void createBoundsReturnsRectangle() {
        Rectangle bounds = GraphicsBounds.createBounds();
        assertNotNull(bounds);
    }
}
