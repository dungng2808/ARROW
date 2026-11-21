package deepboof.impl.forward.standard;

import deepboof.PaddingType;
import deepboof.forward.ConfigPadding;
import deepboof.impl.forward.standard.ClippedPadding2D_F64;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClippedPadding2D_F64_ArrowTest {

    @Test
    public void testConstructorValidConfig() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 0.0);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        assertNotNull(padding);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidConfig() {
        ConfigPadding config = new ConfigPadding(PaddingType.CONSTANT, 0.0);
        new ClippedPadding2D_F64(config);
    }

    @Test
    public void testBorderGet() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 0.0);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        
        try {
            padding.borderGet(0, 0, 0, 0);
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertEquals("The border is clipped and this function should never be called", e.getMessage());
        }
    }

    @Test
    public void testGetClippingOffsetRow() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 0.0);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        
        // Test case where paddedRow < ROW0
        int offset1 = padding.getClippingOffsetRow(-1);
        assertEquals(1, offset1);
        
        // Test case where paddedRow > ROW1
        int offset2 = padding.getClippingOffsetRow(10);
        assertEquals(-10, offset2);
        
        // Test case where paddedRow is between ROW0 and ROW1
        int offset3 = padding.getClippingOffsetRow(0);
        assertEquals(0, offset3);
    }

    @Test
    public void testGetClippingOffsetCol() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 0.0);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        
        // Test case where paddedCol < COL0
        int offset1 = padding.getClippingOffsetCol(-1);
        assertEquals(1, offset1);
        
        // Test case where paddedCol > COL1
        int offset2 = padding.getClippingOffsetCol(10);
        assertEquals(-10, offset2);
        
        // Test case where paddedCol is between COL0 and COL1
        int offset3 = padding.getClippingOffsetCol(0);
        assertEquals(0, offset3);
    }

    @Test
    public void testIsClipped() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 0.0);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        assertTrue(padding.isClipped());
    }

    @Test
    public void testGetTensorType() {
        ConfigPadding config = new ConfigPadding(PaddingType.CLIPPED, 0.0);
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);
        assertEquals(deepboof.tensors.Tensor_F64.class, padding.getTensorType());
    }
}
