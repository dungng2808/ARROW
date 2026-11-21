package wtf.java9.graphics_bounds;

import static org.junit.Assert.assertNotNull;

import java.awt.Rectangle;
import org.junit.Test;

public class GraphicsBounds_ArrowTest {

    @Test
    public void createBoundsReturnsRectangle() {
        Rectangle bounds = GraphicsBounds.createBounds();

        assertNotNull(bounds);
    }
}
