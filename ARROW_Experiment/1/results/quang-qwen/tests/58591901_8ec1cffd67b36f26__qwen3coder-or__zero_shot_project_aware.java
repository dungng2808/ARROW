package deepboof.impl.forward.standard;

import deepboof.PaddingType;
import deepboof.forward.ConfigPadding;
import deepboof.forward.SpatialPadding2D_F64;
import deepboof.tensors.Tensor_F64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClippedPadding2D_F64_ArrowTest {

    @Test
    public void testConstructor_ValidConfig() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 1, 1);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        assertNotNull(padding);
    }

    @Test
    public void testConstructor_InvalidConfig() {
        ConfigPadding config = new ConfigPadding(PaddingType.CONSTANT, 1, 1);
        assertThrows(IllegalArgumentException.class, () -> {
            new ClippedPadding2D_F64(config);
        });
    }

    @Test
    public void testBorderGet_ThrowsException() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 1, 1);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        assertThrows(RuntimeException.class, () -> {
            padding.borderGet(0, 0, 0, 0);
        });
    }

    @Test
    public void testGetClippingOffsetRow_BelowBounds() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 1, 1);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        // Assuming ROW0 = 1, ROW1 = 1 based on default constructor behavior
        assertEquals(0, padding.getClippingOffsetRow(1)); // At boundary
        assertEquals(1, padding.getClippingOffsetRow(0)); // Below bounds
        assertEquals(0, padding.getClippingOffsetRow(2)); // Above bounds
    }

    @Test
    public void testGetClippingOffsetRow_AboveBounds() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 1, 1);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        assertEquals(0, padding.getClippingOffsetRow(1)); // At boundary
        assertEquals(-1, padding.getClippingOffsetRow(2)); // Above bounds
    }

    @Test
    public void testGetClippingOffsetCol_BelowBounds() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 1, 1);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        // Assuming COL0 = 1, COL1 = 1 based on default constructor behavior
        assertEquals(0, padding.getClippingOffsetCol(1)); // At boundary
        assertEquals(1, padding.getClippingOffsetCol(0)); // Below bounds
        assertEquals(0, padding.getClippingOffsetCol(2)); // Above bounds
    }

    @Test
    public void testGetClippingOffsetCol_AboveBounds() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 1, 1);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        assertEquals(0, padding.getClippingOffsetCol(1)); // At boundary
        assertEquals(-1, padding.getClippingOffsetCol(2)); // Above bounds
    }

    @Test
    public void testIsClipped_ReturnsTrue() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 1, 1);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        assertTrue(padding.isClipped());
    }

    @Test
    public void testGetTensorType_ReturnsCorrectClass() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 1, 1);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        assertEquals(Tensor_F64.class, padding.getTensorType());
    }
}
