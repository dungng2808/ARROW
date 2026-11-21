package brown.tracingplane.atomlayer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ByteBuffers_ArrowTest {

    @Test
    public void copyToCopiesRemainingBytesWithoutChangingSourcePosition() {
        ByteBuffer src = ByteBuffer.wrap(new byte[] {1, 2, 3, 4});
        src.position(1);
        src.limit(3);
        ByteBuffer dest = ByteBuffer.allocate(4);

        ByteBuffers.copyTo(src, dest);

        assertEquals(1, src.position());
        assertEquals(2, dest.position());
        assertArrayEquals(new byte[] {2, 3}, Arrays.copyOf(dest.array(), 2));
    }

    @Test
    public void copyToCopiesFromDirectBuffer() {
        ByteBuffer src = ByteBuffer.allocateDirect(3);
        src.put(new byte[] {5, 6, 7});
        src.flip();
        src.position(1);
        ByteBuffer dest = ByteBuffer.allocate(2);

        ByteBuffers.copyTo(src, dest);

        assertEquals(1, src.position());
        assertArrayEquals(new byte[] {6, 7}, dest.array());
    }

    @Test
    public void copyToDoesNothingWhenEitherBufferIsNull() {
        ByteBuffer dest = ByteBuffer.allocate(2);
        ByteBuffers.copyTo(null, dest);
        assertEquals(0, dest.position());

        ByteBuffers.copyTo(ByteBuffer.wrap(new byte[] {1}), null);
        assertTrue(true);
    }

    @Test
    public void copyWithPrefixAddsPrefixAndRemainingBytes() {
        ByteBuffer src = ByteBuffer.wrap(new byte[] {1, 2, 3});
        src.position(1);

        ByteBuffer result = ByteBuffers.copyWithPrefix((byte) 9, src);

        assertEquals(1, src.position());
        assertEquals(0, result.position());
        assertArrayEquals(new byte[] {9, 2, 3}, result.array());
    }

    @Test
    public void copyRemainingCopiesBytesAndPreservesSourcePosition() {
        ByteBuffer src = ByteBuffer.wrap(new byte[] {1, 2, 3, 4});
        src.position(2);

        ByteBuffer result = ByteBuffers.copyRemaining(src);

        assertEquals(2, src.position());
        assertEquals(0, result.position());
        assertArrayEquals(new byte[] {3, 4}, result.array());
    }

    @Test
    public void copyRemainingBytesReturnsRemainingBytes() {
        ByteBuffer src = ByteBuffer.wrap(new byte[] {8, 9, 10});
        src.position(1);

        assertArrayEquals(new byte[] {9, 10}, ByteBuffers.copyRemainingBytes(src));
        assertEquals(1, src.position());
    }

    @Test
    public void getStringReadsRemainingBytesWithoutChangingArrayBackedBufferPosition() {
        ByteBuffer buf = ByteBuffer.wrap(new byte[] {'a', 'b', 'c'});
        buf.position(1);

        assertEquals("bc", ByteBuffers.getString(buf));
        assertEquals(1, buf.position());
    }

    @Test
    public void getStringReadsRemainingBytesFromDirectBuffer() {
        ByteBuffer buf = ByteBuffer.allocateDirect(3);
        buf.put(new byte[] {'x', 'y', 'z'});
        buf.flip();
        buf.position(1);

        assertEquals("yz", ByteBuffers.getString(buf));
        assertEquals(3, buf.position());
    }

    @Test
    public void getStringReturnsNullForNullBuffer() {
        assertNull(ByteBuffers.getString(null));
    }
}
