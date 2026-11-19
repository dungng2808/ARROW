package io.virtdata.libbasics.shared.unary_string;

import org.junit.Test;
import static org.junit.Assert.*;

public class FieldExtractor_ArrowTest {

    @Test
    public void testSingleFieldExtraction() {
        FieldExtractor extractor = new FieldExtractor("|,2");
        String input = "a|b|c|d|e";
        String result = extractor.apply(input);
        assertEquals("b", result);
    }

    @Test
    public void testMultipleFieldsExtraction() {
        FieldExtractor extractor = new FieldExtractor("|,2,4");
        String input = "a|b|c|d|e";
        String result = extractor.apply(input);
        assertEquals("b|d", result);
    }

    @Test
    public void testThreeFieldsExtraction() {
        FieldExtractor extractor = new FieldExtractor("|,1,3,5");
        String input = "a|b|c|d|e";
        String result = extractor.apply(input);
        assertEquals("a|c|e", result);
    }

    @Test
    public void testFieldsWithSpaces() {
        FieldExtractor extractor = new FieldExtractor("|, 1 , 3 ");
        String input = "a|b|c|d|e";
        String result = extractor.apply(input);
        assertEquals("a|c", result);
    }

    @Test
    public void testUnderrunReturnsError() {
        FieldExtractor extractor = new FieldExtractor("|,2,4");
        String input = "a|b|c";
        String result = extractor.apply(input);
        assertEquals("ERROR-UNDERRUN in FieldExtractor", result);
    }

    @Test
    public void testEmptyInput() {
        FieldExtractor extractor = new FieldExtractor("|,1");
        String input = "";
        String result = extractor.apply(input);
        assertEquals("ERROR-UNDERRUN in FieldExtractor", result);
    }

    @Test
    public void testDelimiterInData() {
        FieldExtractor extractor = new FieldExtractor("|,2");
        String input = "a|b|c|d|e";
        String result = extractor.apply(input);
        assertEquals("b", result);
    }

    @Test
    public void testDifferentDelimiter() {
        FieldExtractor extractor = new FieldExtractor(",",2,4);
        String input = "a,b,c,d,e";
        String result = extractor.apply(input);
        assertEquals("b,d", result);
    }

    @Test
    public void testFirstField() {
        FieldExtractor extractor = new FieldExtractor("|,1");
        String input = "a|b|c|d|e";
        String result = extractor.apply(input);
        assertEquals("a", result);
    }

    @Test
    public void testLastField() {
        FieldExtractor extractor = new FieldExtractor("|,5");
        String input = "a|b|c|d|e";
        String result = extractor.apply(input);
        assertEquals("e", result);
    }
}
