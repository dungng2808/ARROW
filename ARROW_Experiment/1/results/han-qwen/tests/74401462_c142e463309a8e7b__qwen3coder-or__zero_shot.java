package brown.tracingplane.atomlayer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

public class ByteBuffers_ArrowTest {

    @Test
    public void testCopyToWithArray() {
        byte[] srcArray = {1, 2, 3, 4, 5};
        ByteBuffer src = ByteBuffer.wrap(srcArray);
        ByteBuffer dest = ByteBuffer.allocate(10);
        
        ByteBuffers.copyTo(src, dest);
        
        assertEquals(5, dest.position());
        dest.position(0);
        byte[] result = new byte[5];
        dest.get(result);
        assertArrayEquals(srcArray, result);
    }

    @Test
    public void testCopyToWithoutArray() {
        ByteBuffer src = ByteBuffer.allocateDirect(5);
        src.put(new byte[]{1, 2, 3, 4, 5});
        src.flip();
        ByteBuffer dest = ByteBuffer.allocate(10);
        
        ByteBuffers.copyTo(src, dest);
        
        assertEquals(5, dest.position());
        dest.position(0);
        byte[] result = new byte[5];
        dest.get(result);
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, result);
    }

    @Test
    public void testCopyToNulls() {
        ByteBuffers.copyTo(null, null);
        ByteBuffers.copyTo(ByteBuffer.allocate(5), null);
        ByteBuffers.copyTo(null, ByteBuffer.allocate(5));
    }

    @Test
    public void testCopyWithPrefix() {
        ByteBuffer src = ByteBuffer.wrap(new byte[]{1, 2, 3});
        ByteBuffer result = ByteBuffers.copyWithPrefix((byte) 0xFF, src);
        
        assertEquals(4, result.remaining());
        assertEquals((byte) 0xFF, result.get());
        byte[] expected = {1, 2, 3};
        byte[] actual = new byte[3];
        result.get(actual);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testCopyRemaining() {
        ByteBuffer src = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        src.position(2);
        ByteBuffer result = ByteBuffers.copyRemaining(src);
        
        assertEquals(3, result.remaining());
        result.position(0);
        byte[] expected = {3, 4, 5};
        byte[] actual = new byte[3];
        result.get(actual);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testCopyRemainingBytes() {
        ByteBuffer src = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        src.position(2);
        byte[] result = ByteBuffers.copyRemainingBytes(src);
        
        assertArrayEquals(new byte[]{3, 4, 5}, result);
    }

    @Test
    public void testGetString() {
        ByteBuffer buf = ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8));
        String result = ByteBuffers.getString(buf);
        assertEquals("Hello", result);
    }

    @Test
    public void testGetStringNull() {
        assertNull(ByteBuffers.getString(null));
    }

    @Test
    public void testGetStringWithoutArray() {
        ByteBuffer buf = ByteBuffer.allocateDirect(5);
        buf.put("World".getBytes(StandardCharsets.UTF_8));
        buf.flip();
        String result = ByteBuffers.getString(buf);
        assertEquals("World", result);
    }

    @Test
    public void testLexicographicComparator() {
        ByteBuffer buf1 = ByteBuffer.wrap(new byte[]{1, 2, 3});
        ByteBuffer buf2 = ByteBuffer.wrap(new byte[]{1, 2, 4});
        int result = ByteBuffers.LEXICOGRAPHIC_COMPARATOR.compare(buf1, buf2);
        assertTrue(result < 0);
    }
}
