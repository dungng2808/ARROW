package com.mobi.rdf.orm.conversion.impl;

import com.mobi.rdf.api.Literal;
import com.mobi.rdf.api.Value;
import com.mobi.rdf.orm.Thing;
import com.mobi.rdf.orm.conversion.ValueConversionException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class DoubleValueConverter_ArrowTest {

    private DoubleValueConverter converter;

    @Mock
    private Value value;

    @Mock
    private Thing thing;

    @Mock
    private Literal literal;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        converter = new DoubleValueConverter();
    }

    @Test
    public void testConvertValue_ValidDoubleString() throws ValueConversionException {
        when(value.stringValue()).thenReturn("3.14159");
        Double result = converter.convertValue(value, thing, Double.class);
        assertEquals(3.14159, result, 0.00001);
    }

    @Test(expected = ValueConversionException.class)
    public void testConvertValue_InvalidDoubleString() throws ValueConversionException {
        when(value.stringValue()).thenReturn("not_a_number");
        converter.convertValue(value, thing, Double.class);
    }

    @Test
    public void testConvertType_NormalDouble() throws ValueConversionException {
        Double input = 2.71828;
        when(thing.getValueFactory()).thenReturn(new com.mobi.rdf.api.impl.ValueFactoryImpl());
        Value result = converter.convertType(input, thing);
        assertNotNull(result);
        assertEquals(input.toString(), result.stringValue());
    }

    @Test
    public void testConvertType_ZeroDouble() throws ValueConversionException {
        Double input = 0.0;
        when(thing.getValueFactory()).thenReturn(new com.mobi.rdf.api.impl.ValueFactoryImpl());
        Value result = converter.convertType(input, thing);
        assertNotNull(result);
        assertEquals(input.toString(), result.stringValue());
    }

    @Test
    public void testConvertType_NegativeDouble() throws ValueConversionException {
        Double input = -1.5;
        when(thing.getValueFactory()).thenReturn(new com.mobi.rdf.api.impl.ValueFactoryImpl());
        Value result = converter.convertType(input, thing);
        assertNotNull(result);
        assertEquals(input.toString(), result.stringValue());
    }

    @Test
    public void testConvertType_LargeDouble() throws ValueConversionException {
        Double input = 123456789.987654321;
        when(thing.getValueFactory()).thenReturn(new com.mobi.rdf.api.impl.ValueFactoryImpl());
        Value result = converter.convertType(input, thing);
        assertNotNull(result);
        assertEquals(input.toString(), result.stringValue());
    }
}
