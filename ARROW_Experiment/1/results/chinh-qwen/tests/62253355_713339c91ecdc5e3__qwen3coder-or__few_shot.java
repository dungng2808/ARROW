package com.mobi.rdf.orm.conversion.impl;

import com.mobi.rdf.api.Value;
import com.mobi.rdf.api.Literal;
import com.mobi.rdf.orm.Thing;
import com.mobi.rdf.orm.conversion.ValueConversionException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DoubleValueConverter_ArrowTest {

    private DoubleValueConverter converter;
    private Value mockValue;
    private Thing mockThing;
    private Literal mockLiteral;

    @Before
    public void setUp() {
        converter = new DoubleValueConverter();
        mockValue = mock(Value.class);
        mockThing = mock(Thing.class);
        mockLiteral = mock(Literal.class);
    }

    @Test
    public void testConvertValue_Success() throws ValueConversionException {
        when(mockValue.stringValue()).thenReturn("3.14159");
        
        Double result = converter.convertValue(mockValue, mockThing, Double.class);
        
        assertEquals(3.14159, result, 0.00001);
    }

    @Test(expected = ValueConversionException.class)
    public void testConvertValue_NumberFormatException() throws ValueConversionException {
        when(mockValue.stringValue()).thenReturn("not_a_number");
        
        converter.convertValue(mockValue, mockThing, Double.class);
    }

    @Test
    public void testConvertType_Success() throws ValueConversionException {
        when(mockThing.getValueFactory()).thenReturn(mock(org.mobi.rdf.api.ValueFactory.class));
        when(mockThing.getValueFactory().createLiteral(anyDouble())).thenReturn(mockLiteral);
        
        Value result = converter.convertType(2.71828, mockThing);
        
        assertNotNull(result);
        assertEquals(mockLiteral, result);
    }
}
