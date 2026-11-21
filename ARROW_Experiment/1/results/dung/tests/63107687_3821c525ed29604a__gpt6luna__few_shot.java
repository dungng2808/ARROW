package io.virtdata.libbasics.shared.unary_string;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FieldExtractor_ArrowTest {

    @Test
    public void extractsRequestedFieldsInSpecifiedOrder() {
        FieldExtractor extractor = new FieldExtractor("|,3,1");
        assertEquals("third|first", extractor.apply("first|second|third"));
    }

    @Test
    public void extractsSingleField() {
        FieldExtractor extractor = new FieldExtractor(",", "2");
        assertEquals("second", extractor.apply("first,second,third"));
    }
}
