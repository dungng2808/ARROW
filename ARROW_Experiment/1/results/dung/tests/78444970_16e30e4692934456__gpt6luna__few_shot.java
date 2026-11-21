import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class LZ78_ArrowTest {

    @Test
    public void encodeEmptyInput() {
        List<LZ78Codeword> result = LZ78.encode(new char[0]);
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    public void encodeSingleCharacter() {
        List<LZ78Codeword> result = LZ78.encode(new char[] {'a'});
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getI());
        assertEquals('a', result.get(0).getX());
    }

    @Test
    public void encodeRepeatedCharacters() {
        List<LZ78Codeword> result = LZ78.encode(new char[] {'a', 'a', 'a'});
        assertEquals(2, result.size());
        assertEquals(0, result.get(0).getI());
        assertEquals('a', result.get(0).getX());
        assertEquals(1, result.get(1).getI());
        assertEquals('a', result.get(1).getX());
    }

    @Test
    public void decodeCodewords() {
        List<LZ78Codeword> input = Arrays.asList(
                new LZ78Codeword(0, 'a'),
                new LZ78Codeword(1, 'b'),
                new LZ78Codeword(0, 'c'));
        assertArrayEquals(new char[] {'a', 'a', 'b', 'c'}, LZ78.decode(input));
    }

    @Test
    public void decodeEmptyInput() {
        assertArrayEquals(new char[0], LZ78.decode(Collections.<LZ78Codeword>emptyList()));
    }
}
