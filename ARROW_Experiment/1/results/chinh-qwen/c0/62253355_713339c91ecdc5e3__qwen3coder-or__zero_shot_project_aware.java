package com.mobi.rdf.orm.conversion.impl;

import com.mobi.rdf.api.Literal;
import com.mobi.rdf.api.Value;
import com.mobi.rdf.api.ValueFactory;
import com.mobi.rdf.orm.Thing;
import com.mobi.rdf.orm.conversion.ValueConversionException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class DoubleValueConverter_ArrowTest {

    private DoubleValueConverter converter;

    @Mock
    private Thing thing;

    @Mock
    private ValueFactory valueFactory;

    @Mock
    private Value value;

    @Mock
    private Literal literal;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        converter = new DoubleValueConverter();
        when(thing.getValueFactory()).thenReturn(valueFactory);
    }

    @Test
    public void testConvertValue_NormalCase() throws ValueConversionException {
        String stringValue = "3.14159";
        when(value.stringValue()).thenReturn(stringValue);
        
        Double result = converter.convertValue(value, thing, Double.class);
        
        assertEquals(3.14159, result, 0.00001);
    }

    @Test(expected = ValueConversionException.class)
    public void testConvertValue_NumberFormatException() throws ValueConversionException {
        String invalidString = "not_a_number";
        when(value.stringValue()).thenReturn(invalidString);
        
        converter.convertValue(value, thing, Double.class);
    }

    @Test
    public void testConvertType_NormalCase() throws ValueConversionException {
        Double doubleValue = 2.71828;
        when(valueFactory.createLiteral(doubleValue)).thenReturn(literal);
        
        Value result = converter.convertType(doubleValue, thing);
        
        assertNotNull(result);
        assertEquals(literal, result);
    }
}
