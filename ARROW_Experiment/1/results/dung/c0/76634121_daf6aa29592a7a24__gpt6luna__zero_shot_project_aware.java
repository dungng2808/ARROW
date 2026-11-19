package org.apache.qpid.proton.codec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collection;

import org.junit.Test;

public class StringType_ArrowTest
{
    @Test
    public void getTypeClassReturnsStringClass()
    {
        assertEquals(String.class, StringType.class.getMethod("getTypeClass").getReturnType());
    }

    @Test
    public void calculateUtf8LengthHandlesAscii()
    {
        assertEquals(3, StringType.calculateUTF8Length("abc"));
    }

    @Test
    public void calculateUtf8LengthHandlesEmptyString()
    {
        assertEquals(0, StringType.calculateUTF8Length(""));
    }

    @Test
    public void calculateUtf8LengthHandlesTwoByteCharacters()
    {
        assertEquals(2, StringType.calculateUTF8Length("\u0080"));
    }

    @Test
    public void calculateUtf8LengthHandlesThreeByteCharacters()
    {
        assertEquals(3, StringType.calculateUTF8Length("\u0800"));
    }

    @Test
    public void calculateUtf8LengthHandlesSurrogatePair()
    {
        assertEquals(4, StringType.calculateUTF8Length("\uD83D\uDE00"));
    }

    @Test
    public void extendedCalculateUtf8LengthCountsRemainder()
    {
        assertEquals(5, StringType.extendedCalculateUTF8Length("a\u0080\u0800", 1, 3, 1));
    }

    @Test
    public void constructorIsPackagePrivate()
    {
        Constructor<?>[] constructors = StringType.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        assertTrue(!Modifier.isPublic(constructors[0].getModifiers()));
    }
}
