package brown.tracingplane.atomlayer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.ByteBuffer;
import org.junit.Test;

public class ByteBuffers_ArrowTest {

    @Test
    public void copyToCopiesRemainingArrayBytesWithoutChangingSourcePosition() {
        ByteBuffer src = ByteBuffer.wrap(new byte[] { 1, 2, 3, 4 });
        src.position(1);
        src.limit(3);
        ByteBuffer dest = ByteBuffer.allocate(4);

        ByteBuffers.copyTo(src, dest);

        assertEquals(1, src.position());
        assertEquals(2, dest.position());
        assertArrayEquals(new byte[] { 2, 3 }, new byte[] { dest.get(0), dest.get(1) });
    }

    @Test
    public void copyToCopiesRemainingBytesFromDirectBuffer() {
        ByteBuffer src = ByteBuffer.allocateDirect(3);
        src.put(new byte[] { 5, 6, 7 });
        src.flip();
        src.position(1);
        ByteBuffer dest = ByteBuffer.allocate(2);

        ByteBuffers.copyTo(src, dest);

        assertEquals(1, src.position());
        assertEquals(2, dest.position());
        assertArrayEquals(new byte[] { 6, 7 }, new byte[] { dest.get(0), dest.get(1) });
    }

    @Test
    public void copyToDoesNothingWhenEitherBufferIsNull() {
        ByteBuffer dest = ByteBuffer.allocate(2);
        ByteBuffers.copyTo(null, dest);
        assertEquals(0, dest.position());

        ByteBuffer src = ByteBuffer.wrap(new byte[] { 1 });
        ByteBuffers.copyTo(src, null);
        assertEquals(0, src.position());
    }

    @Test
    public void copyWithPrefixAddsPrefixAndRemainingBytes() {
        ByteBuffer src = ByteBuffer.wrap(new byte[] { 1, 2, 3 });
        src.position(1);

        ByteBuffer result = ByteBuffers.copyWithPrefix((byte) 9, src);

        assertEquals(1, src.position());
        assertEquals(0, result.position());
        assertArrayEquals(new byte[] { 9, 2, 3 }, result.array());
    }

    @Test
    public void copyRemainingCopiesRemainingBytesAndPreservesSourcePosition() {
        ByteBuffer src = ByteBuffer.wrap(new byte[] { 1, 2, 3 });
        src.position(1);

        ByteBuffer result = ByteBuffers.copyRemaining(src);

        assertEquals(1, src.position());
        assertEquals(0, result.position());
        assertArrayEquals(new byte[] { 2, 3 }, result.array());
    }

    @Test
    public void copyRemainingHandlesEmptyBuffer() {
        ByteBuffer result = ByteBuffers.copyRemaining(ByteBuffer.allocate(0));

        assertEquals(0, result.position());
        assertArrayEquals(new byte[0], result.array());
    }

    @Test
    public void copyRemainingBytesReturnsRemainingBytes() {
        ByteBuffer src = ByteBuffer.wrap(new byte[] { 4, 5, 6 });
        src.position(1);

        assertArrayEquals(new byte[] { 5, 6 }, ByteBuffers.copyRemainingBytes(src));
        assertEquals(1, src.position());
    }

    @Test
    public void getStringReadsRemainingArrayBytesWithoutChangingPosition() {
        ByteBuffer buf = ByteBuffer.wrap(new byte[] { 'a', 'b', 'c' });
        buf.position(1);

        assertEquals("bc", ByteBuffers.getString(buf));
        assertEquals(1, buf.position());
    }

    @Test
    public void getStringReadsRemainingDirectBufferBytes() {
        ByteBuffer buf = ByteBuffer.allocateDirect(3);
        buf.put(new byte[] { 'x', 'y', 'z' });
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
