package deepboof.impl.forward.standard;

import deepboof.PaddingType;
import deepboof.forward.ConfigPadding;
import deepboof.tensors.Tensor_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ClippedPadding2D_F64_ArrowTest {

    @Test
    public void reportsClippedAndTensorType() {
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(new ConfigPadding(PaddingType.CLIPPED));
        assertTrue(padding.isClipped());
        assertEquals(Tensor_F64.class, padding.getTensorType());
    }

    @Test
    public void clippingOffsetsForRows() {
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(new ConfigPadding(PaddingType.CLIPPED));
        assertEquals(2, padding.getClippingOffsetRow(-2));
        assertEquals(0, padding.getClippingOffsetRow(0));
        assertEquals(-2, padding.getClippingOffsetRow(2));
    }

    @Test
    public void clippingOffsetsForColumns() {
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(new ConfigPadding(PaddingType.CLIPPED));
        assertEquals(2, padding.getClippingOffsetCol(-2));
        assertEquals(0, padding.getClippingOffsetCol(0));
        assertEquals(-2, padding.getClippingOffsetCol(2));
    }

    @Test
    public void borderGetThrows() {
        ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(new ConfigPadding(PaddingType.CLIPPED));
        try {
            padding.borderGet(0, 0, 0, 0);
            fail("Expected borderGet to throw");
        } catch (RuntimeException expected) {
            assertEquals("The border is clipped and this function should never be called", expected.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonClippedConfiguration() {
        new ClippedPadding2D_F64(new ConfigPadding(PaddingType.ZERO));
    }
}
