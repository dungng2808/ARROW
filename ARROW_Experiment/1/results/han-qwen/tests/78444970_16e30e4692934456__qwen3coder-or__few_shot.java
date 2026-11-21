
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.LinkedList;

public class LZ78_ArrowTest {

    @Test
    public void testEncodeEmptyInput() {
        char[] input = new char[0];
        List<LZ78Codeword> result = LZ78.encode(input);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testEncodeSingleCharacter() {
        char[] input = {'a'};
        List<LZ78Codeword> result = LZ78.encode(input);
        assertNotNull(result);
        assertEquals(1, result.size());
        LZ78Codeword codeword = result.get(0);
        assertEquals(0, codeword.getI());
        assertEquals('a', codeword.getX());
    }

    @Test
    public void testEncodeSimpleRepetition() {
        char[] input = {'a', 'a'};
        List<LZ78Codeword> result = LZ78.encode(input);
        assertNotNull(result);
        assertEquals(2, result.size());
        
        LZ78Codeword first = result.get(0);
        assertEquals(0, first.getI());
        assertEquals('a', first.getX());
        
        LZ78Codeword second = result.get(1);
        assertEquals(1, second.getI());
        assertEquals(0, second.getX());
    }

    @Test
    public void testDecodeEmptyInput() {
        List<LZ78Codeword> input = new LinkedList<>();
        char[] result = LZ78.decode(input);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testDecodeSingleCodeword() {
        List<LZ78Codeword> input = new LinkedList<>();
        input.add(new LZ78Codeword(0, 'a'));
        char[] result = LZ78.decode(input);
        assertNotNull(result);
        assertEquals("a", new String(result));
    }

    @Test
    public void testEncodeAndDecodeSimpleCase() {
        char[] original = {'a', 'b', 'a', 'b'};
        List<LZ78Codeword> encoded = LZ78.encode(original);
        char[] decoded = LZ78.decode(encoded);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void testEncodeAndDecodeComplexCase() {
        char[] original = {'a', 'b', 'c', 'a', 'b', 'c'};
        List<LZ78Codeword> encoded = LZ78.encode(original);
        char[] decoded = LZ78.decode(encoded);
        assertArrayEquals(original, decoded);
    }
}
