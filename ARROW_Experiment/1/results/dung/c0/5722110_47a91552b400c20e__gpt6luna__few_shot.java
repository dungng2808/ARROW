package org.jtransfo;

import java.lang.reflect.Type;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

public class ReadOnlyDomainAutomaticSetTypeConverter_ArrowTest {

    @Test
    public void canConvertReturnsFalse() {
        ReadOnlyDomainAutomaticSetTypeConverter converter = new ReadOnlyDomainAutomaticSetTypeConverter();
        Type toType = String.class;
        Type domainType = Integer.class;

        assertFalse(converter.canConvert(toType, domainType));
    }

    @Test
    public void customNameConstructorCanConvertReturnsFalse() {
        ReadOnlyDomainAutomaticSetTypeConverter converter =
                new ReadOnlyDomainAutomaticSetTypeConverter("custom");

        assertFalse(converter.canConvert(String.class, Integer.class));
    }

    @Test
    public void doConvertOneReturnsFoundTarget() throws Exception {
        JTransfo jTransfo = new JTransfo() {
            @Override
            public Object findTarget(Object toObject, Class<?> domainObjectType, String... tags) {
                return "found";
            }
        };
        ReadOnlyDomainAutomaticSetTypeConverter converter = new ReadOnlyDomainAutomaticSetTypeConverter();

        Object result = converter.doConvertOne(jTransfo, "transfer", String.class, "tag");

        assertSame("found", result);
    }
}
