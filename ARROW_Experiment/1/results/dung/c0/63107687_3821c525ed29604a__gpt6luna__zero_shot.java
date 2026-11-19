package io.virtdata.libbasics.shared.unary_string;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FieldExtractor_ArrowTest {

    @Test
    public void extractsRequestedFieldsInSpecifiedOrder() {
        FieldExtractor extractor = new FieldExtractor("|,3,1");
        assertEquals("gamma|alpha", extractor.apply("alpha|beta|gamma|delta"));
    }

    @Test
    public void extractsSingleField() {
        FieldExtractor extractor = new FieldExtractor(",", "2");
        assertEquals("beta", extractor.apply("alpha,beta,gamma"));
    }
}
