package brown.tracingplane.atomlayer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.ByteBuffer;
import org.junit.Test;

public class ByteBuffers_ArrowTest {

    @Test
    public void copyToCopiesRemainingBytesWithoutChangingSourcePosition() {
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
    public void copyToCopiesFromBufferWithoutArray() {
        ByteBuffer src = ByteBuffer.allocateDirect(4);
        src.put(new byte[] { 5, 6, 7, 8 });
        src.position(1);
        src.limit(3);
        ByteBuffer dest = ByteBuffer.allocate(2);

        ByteBuffers.copyTo(src, dest);

        assertEquals(1, src.position());
        assertEquals(2, dest.position());
        assertArrayEquals(new byte[] { 6, 7 }, dest.array());
    }

    @Test
    public void copyToDoesNothingWhenSourceIsNull() {
        ByteBuffer dest = ByteBuffer.allocate(2);

        ByteBuffers.copyTo(null, dest);

        assertEquals(0, dest.position());
    }

    @Test
    public void copyToDoesNothingWhenDestinationIsNull() {
        ByteBuffer src = ByteBuffer.wrap(new byte[] { 1, 2 });
        ByteBuffers.copyTo(src, null);

        assertEquals(0, src.position());
    }

    @Test
    public void copyWithPrefixReturnsPrefixAndRemainingBytes() {
        ByteBuffer src = ByteBuffer.wrap(new byte[] { 10, 20, 30 });
        src.position(1);

        ByteBuffer result = ByteBuffers.copyWithPrefix((byte) 9, src);

        assertEquals(1, src.position());
        assertEquals(0, result.position());
        assertArrayEquals(new byte[] { 9, 20, 30 }, result.array());
    }

    @Test
    public void copyRemainingReturnsRemainingBytesAtPositionZero() {
        ByteBuffer src = ByteBuffer.wrap(new byte[] { 1, 2, 3, 4 });
        src.position(2);

        ByteBuffer result = ByteBuffers.copyRemaining(src);

        assertEquals(2, src.position());
        assertEquals(0, result.position());
        assertArrayEquals(new byte[] { 3, 4 }, result.array());
    }

    @Test
    public void copyRemainingBytesReturnsRemainingBytes() {
        ByteBuffer src = ByteBuffer.wrap(new byte[] { 4, 5, 6 });
        src.position(1);

        byte[] result = ByteBuffers.copyRemainingBytes(src);

        assertEquals(1, src.position());
        assertArrayEquals(new byte[] { 5, 6 }, result);
    }

    @Test
    public void getStringReturnsRemainingBytesAsString() {
        ByteBuffer buf = ByteBuffer.wrap("hello".getBytes());
        buf.position(1);

        String result = ByteBuffers.getString(buf);

        assertEquals("ello", result);
        assertEquals(1, buf.position());
    }

    @Test
    public void getStringReadsRemainingBytesFromBufferWithoutArray() {
        ByteBuffer buf = ByteBuffer.allocateDirect(5);
        buf.put("hello".getBytes());
        buf.position(1);

        String result = ByteBuffers.getString(buf);

        assertEquals("ello", result);
        assertEquals(5, buf.position());
    }

    @Test
    public void getStringReturnsNullForNullBuffer() {
        assertNull(ByteBuffers.getString(null));
    }
}
