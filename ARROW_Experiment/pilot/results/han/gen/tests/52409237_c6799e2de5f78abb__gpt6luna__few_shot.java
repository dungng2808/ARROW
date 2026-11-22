package com.tqmall.search.commons.nlp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TraditionToSimple_ArrowTest {

    @Test
    public void instanceReturnsSingleton() {
        TraditionToSimple first = TraditionToSimple.instance();
        TraditionToSimple second = TraditionToSimple.instance();

        assertSame(first, second);
    }

    @Test
    public void convertsTraditionalCharacter() {
        TraditionToSimple converter = TraditionToSimple.instance();

        assertEquals('汉', converter.convert('漢'));
        assertTrue(converter.isTraditional('漢'));
    }

    @Test
    public void leavesUnmappedCjkCharacterUnchanged() {
        TraditionToSimple converter = TraditionToSimple.instance();

        assertEquals('龘', converter.convert('龘'));
        assertFalse(converter.isTraditional('龘'));
    }

    @Test
    public void leavesCharactersOutsideCjkRangeUnchanged() {
        TraditionToSimple converter = TraditionToSimple.instance();

        assertEquals('A', converter.convert('A'));
        assertFalse(converter.isTraditional('A'));
    }

    @Test
    public void convertsStringContainingTraditionalCharacters() {
        TraditionToSimple converter = TraditionToSimple.instance();
        String input = "繁體中文";

        String result = converter.convert(input);

        assertEquals("繁体中文", result);
        assertNotSame(input, result);
    }

    @Test
    public void returnsOriginalStringWhenNoConversionIsNeeded() {
        TraditionToSimple converter = TraditionToSimple.instance();
        String input = "简体中文";

        String result = converter.convert(input);

        assertEquals(input, result);
        assertSame(input, result);
    }

    @Test
    public void returnsNullForNullString() {
        TraditionToSimple converter = TraditionToSimple.instance();

        assertEquals(null, converter.convert((String) null));
    }

    @Test
    public void returnsEmptyStringUnchanged() {
        TraditionToSimple converter = TraditionToSimple.instance();
        String input = "";

        assertSame(input, converter.convert(input));
    }

    @Test
    public void convertsOnlyRequestedArrayRange() {
        TraditionToSimple converter = TraditionToSimple.instance();
        char[] text = {'A', '漢', '體', 'B'};

        converter.convert(text, 1, 2);

        assertEquals(new char[]{'A', '汉', '体', 'B'}, text);
    }

    @Test
    public void convertsEmptyArrayRangeWithoutChangingArray() {
        TraditionToSimple converter = TraditionToSimple.instance();
        char[] text = {'漢'};

        converter.convert(text, 0, 0);

        assertEquals(new char[]{'漢'}, text);
    }
}
