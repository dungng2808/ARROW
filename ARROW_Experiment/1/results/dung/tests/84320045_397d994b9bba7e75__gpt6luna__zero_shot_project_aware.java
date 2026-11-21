package wtf.java9.graphics_bounds;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Rectangle;
import org.junit.jupiter.api.Test;

public class GraphicsBounds_ArrowTest {

    @Test
    public void createBoundsReturnsDeviceConfigurationBounds() {
        Rectangle bounds = GraphicsBounds.createBounds();

        assertThat(bounds).isNotNull();
        assertThat(bounds.width).isGreaterThan(0);
        assertThat(bounds.height).isGreaterThan(0);
    }
}
