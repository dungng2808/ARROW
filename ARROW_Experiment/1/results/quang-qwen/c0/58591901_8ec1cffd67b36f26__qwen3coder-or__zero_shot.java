package deepboof.impl.forward.standard;

import deepboof.PaddingType;
import deepboof.forward.ConfigPadding;
import deepboof.impl.forward.standard.ClippedPadding2D_F64;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClippedPadding2D_F64_ArrowTest {

    @Test
    public void testConstructorValidConfig() {
        ConfigPadding config = new ConfigPadding();
        config.type = PaddingType.CLIPPED;
        ClippedPadding2D_F64 instance = new ClippedPadding2D_F64(config);
        assertNotNull(instance);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidConfig() {
        ConfigPadding config = new ConfigPadding();
        config.type = PaddingType.CONSTANT;
        new ClippedPadding2D_F64(config);
    }

    @Test(expected = RuntimeException.class)
    public void testBorderGet() {
        ConfigPadding config = new ConfigPadding();
        config.type = PaddingType.CLIPPED;
        ClippedPadding2D_F64 instance = new ClippedPadding2D_F64(config);
        instance.borderGet(0, 0, 0, 0);
    }

    @Test
    public void testGetClippingOffsetRow_ClippedBelow() {
        ConfigPadding config = new ConfigPadding();
        config.type = PaddingType.CLIPPED;
        ClippedPadding2D_F64 instance = new ClippedPadding2D_F64(config);
        
        // Simulate ROW0 = 0, ROW1 = 10 for testing purposes
        // We'll test with paddedRow < ROW0
        int result = instance.getClippingOffsetRow(-1);
        assertEquals(1, result); // ROW0 - paddedRow = 0 - (-1) = 1
    }

    @Test
    public void testGetClippingOffsetRow_ClippedAbove() {
        ConfigPadding config = new ConfigPadding();
        config.type = PaddingType.CLIPPED;
        ClippedPadding2D_F64 instance = new ClippedPadding2D_F64(config);
        
        // Simulate ROW0 = 0, ROW1 = 10 for testing purposes
        // We'll test with paddedRow > ROW1
        int result = instance.getClippingOffsetRow(11);
        assertEquals(-1, result); // ROW1 - paddedRow = 10 - 11 = -1
    }

    @Test
    public void testGetClippingOffsetRow_WithinBounds() {
        ConfigPadding config = new ConfigPadding();
        config.type = PaddingType.CLIPPED;
        ClippedPadding2D_F64 instance = new ClippedPadding2D_F64(config);
        
        // Simulate ROW0 = 0, ROW1 = 10 for testing purposes
        // We'll test with paddedRow between ROW0 and ROW1 inclusive
        int result = instance.getClippingOffsetRow(5);
        assertEquals(0, result); // Should return 0 when within bounds
    }

    @Test
    public void testGetClippingOffsetCol_ClippedBelow() {
        ConfigPadding config = new ConfigPadding();
        config.type = PaddingType.CLIPPED;
        ClippedPadding2D_F64 instance = new ClippedPadding2D_F64(config);
        
        // Simulate COL0 = 0, COL1 = 10 for testing purposes
        // We'll test with paddedCol < COL0
        int result = instance.getClippingOffsetCol(-1);
        assertEquals(1, result); // COL0 - paddedCol = 0 - (-1) = 1
    }

    @Test
    public void testGetClippingOffsetCol_ClippedAbove() {
        ConfigPadding config = new ConfigPadding();
        config.type = PaddingType.CLIPPED;
        ClippedPadding2D_F64 instance = new ClippedPadding2D_F64(config);
        
        // Simulate COL0 = 0, COL1 = 10 for testing purposes
        // We'll test with paddedCol > COL1
        int result = instance.getClippingOffsetCol(11);
        assertEquals(-1, result); // COL1 - paddedCol = 10 - 11 = -1
    }

    @Test
    public void testGetClippingOffsetCol_WithinBounds() {
        ConfigPadding config = new ConfigPadding();
        config.type = PaddingType.CLIPPED;
        ClippedPadding2D_F64 instance = new ClippedPadding2D_F64(config);
        
        // Simulate COL0 = 0, COL1 = 10 for testing purposes
        // We'll test with paddedCol between COL0 and COL1 inclusive
        int result = instance.getClippingOffsetCol(5);
        assertEquals(0, result); // Should return 0 when within bounds
    }

    @Test
    public void testIsClipped() {
        ConfigPadding config = new ConfigPadding();
        config.type = PaddingType.CLIPPED;
        ClippedPadding2D_F64 instance = new ClippedPadding2D_F64(config);
        assertTrue(instance.isClipped());
    }

    @Test
    public void testGetTensorType() {
        ConfigPadding config = new ConfigPadding();
        config.type = PaddingType.CLIPPED;
        ClippedPadding2D_F64 instance = new ClippedPadding2D_F64(config);
        assertEquals(deepboof.tensors.Tensor_F64.class, instance.getTensorType());
    }
}
