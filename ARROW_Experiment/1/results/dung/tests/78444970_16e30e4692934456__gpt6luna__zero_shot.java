import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class LZ78_ArrowTest {

    @Test
    public void encodeEmptyInputReturnsNoCodewords() {
        assertEquals(Collections.emptyList(), LZ78.encode(new char[0]));
    }

    @Test
    public void encodeSingleCharacter() {
        List<LZ78Codeword> result = LZ78.encode(new char[]{'a'});

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getI());
        assertEquals('a', result.get(0).getX());
    }

    @Test
    public void encodeRepeatedCharacters() {
        List<LZ78Codeword> result = LZ78.encode(new char[]{'a', 'a', 'a'});

        assertEquals(2, result.size());
        assertEquals(0, result.get(0).getI());
        assertEquals('a', result.get(0).getX());
        assertEquals(1, result.get(1).getI());
        assertEquals('a', result.get(1).getX());
    }

    @Test
    public void decodeEmptyInputReturnsEmptyArray() {
        assertArrayEquals(new char[0], LZ78.decode(Collections.<LZ78Codeword>emptyList()));
    }

    @Test
    public void decodeCodewordsWithNewSymbols() {
        List<LZ78Codeword> input = Arrays.asList(
                new LZ78Codeword(0, 'a'),
                new LZ78Codeword(0, 'b'));

        assertArrayEquals(new char[]{'a', 'b'}, LZ78.decode(input));
    }

    @Test
    public void decodeCodewordUsingExistingWordAndTerminal() {
        List<LZ78Codeword> input = Arrays.asList(
                new LZ78Codeword(0, 'a'),
                new LZ78Codeword(1, 'b'));

        assertArrayEquals(new char[]{'a', 'a', 'b'}, LZ78.decode(input));
    }

    @Test
    public void decodeCodewordUsingExistingWordOnly() {
        List<LZ78Codeword> input = Arrays.asList(
                new LZ78Codeword(0, 'a'),
                new LZ78Codeword(1, (char) 0));

        assertArrayEquals(new char[]{'a', 'a'}, LZ78.decode(input));
    }

    @Test
    public void encodeAndDecodeRoundTrip() {
        char[] input = new char[]{'a', 'b', 'a', 'b', 'a', 'c'};

        assertArrayEquals(input, LZ78.decode(LZ78.encode(input)));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void decodeRejectsReferenceToMissingTableEntry() {
        LZ78.decode(Collections.singletonList(new LZ78Codeword(1, 'a')));
    }
}
