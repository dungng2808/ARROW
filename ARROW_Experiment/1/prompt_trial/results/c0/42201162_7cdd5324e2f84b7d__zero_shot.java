package org.evosuite.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NumberFormatter_ArrowTest {

    private enum Sample {
        FIRST, SECOND
    }

    @Test
    public void formatsNull() {
        assertEquals("null", NumberFormatter.getNumberString(null));
    }

    @Test
    public void formatsCharacter() {
        assertEquals("'a'", NumberFormatter.getNumberString(Character.valueOf('a')));
    }

    @Test
    public void escapesSingleQuoteCharacter() {
        assertEquals("'\\''", NumberFormatter.getNumberString(Character.valueOf('\'')));
    }

    @Test
    public void formatsStringWithEscapedNewline() {
        assertEquals("\"a\\nb\"", NumberFormatter.getNumberString("a\nb"));
    }

    @Test
    public void formatsNegativeFloat() {
        assertEquals("(-1.5F)", NumberFormatter.getNumberString(Float.valueOf(-1.5F)));
    }

    @Test
    public void formatsPositiveFloat() {
        assertEquals("1.5F", NumberFormatter.getNumberString(Float.valueOf(1.5F)));
    }

    @Test
    public void formatsFloatSpecialValues() {
        assertEquals("Float.NaN", NumberFormatter.getNumberString(Float.valueOf(Float.NaN)));
        assertEquals("Float.NEGATIVE_INFINITY", NumberFormatter.getNumberString(Float.valueOf(Float.NEGATIVE_INFINITY)));
        assertEquals("Float.POSITIVE_INFINITY", NumberFormatter.getNumberString(Float.valueOf(Float.POSITIVE_INFINITY)));
    }

    @Test
    public void formatsNegativeDouble() {
        assertEquals("(-2.5)", NumberFormatter.getNumberString(Double.valueOf(-2.5)));
    }

    @Test
    public void formatsDoubleSpecialValues() {
        assertEquals("Double.NaN", NumberFormatter.getNumberString(Double.valueOf(Double.NaN)));
        assertEquals("Double.NEGATIVE_INFINITY", NumberFormatter.getNumberString(Double.valueOf(Double.NEGATIVE_INFINITY)));
        assertEquals("Double.POSITIVE_INFINITY", NumberFormatter.getNumberString(Double.valueOf(Double.POSITIVE_INFINITY)));
    }

    @Test
    public void formatsNegativeLong() {
        assertEquals("(-3L)", NumberFormatter.getNumberString(Long.valueOf(-3L)));
    }

    @Test
    public void formatsNegativeByte() {
        assertEquals("(byte) (-3)", NumberFormatter.getNumberString(Byte.valueOf((byte) -3)));
    }

    @Test
    public void formatsNegativeShort() {
        assertEquals("(short) (-3)", NumberFormatter.getNumberString(Short.valueOf((short) -3)));
    }

    @Test
    public void formatsIntegerBoundariesAndNegativeValue() {
        assertEquals("Integer.MAX_VALUE", NumberFormatter.getNumberString(Integer.valueOf(Integer.MAX_VALUE)));
        assertEquals("Integer.MIN_VALUE", NumberFormatter.getNumberString(Integer.valueOf(Integer.MIN_VALUE)));
        assertEquals("(-3)", NumberFormatter.getNumberString(Integer.valueOf(-3)));
    }

    @Test
    public void formatsBoolean() {
        assertEquals("true", NumberFormatter.getNumberString(Boolean.TRUE));
    }

    @Test
    public void formatsEnum() {
        assertEquals("NumberFormatter_ArrowTest.Sample.FIRST",
                NumberFormatter.getNumberString(Sample.FIRST));
    }

    @Test
    public void getsBoxedClassNames() {
        assertEquals("double", NumberFormatter.getBoxedClassName(Double.valueOf(1.0)));
        assertEquals("float", NumberFormatter.getBoxedClassName(Float.valueOf(1.0F)));
        assertEquals("long", NumberFormatter.getBoxedClassName(Long.valueOf(1L)));
        assertEquals("boolean", NumberFormatter.getBoxedClassName(Boolean.TRUE));
        assertEquals("short", NumberFormatter.getBoxedClassName(Short.valueOf((short) 1)));
        assertEquals("int", NumberFormatter.getBoxedClassName(Integer.valueOf(1)));
        assertEquals("byte", NumberFormatter.getBoxedClassName(Byte.valueOf((byte) 1)));
        assertEquals("char", NumberFormatter.getBoxedClassName(Character.valueOf('a')));
        assertEquals("String", NumberFormatter.getBoxedClassName("text"));
    }
}
