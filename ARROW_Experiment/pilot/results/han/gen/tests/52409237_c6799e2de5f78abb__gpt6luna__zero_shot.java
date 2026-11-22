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
    public void convertLeavesCharactersOutsideCjkRangeUnchanged() {
        TraditionToSimple converter = TraditionToSimple.instance();

        assertEquals('A', converter.convert('A'));
        assertEquals('\u0000', converter.convert('\u0000'));
        assertEquals('\uFFFF', converter.convert('\uFFFF'));
    }

    @Test
    public void convertStringReturnsSameStringWhenNoConversionIsNeeded() {
        TraditionToSimple converter = TraditionToSimple.instance();
        String text = "abc";

        assertSame(text, converter.convert(text));
    }

    @Test
    public void convertStringReturnsNullForNullInput() {
        TraditionToSimple converter = TraditionToSimple.instance();

        assertEquals(null, converter.convert((String) null));
    }

    @Test
    public void convertStringReturnsEmptyInputUnchanged() {
        TraditionToSimple converter = TraditionToSimple.instance();
        String text = "";

        assertSame(text, converter.convert(text));
    }

    @Test
    public void convertStringConvertsTraditionalCharacter() {
        TraditionToSimple converter = TraditionToSimple.instance();
        String traditional = "\u9AD4";
        String converted = converter.convert(traditional);

        assertEquals(1, converted.length());
        assertEquals(converter.convert('\u9AD4'), converted.charAt(0));
        assertNotSame(traditional, converted);
    }

    @Test
    public void isTraditionalMatchesWhetherCharacterChanges() {
        TraditionToSimple converter = TraditionToSimple.instance();

        assertFalse(converter.isTraditional('A'));
        assertEquals(converter.convert('\u9AD4') != '\u9AD4',
                converter.isTraditional('\u9AD4'));
    }

    @Test
    public void convertCharArrayOnlyChangesRequestedRange() {
        TraditionToSimple converter = TraditionToSimple.instance();
        char[] text = new char[] {'A', '\u9AD4', 'B'};

        converter.convert(text, 1, 1);

        assertEquals('A', text[0]);
        assertEquals(converter.convert('\u9AD4'), text[1]);
        assertEquals('B', text[2]);
    }

    @Test
    public void convertCharArrayWithZeroLengthLeavesArrayUnchanged() {
        TraditionToSimple converter = TraditionToSimple.instance();
        char[] text = new char[] {'A', '\u9AD4'};

        converter.convert(text, 1, 0);

        assertEquals('A', text[0]);
        assertEquals('\u9AD4', text[1]);
    }
}
