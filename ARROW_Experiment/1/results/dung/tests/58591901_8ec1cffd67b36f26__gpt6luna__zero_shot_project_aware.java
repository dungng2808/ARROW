package deepboof.impl.forward.standard;

import deepboof.PaddingType;
import deepboof.forward.ConfigPadding;
import deepboof.tensors.Tensor_F64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClippedPadding2D_F64_ArrowTest {
	@Test
	public void clippingOffsets() {
		ConfigPadding config = new ConfigPadding();
		config.type = PaddingType.CLIPPED;
		ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);

		assertEquals(0, padding.getClippingOffsetRow(0));
		assertEquals(0, padding.getClippingOffsetCol(0));
	}

	@Test
	public void clippedAndTensorType() {
		ConfigPadding config = new ConfigPadding();
		config.type = PaddingType.CLIPPED;
		ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);

		assertTrue(padding.isClipped());
		assertEquals(Tensor_F64.class, padding.getTensorType());
	}

	@Test
	public void rejectsOtherPaddingType() {
		ConfigPadding config = new ConfigPadding();
		config.type = PaddingType.ZERO;
		assertThrows(IllegalArgumentException.class, () -> new ClippedPadding2D_F64(config));
	}

	@Test
	public void borderGetThrows() {
		ConfigPadding config = new ConfigPadding();
		config.type = PaddingType.CLIPPED;
		ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);

		assertThrows(RuntimeException.class, () -> padding.borderGet(0, 0, 0, 0));
	}
}
