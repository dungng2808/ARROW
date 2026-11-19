
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
    public void testEncodeTwoDifferentCharacters() {
        char[] input = {'a', 'b'};
        List<LZ78Codeword> result = LZ78.encode(input);
        assertEquals(2, result.size());
        assertEquals(0, result.get(0).getI());
        assertEquals('a', result.get(0).getX());
        assertEquals(0, result.get(1).getI());
        assertEquals('b', result.get(1).getX());
    }

    @Test
    public void testEncodeRepeatedCharacter() {
        char[] input = {'a', 'a'};
        List<LZ78Codeword> result = LZ78.encode(input);
        assertEquals(2, result.size());
        assertEquals(0, result.get(0).getI());
        assertEquals('a', result.get(0).getX());
        assertEquals(1, result.get(1).getI());
        assertEquals('a', result.get(1).getX());
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
        assertArrayEquals(new char[]{'a'}, result);
    }

    @Test
    public void testDecodeTwoCodewords() {
        List<LZ78Codeword> input = new LinkedList<>();
        input.add(new LZ78Codeword(0, 'a'));
        input.add(new LZ78Codeword(0, 'b'));
        char[] result = LZ78.decode(input);
        assertArrayEquals(new char[]{'a', 'b'}, result);
    }

    @Test
    public void testEncodeDecodeRoundTrip() {
        char[] original = {'a', 'b', 'a', 'b', 'c'};
        List<LZ78Codeword> encoded = LZ78.encode(original);
        char[] decoded = LZ78.decode(encoded);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void testSafeSubString() {
        char[] input = {'a', 'b', 'c'};
        String result = LZ78.safeSubString(input, 0, 2);
        assertEquals("ab", result);
    }

    @Test
    public void testSafeSubChar() {
        char[] input = {'a', 'b', 'c'};
        char result = LZ78.safeSubChar(input, 1);
        assertEquals('b', result);
    }

    @Test
    public void testSafeSubCharOutOfBounds() {
        char[] input = {'a', 'b', 'c'};
        char result = LZ78.safeSubChar(input, 5);
        assertEquals(0, result);
    }
}
