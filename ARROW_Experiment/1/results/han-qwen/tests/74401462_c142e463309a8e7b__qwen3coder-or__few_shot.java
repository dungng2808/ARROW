package brown.tracingplane.atomlayer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import org.junit.Test;
import static org.junit.Assert.*;

public class ByteBuffers_ArrowTest {

    @Test
    public void testLexicographicComparator() {
        assertNotNull(ByteBuffers.LEXICOGRAPHIC_COMPARATOR);
        assertTrue(ByteBuffers.LEXICOGRAPHIC_COMPARATOR instanceof Comparator);
    }

    @Test
    public void testCopyToWithNullBuffers() {
        ByteBuffer src = null;
        ByteBuffer dest = null;
        ByteBuffers.copyTo(src, dest);
        // Should not throw any exception
    }

    @Test
    public void testCopyToWithNullSrc() {
        ByteBuffer src = null;
        ByteBuffer dest = ByteBuffer.allocate(10);
        ByteBuffers.copyTo(src, dest);
        // Should not throw any exception
    }

    @Test
    public void testCopyToWithNullDest() {
        ByteBuffer src = ByteBuffer.allocate(10);
        ByteBuffer dest = null;
        ByteBuffers.copyTo(src, dest);
        // Should not throw any exception
    }

    @Test
    public void testCopyToWithArrayBasedBuffer() {
        byte[] data = {1, 2, 3, 4, 5};
        ByteBuffer src = ByteBuffer.wrap(data);
        ByteBuffer dest = ByteBuffer.allocate(10);
        ByteBuffers.copyTo(src, dest);
        assertEquals(5, dest.position());
        dest.position(0);
        byte[] result = new byte[5];
        dest.get(result);
        assertArrayEquals(data, result);
    }

    @Test
    public void testCopyToWithDirectBuffer() {
        ByteBuffer src = ByteBuffer.allocateDirect(5);
        src.put(new byte[]{1, 2, 3, 4, 5});
        src.position(0);
        ByteBuffer dest = ByteBuffer.allocate(10);
        ByteBuffers.copyTo(src, dest);
        assertEquals(5, dest.position());
        dest.position(0);
        byte[] result = new byte[5];
        dest.get(result);
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, result);
    }

    @Test
    public void testCopyWithPrefix() {
        ByteBuffer src = ByteBuffer.wrap(new byte[]{1, 2, 3});
        ByteBuffer result = ByteBuffers.copyWithPrefix((byte) 0, src);
        assertEquals(4, result.remaining());
        assertEquals(0, result.get());
        assertEquals(1, result.get());
        assertEquals(2, result.get());
        assertEquals(3, result.get());
    }

    @Test
    public void testCopyRemaining() {
        ByteBuffer src = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        src.position(2);
        ByteBuffer result = ByteBuffers.copyRemaining(src);
        assertEquals(3, result.remaining());
        assertEquals(3, result.get());
        assertEquals(4, result.get());
        assertEquals(5, result.get());
    }

    @Test
    public void testCopyRemainingBytes() {
        ByteBuffer src = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        src.position(2);
        byte[] result = ByteBuffers.copyRemainingBytes(src);
        assertArrayEquals(new byte[]{3, 4, 5}, result);
    }

    @Test
    public void testGetStringWithNullBuffer() {
        assertNull(ByteBuffers.getString(null));
    }

    @Test
    public void testGetStringWithArrayBasedBuffer() {
        ByteBuffer buf = ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8));
        String result = ByteBuffers.getString(buf);
        assertEquals("Hello", result);
    }

    @Test
    public void testGetStringWithDirectBuffer() {
        ByteBuffer buf = ByteBuffer.allocateDirect(5);
        buf.put("Hello".getBytes(StandardCharsets.UTF_8));
        buf.position(0);
        String result = ByteBuffers.getString(buf);
        assertEquals("Hello", result);
    }
}
