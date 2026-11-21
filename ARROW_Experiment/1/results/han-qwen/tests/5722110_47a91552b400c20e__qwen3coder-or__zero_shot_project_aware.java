package org.jtransfo;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ReadOnlyDomainAutomaticSetTypeConverter_ArrowTest {

    @Test
    public void testNoArgConstructor() {
        ReadOnlyDomainAutomaticSetTypeConverter converter = new ReadOnlyDomainAutomaticSetTypeConverter();
        assertNotNull(converter);
    }

    @Test
    public void testNamedConstructor() {
        ReadOnlyDomainAutomaticSetTypeConverter converter = new ReadOnlyDomainAutomaticSetTypeConverter("testName");
        assertNotNull(converter);
    }

    @Test
    public void testCanConvertReturnsFalse() {
        ReadOnlyDomainAutomaticSetTypeConverter converter = new ReadOnlyDomainAutomaticSetTypeConverter();
        boolean result = converter.canConvert(String.class, Integer.class);
        assertFalse(result);
    }

    @Test
    public void testDoConvertOne() throws Exception {
        ReadOnlyDomainAutomaticSetTypeConverter converter = new ReadOnlyDomainAutomaticSetTypeConverter();
        JTransfo jTransfo = mock(JTransfo.class);
        Object toObject = new Object();
        Class<?> domainObjectType = String.class;
        String[] tags = {"tag1", "tag2"};

        Object expectedResult = new Object();
        when(jTransfo.findTarget(toObject, domainObjectType, tags)).thenReturn(expectedResult);

        Object result = converter.doConvertOne(jTransfo, toObject, domainObjectType, tags);

        assertEquals(expectedResult, result);
        verify(jTransfo).findTarget(toObject, domainObjectType, tags);
    }
}
