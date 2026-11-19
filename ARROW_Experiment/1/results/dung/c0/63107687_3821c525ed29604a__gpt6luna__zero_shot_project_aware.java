package io.virtdata.libbasics.shared.unary_string;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FieldExtractor_ArrowTest {

    @Test
    public void extractsSelectedFieldsInRequestedOrder() {
        FieldExtractor extractor = new FieldExtractor("|,3,1");
        assertEquals("third|first", extractor.apply("first|second|third"));
    }

    @Test
    public void extractsSingleField() {
        FieldExtractor extractor = new FieldExtractor(",", "2");
        assertEquals("beta", extractor.apply("alpha,beta,gamma"));
    }

    @Test
    public void returnsUnderrunWhenRequestedIndexExceedsAvailableFields() {
        FieldExtractor extractor = new FieldExtractor("|,4");
        assertEquals("ERROR-UNDERRUN in FieldExtractor", extractor.apply("one|two|three"));
    }

    @Test
    public void extractsEmptyField() {
        FieldExtractor extractor = new FieldExtractor("|,2");
        assertEquals("", extractor.apply("one|"));
    }
}
