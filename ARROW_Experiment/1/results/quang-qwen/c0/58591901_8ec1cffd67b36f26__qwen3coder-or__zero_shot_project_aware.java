package deepboof.impl.forward.standard;

import deepboof.PaddingType;
import deepboof.forward.ConfigPadding;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClippedPadding2D_F64_ArrowTest {

    @Test
    public void testConstructor_ValidConfig() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 1, 1, 1, 1);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        
        assertNotNull(padding);
    }

    @Test
    public void testConstructor_InvalidConfig() {
        ConfigPadding config = new ConfigPadding(PaddingType.CONSTANT, 1, 1, 1, 1);
        
        assertThrows(IllegalArgumentException.class, () -> {
            new ClippedPadding2D_F64(config);
        });
    }

    @Test
    public void testBorderGet_ThrowsException() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 1, 1, 1, 1);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        
        assertThrows(RuntimeException.class, () -> {
            padding.borderGet(0, 0, 0, 0);
        });
    }

    @Test
    public void testGetClippingOffsetRow_BelowBounds() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 1, 1, 1, 1);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        
        // Assuming ROW0 = 1, ROW1 = 1 based on config
        int offset = padding.getClippingOffsetRow(0); // paddedRow < ROW0
        assertEquals(1, offset); // 1 - 0 = 1
    }

    @Test
    public void testGetClippingOffsetRow_AboveBounds() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 1, 1, 1, 1);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        
        // Assuming ROW0 = 1, ROW1 = 1 based on config
        int offset = padding.getClippingOffsetRow(2); // paddedRow > ROW1
        assertEquals(-1, offset); // 1 - 2 = -1
    }

    @Test
    public void testGetClippingOffsetRow_WithinBounds() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 1, 1, 1, 1);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        
        // Assuming ROW0 = 1, ROW1 = 1 based on config
        int offset = padding.getClippingOffsetRow(1); // paddedRow within bounds
        assertEquals(0, offset);
    }

    @Test
    public void testGetClippingOffsetCol_BelowBounds() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 1, 1, 1, 1);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        
        // Assuming COL0 = 1, COL1 = 1 based on config
        int offset = padding.getClippingOffsetCol(0); // paddedCol < COL0
        assertEquals(1, offset); // 1 - 0 = 1
    }

    @Test
    public void testGetClippingOffsetCol_AboveBounds() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 1, 1, 1, 1);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        
        // Assuming COL0 = 1, COL1 = 1 based on config
        int offset = padding.getClippingOffsetCol(2); // paddedCol > COL1
        assertEquals(-1, offset); // 1 - 2 = -1
    }

    @Test
    public void testGetClippingOffsetCol_WithinBounds() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 1, 1, 1, 1);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        
        // Assuming COL0 = 1, COL1 = 1 based on config
        int offset = padding.getClippingOffsetCol(1); // paddedCol within bounds
        assertEquals(0, offset);
    }

    @Test
    public void testIsClipped_ReturnsTrue() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 1, 1, 1, 1);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        
        assertTrue(padding.isClipped());
    }

    @Test
    public void testGetTensorType_ReturnsCorrectClass() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 1, 1, 1, 1);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        
        assertEquals(org.openjdk.jmh.annotations.State.class, padding.getTensorType());
    }
}
