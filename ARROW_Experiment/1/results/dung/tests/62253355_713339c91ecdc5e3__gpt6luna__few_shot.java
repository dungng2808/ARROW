package com.mobi.rdf.orm.conversion.impl;

import com.mobi.rdf.api.Value;
import com.mobi.rdf.orm.Thing;
import com.mobi.rdf.orm.conversion.ValueConversionException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DoubleValueConverter_ArrowTest {

    @Test
    public void convertsValueStringToDouble() throws Exception {
        DoubleValueConverter converter = new DoubleValueConverter();
        Value value = new Value() {
            @Override
            public String stringValue() {
                return "12.5";
            }
        };

        Double result = converter.convertValue(value, null, Double.class);

        assertEquals(Double.valueOf(12.5), result);
    }

    @Test
    public void convertsNegativeValueStringToDouble() throws Exception {
        DoubleValueConverter converter = new DoubleValueConverter();
        Value value = new Value() {
            @Override
            public String stringValue() {
                return "-3.25";
            }
        };

        Double result = converter.convertValue(value, null, Double.class);

        assertEquals(Double.valueOf(-3.25), result);
    }

    @Test(expected = ValueConversionException.class)
    public void throwsConversionExceptionForInvalidDouble() throws Exception {
        DoubleValueConverter converter = new DoubleValueConverter();
        Value value = new Value() {
            @Override
            public String stringValue() {
                return "not-a-number";
            }
        };

        converter.convertValue(value, null, Double.class);
    }

    @Test
    public void convertsDoubleTypeToValue() throws Exception {
        DoubleValueConverter converter = new DoubleValueConverter();

        Value result = converter.convertType(4.5, null);

        assertNotNull(result);
        assertEquals("4.5", result.stringValue());
    }
}
