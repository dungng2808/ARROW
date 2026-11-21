package com.mobi.rdf.orm.conversion.impl;

import com.mobi.rdf.api.Value;
import com.mobi.rdf.orm.Thing;
import com.mobi.rdf.orm.conversion.ValueConversionException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DoubleValueConverter_ArrowTest {

    @Test
    public void convertValueParsesDouble() throws Exception {
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
    public void convertValueParsesNegativeDouble() throws Exception {
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

    @Test
    public void convertValueWrapsInvalidNumber() throws Exception {
        DoubleValueConverter converter = new DoubleValueConverter();
        Value value = new Value() {
            @Override
            public String stringValue() {
                return "not-a-number";
            }
        };

        try {
            converter.convertValue(value, null, Double.class);
        } catch (ValueConversionException e) {
            assertEquals("Issue getting double value from statement", e.getMessage());
            assertEquals(NumberFormatException.class, e.getCause().getClass());
            return;
        }
        throw new AssertionError("Expected ValueConversionException");
    }

    @Test
    public void convertTypeWithNullThingThrows() throws Exception {
        DoubleValueConverter converter = new DoubleValueConverter();

        try {
            converter.convertType(Double.valueOf(4.5), null);
        } catch (Exception e) {
            assertNotNull(e);
            return;
        }
        throw new AssertionError("Expected an exception");
    }
}
