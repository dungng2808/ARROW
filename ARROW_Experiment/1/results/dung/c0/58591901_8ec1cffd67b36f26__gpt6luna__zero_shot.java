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
	public void clippingOffsetsAndType() {
		ConfigPadding config = new ConfigPadding();
		config.type = PaddingType.CLIPPED;
		ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);

		assertEquals(0, padding.getClippingOffsetRow(0));
		assertEquals(0, padding.getClippingOffsetCol(0));
		assertTrue(padding.isClipped());
		assertEquals(Tensor_F64.class, padding.getTensorType());
	}

	@Test
	public void rejectsNonClippedConfiguration() {
		ConfigPadding config = new ConfigPadding();
		config.type = PaddingType.ZERO;

		try {
			new ClippedPadding2D_F64(config);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// Expected.
		}
	}

	@Test
	public void borderGetThrows() {
		ConfigPadding config = new ConfigPadding();
		config.type = PaddingType.CLIPPED;
		ClippedPadding2D_F64 padding = new ClippedPadding2D_F64(config);

		try {
			padding.borderGet(0, 0, 0, 0);
			fail("Expected RuntimeException");
		} catch (RuntimeException expected) {
			// Expected.
		}
	}
}
