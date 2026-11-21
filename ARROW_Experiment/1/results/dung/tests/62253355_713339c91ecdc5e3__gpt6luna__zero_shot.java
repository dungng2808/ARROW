package com.mobi.rdf.orm.conversion.impl;

import com.mobi.rdf.api.Value;
import com.mobi.rdf.orm.Thing;
import com.mobi.rdf.orm.conversion.ValueConversionException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DoubleValueConverter_ArrowTest {

    @Test
    public void convertValueParsesDecimal() throws Exception {
        DoubleValueConverter converter = new DoubleValueConverter();
        Value value = new Value() {
            @Override
            public String stringValue() {
                return "12.5";
            }
        };

        assertEquals(Double.valueOf(12.5), converter.convertValue(value, null, Double.class));
    }

    @Test(expected = ValueConversionException.class)
    public void convertValueRejectsInvalidNumber() throws Exception {
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
    public void convertTypeReturnsValue() throws Exception {
        DoubleValueConverter converter = new DoubleValueConverter();
        Value value = converter.convertType(3.25, null);

        assertNotNull(value);
    }
}
