package org.jtransfo;

import java.lang.reflect.Type;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

public class ReadOnlyDomainAutomaticSetTypeConverter_ArrowTest {

    @Test
    public void canConvertAlwaysReturnsFalse() {
        ReadOnlyDomainAutomaticSetTypeConverter converter = new ReadOnlyDomainAutomaticSetTypeConverter();
        Type toType = String.class;
        Type domainType = Integer.class;

        assertFalse(converter.canConvert(toType, domainType));
    }

    @Test
    public void canConvertReturnsFalseForNullTypes() {
        ReadOnlyDomainAutomaticSetTypeConverter converter = new ReadOnlyDomainAutomaticSetTypeConverter();

        assertFalse(converter.canConvert(null, null));
    }

    @Test
    public void noArgumentConstructorCreatesConverter() {
        ReadOnlyDomainAutomaticSetTypeConverter converter = new ReadOnlyDomainAutomaticSetTypeConverter();

        assertSame(ReadOnlyDomainAutomaticSetTypeConverter.class, converter.getClass());
    }

    @Test
    public void namedConstructorCreatesConverter() {
        ReadOnlyDomainAutomaticSetTypeConverter converter = new ReadOnlyDomainAutomaticSetTypeConverter("custom");

        assertSame(ReadOnlyDomainAutomaticSetTypeConverter.class, converter.getClass());
    }
}
