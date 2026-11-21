package brown.tracingplane.atomlayer;

import java.nio.ByteBuffer;
import java.util.Comparator;

import org.junit.Test;
import static org.junit.Assert.*;

public class ByteBuffers_ArrowTest {

    @Test
    public void testLexicographicComparator() {
        Comparator<ByteBuffer> comparator = ByteBuffers.LEXICOGRAPHIC_COMPARATOR;
        assertNotNull(comparator);
    }

    @Test
    public void testCopyToNullSrc() {
        ByteBuffer dest = ByteBuffer.allocate(10);
        ByteBuffers.copyTo(null, dest);
        assertEquals(0, dest.position());
    }

    @Test
    public void testCopyToNullDest() {
        ByteBuffer src = ByteBuffer.allocate(10);
        src.put(new byte[]{1, 2, 3});
        src.position(0);
        ByteBuffers.copyTo(src, null);
        assertEquals(0, src.position());
    }

    @Test
    public void testCopyToArrayBasedBuffer() {
        ByteBuffer src = ByteBuffer.allocate(5);
        src.put(new byte[]{1, 2, 3, 4, 5});
        src.position(0);
        
        ByteBuffer dest = ByteBuffer.allocate(10);
        ByteBuffers.copyTo(src, dest);
        
        assertEquals(5, dest.position());
        dest.position(0);
        assertEquals(1, dest.get());
        assertEquals(2, dest.get());
        assertEquals(3, dest.get());
        assertEquals(4, dest.get());
        assertEquals(5, dest.get());
    }

    @Test
    public void testCopyToNonArrayBasedBuffer() {
        ByteBuffer src = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        src.position(0);
        
        ByteBuffer dest = ByteBuffer.allocate(10);
        ByteBuffers.copyTo(src, dest);
        
        assertEquals(5, dest.position());
        dest.position(0);
        assertEquals(1, dest.get());
        assertEquals(2, dest.get());
        assertEquals(3, dest.get());
        assertEquals(4, dest.get());
        assertEquals(5, dest.get());
    }

    @Test
    public void testCopyWithPrefix() {
        ByteBuffer src = ByteBuffer.allocate(3);
        src.put(new byte[]{1, 2, 3});
        src.position(0);
        
        ByteBuffer result = ByteBuffers.copyWithPrefix((byte) 0xFF, src);
        
        assertEquals(4, result.remaining());
        assertEquals((byte) 0xFF, result.get());
        assertEquals((byte) 1, result.get());
        assertEquals((byte) 2, result.get());
        assertEquals((byte) 3, result.get());
    }

    @Test
    public void testCopyRemaining() {
        ByteBuffer src = ByteBuffer.allocate(3);
        src.put(new byte[]{1, 2, 3});
        src.position(0);
        
        ByteBuffer result = ByteBuffers.copyRemaining(src);
        
        assertEquals(3, result.remaining());
        result.position(0);
        assertEquals((byte) 1, result.get());
        assertEquals((byte) 2, result.get());
        assertEquals((byte) 3, result.get());
    }

    @Test
    public void testCopyRemainingBytes() {
        ByteBuffer src = ByteBuffer.allocate(3);
        src.put(new byte[]{1, 2, 3});
        src.position(0);
        
        byte[] result = ByteBuffers.copyRemainingBytes(src);
        
        assertEquals(3, result.length);
        assertEquals((byte) 1, result[0]);
        assertEquals((byte) 2, result[1]);
        assertEquals((byte) 3, result[2]);
    }

    @Test
    public void testGetStringNull() {
        String result = ByteBuffers.getString(null);
        assertNull(result);
    }

    @Test
    public void testGetStringFromArrayBasedBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(3);
        buf.put(new byte[]{65, 66, 67});
        buf.position(0);
        
        String result = ByteBuffers.getString(buf);
        assertEquals("ABC", result);
    }

    @Test
    public void testGetStringFromNonArrayBasedBuffer() {
        ByteBuffer buf = ByteBuffer.wrap(new byte[]{65, 66, 67});
        buf.position(0);
        
        String result = ByteBuffers.getString(buf);
        assertEquals("ABC", result);
    }
}
