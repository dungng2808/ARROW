package org.jtransfo;

import java.lang.reflect.Type;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

public class ReadOnlyDomainAutomaticSetTypeConverter_ArrowTest {

    @Test
    public void canConvertAlwaysReturnsFalse() {
        ReadOnlyDomainAutomaticSetTypeConverter converter =
                new ReadOnlyDomainAutomaticSetTypeConverter();
        Type toType = String.class;
        Type domainType = Integer.class;

        assertFalse(converter.canConvert(toType, domainType));
    }

    @Test
    public void canConvertReturnsFalseForNullTypes() {
        ReadOnlyDomainAutomaticSetTypeConverter converter =
                new ReadOnlyDomainAutomaticSetTypeConverter();

        assertFalse(converter.canConvert(null, null));
    }

    @Test
    public void namedConstructorCreatesConverter() {
        ReadOnlyDomainAutomaticSetTypeConverter converter =
                new ReadOnlyDomainAutomaticSetTypeConverter("custom");

        assertFalse(converter.canConvert(String.class, Integer.class));
    }

    @Test
    public void doConvertOneReturnsFoundTarget() throws JTransfoException {
        JTransfo jTransfo = new JTransfo() {
            @Override
            public Object findTarget(Object toObject, Class<?> domainObjectType, String... tags) {
                return toObject;
            }
        };
        ReadOnlyDomainAutomaticSetTypeConverter converter =
                new ReadOnlyDomainAutomaticSetTypeConverter();
        Object toObject = new Object();

        assertSame(toObject, converter.doConvertOne(jTransfo, toObject, Object.class));
    }
}
