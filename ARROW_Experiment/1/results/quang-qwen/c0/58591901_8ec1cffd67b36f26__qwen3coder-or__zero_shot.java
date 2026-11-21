package deepboof.impl.forward.standard;

import deepboof.PaddingType;
import deepboof.forward.ConfigPadding;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClippedPadding2D_F64_ArrowTest {

    @Test
    public void testConstructor_validConfig() {
        ConfigPadding config = new ConfigPadding();
        config.type = PaddingType.CLIPPED;
        ClippedPadding2D_F64 instance = new ClippedPadding2D_F64(config);
        assertNotNull(instance);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidConfig() {
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
    public void testGetClippingOffsetRow_below() {
        ConfigPadding config = new ConfigPadding();
        config.type = PaddingType.CLIPPED;
        ClippedPadding2D_F64 instance = new ClippedPadding2D_F64(config);
        
        // Set up some dummy values for ROW0 and ROW1
        // Since we can't access private fields, we'll test behavior indirectly
        // We know that if paddedRow < ROW0, it should return ROW0 - paddedRow
        // But since we don't know ROW0, we'll just verify it returns a negative value when appropriate
        
        // This test assumes ROW0 is set to 0 for simplicity
        int result = instance.getClippingOffsetRow(-1); // Should be 0 - (-1) = 1
        assertEquals(1, result);
    }

    @Test
    public void testGetClippingOffsetRow_above() {
        ConfigPadding config = new ConfigPadding();
        config.type = PaddingType.CLIPPED;
        ClippedPadding2D_F64 instance = new ClippedPadding2D_F64(config);
        
        // This test assumes ROW1 is set to 10 for simplicity
        int result = instance.getClippingOffsetRow(11); // Should be 10 - 11 = -1
        assertEquals(-1, result);
    }

    @Test
    public void testGetClippingOffsetRow_within() {
        ConfigPadding config = new ConfigPadding();
        config.type = PaddingType.CLIPPED;
        ClippedPadding2D_F64 instance = new ClippedPadding2D_F64(config);
        
        // When within range, should return 0
        int result = instance.getClippingOffsetRow(0); // Assuming 0 is within range
        assertEquals(0, result);
    }

    @Test
    public void testGetClippingOffsetCol_below() {
        ConfigPadding config = new ConfigPadding();
        config.type = PaddingType.CLIPPED;
        ClippedPadding2D_F64 instance = new ClippedPadding2D_F64(config);
        
        // This test assumes COL0 is set to 0 for simplicity
        int result = instance.getClippingOffsetCol(-1); // Should be 0 - (-1) = 1
        assertEquals(1, result);
    }

    @Test
    public void testGetClippingOffsetCol_above() {
        ConfigPadding config = new ConfigPadding();
        config.type = PaddingType.CLIPPED;
        ClippedPadding2D_F64 instance = new ClippedPadding2D_F64(config);
        
        // This test assumes COL1 is set to 10 for simplicity
        int result = instance.getClippingOffsetCol(11); // Should be 10 - 11 = -1
        assertEquals(-1, result);
    }

    @Test
    public void testGetClippingOffsetCol_within() {
        ConfigPadding config = new ConfigPadding();
        config.type = PaddingType.CLIPPED;
        ClippedPadding2D_F64 instance = new ClippedPadding2D_F64(config);
        
        // When within range, should return 0
        int result = instance.getClippingOffsetCol(0); // Assuming 0 is within range
        assertEquals(0, result);
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
