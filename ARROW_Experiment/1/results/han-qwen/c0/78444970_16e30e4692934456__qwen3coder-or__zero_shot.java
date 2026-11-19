
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.LinkedList;

public class LZ78_ArrowTest {

    @Test
    public void testEncodeEmptyInput() {
        char[] input = new char[0];
        List<LZ78Codeword> result = LZ78.encode(input);
        assertEquals(0, result.size());
    }

    @Test
    public void testEncodeSingleCharacter() {
        char[] input = {'a'};
        List<LZ78Codeword> result = LZ78.encode(input);
        assertEquals(1, result.size());
        LZ78Codeword codeword = result.get(0);
        assertEquals(0, codeword.getI());
        assertEquals('a', codeword.getX());
    }

    @Test
    public void testEncodeSimpleRepetition() {
        char[] input = {'a', 'a'};
        List<LZ78Codeword> result = LZ78.encode(input);
        assertEquals(2, result.size());
        
        LZ78Codeword first = result.get(0);
        assertEquals(0, first.getI());
        assertEquals('a', first.getX());
        
        LZ78Codeword second = result.get(1);
        assertEquals(1, second.getI());
        assertEquals('a', second.getX());
    }

    @Test
    public void testEncodeWithRepeat() {
        char[] input = {'a', 'b', 'a'};
        List<LZ78Codeword> result = LZ78.encode(input);
        assertEquals(3, result.size());
        
        LZ78Codeword first = result.get(0);
        assertEquals(0, first.getI());
        assertEquals('a', first.getX());
        
        LZ78Codeword second = result.get(1);
        assertEquals(0, second.getI());
        assertEquals('b', second.getX());
        
        LZ78Codeword third = result.get(2);
        assertEquals(1, third.getI());
        assertEquals('a', third.getX());
    }

    @Test
    public void testDecodeEmptyInput() {
        List<LZ78Codeword> input = new LinkedList<>();
        char[] result = LZ78.decode(input);
        assertEquals(0, result.length);
    }

    @Test
    public void testDecodeSingleCodeword() {
        List<LZ78Codeword> input = new LinkedList<>();
        input.add(new LZ78Codeword(0, 'a'));
        char[] result = LZ78.decode(input);
        assertEquals("a", new String(result));
    }

    @Test
    public void testDecodeSimpleRepetition() {
        List<LZ78Codeword> input = new LinkedList<>();
        input.add(new LZ78Codeword(0, 'a'));
        input.add(new LZ78Codeword(1, 'a'));
        char[] result = LZ78.decode(input);
        assertEquals("aa", new String(result));
    }

    @Test
    public void testEncodeDecodeRoundTrip() {
        char[] original = {'a', 'b', 'a', 'b', 'c'};
        List<LZ78Codeword> encoded = LZ78.encode(original);
        char[] decoded = LZ78.decode(encoded);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void testEncodeDecodeRoundTripSingleChar() {
        char[] original = {'x'};
        List<LZ78Codeword> encoded = LZ78.encode(original);
        char[] decoded = LZ78.decode(encoded);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void testEncodeDecodeRoundTripEmpty() {
        char[] original = new char[0];
        List<LZ78Codeword> encoded = LZ78.encode(original);
        char[] decoded = LZ78.decode(encoded);
        assertArrayEquals(original, decoded);
    }
}
