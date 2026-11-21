package wtf.java9.graphics_bounds;

import org.junit.jupiter.api.Test;
import java.awt.Rectangle;
import static org.assertj.core.api.Assertions.assertThat;

public class GraphicsBounds_ArrowTest {

    @Test
    public void testCreateBoundsReturnsExpectedSize() {
        Rectangle bounds = GraphicsBounds.createBounds();
        assertThat(bounds.width).isEqualTo(800);
        assertThat(bounds.height).isEqualTo(600);
    }

    @Test
    public void testCreateBoundsReturnsNonNullRectangle() {
        Rectangle bounds = GraphicsBounds.createBounds();
        assertThat(bounds).isNotNull();
    }

    @Test
    public void testCreateBoundsReturnsRectangleWithPositiveDimensions() {
        Rectangle bounds = GraphicsBounds.createBounds();
        assertThat(bounds.width).isGreaterThan(0);
        assertThat(bounds.height).isGreaterThan(0);
    }
}
