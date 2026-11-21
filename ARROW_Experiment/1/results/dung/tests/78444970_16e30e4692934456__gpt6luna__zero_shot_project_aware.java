import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class LZ78_ArrowTest {

    @Test
    public void encodeEmptyInputReturnsNoCodewords() {
        assertEquals(0, LZ78.encode(new char[0]).size());
    }

    @Test
    public void encodeSingleCharacter() {
        List<LZ78Codeword> result = LZ78.encode(new char[]{'A'});

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getI());
        assertEquals('A', result.get(0).getX());
    }

    @Test
    public void encodeRepeatedCharacters() {
        List<LZ78Codeword> result = LZ78.encode(new char[]{'A', 'A', 'A'});

        assertEquals(2, result.size());
        assertEquals(0, result.get(0).getI());
        assertEquals('A', result.get(0).getX());
        assertEquals(1, result.get(1).getI());
        assertEquals('A', result.get(1).getX());
    }

    @Test
    public void encodePrefixAtEndUsesZeroTerminal() {
        List<LZ78Codeword> result = LZ78.encode(new char[]{'A', 'B', 'A'});

        assertEquals(2, result.size());
        assertEquals(0, result.get(0).getI());
        assertEquals('A', result.get(0).getX());
        assertEquals(0, result.get(1).getI());
        assertEquals('B', result.get(1).getX());
    }

    @Test
    public void decodeCodewords() {
        List<LZ78Codeword> input = Arrays.asList(
                new LZ78Codeword(0, 'A'),
                new LZ78Codeword(0, 'B'),
                new LZ78Codeword(1, 'A')
        );

        assertArrayEquals(new char[]{'A', 'B', 'A', 'A'}, LZ78.decode(input));
    }

    @Test
    public void decodeEmptyInputReturnsEmptyArray() {
        assertArrayEquals(new char[0], LZ78.decode(Arrays.<LZ78Codeword>asList()));
    }
}
