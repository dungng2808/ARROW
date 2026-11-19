package org.evosuite.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NumberFormatter_ArrowTest {

    private enum Sample {
        VALUE
    }

    @Test
    public void formatsNull() {
        assertEquals("null", NumberFormatter.getNumberString(null));
    }

    @Test
    public void formatsCharacters() {
        assertEquals("'a'", NumberFormatter.getNumberString(Character.valueOf('a')));
        assertEquals("'\\''", NumberFormatter.getNumberString(Character.valueOf('\'')));
        assertEquals("'\\n'", NumberFormatter.getNumberString(Character.valueOf('\n')));
    }

    @Test
    public void formatsStrings() {
        assertEquals("\"hello\"", NumberFormatter.getNumberString("hello"));
        assertEquals("\"a\\nb\"", NumberFormatter.getNumberString("a\nb"));
    }

    @Test
    public void formatsFloatingPointValues() {
        assertEquals("1.5F", NumberFormatter.getNumberString(Float.valueOf(1.5F)));
        assertEquals("(-1.5F)", NumberFormatter.getNumberString(Float.valueOf(-1.5F)));
        assertEquals("Float.NaN", NumberFormatter.getNumberString(Float.valueOf(Float.NaN)));
        assertEquals("Float.POSITIVE_INFINITY", NumberFormatter.getNumberString(Float.valueOf(Float.POSITIVE_INFINITY)));
        assertEquals("Float.NEGATIVE_INFINITY", NumberFormatter.getNumberString(Float.valueOf(Float.NEGATIVE_INFINITY)));

        assertEquals("1.5", NumberFormatter.getNumberString(Double.valueOf(1.5)));
        assertEquals("(-1.5)", NumberFormatter.getNumberString(Double.valueOf(-1.5)));
        assertEquals("Double.NaN", NumberFormatter.getNumberString(Double.valueOf(Double.NaN)));
        assertEquals("Double.POSITIVE_INFINITY", NumberFormatter.getNumberString(Double.valueOf(Double.POSITIVE_INFINITY)));
        assertEquals("Double.NEGATIVE_INFINITY", NumberFormatter.getNumberString(Double.valueOf(Double.NEGATIVE_INFINITY)));
    }

    @Test
    public void formatsIntegralValues() {
        assertEquals("12L", NumberFormatter.getNumberString(Long.valueOf(12L)));
        assertEquals("(-12L)", NumberFormatter.getNumberString(Long.valueOf(-12L)));
        assertEquals("(byte)3", NumberFormatter.getNumberString(Byte.valueOf((byte) 3)));
        assertEquals("(byte) (-3)", NumberFormatter.getNumberString(Byte.valueOf((byte) -3)));
        assertEquals("(short)4", NumberFormatter.getNumberString(Short.valueOf((short) 4)));
        assertEquals("(short) (-4)", NumberFormatter.getNumberString(Short.valueOf((short) -4)));
        assertEquals("12", NumberFormatter.getNumberString(Integer.valueOf(12)));
        assertEquals("(-12)", NumberFormatter.getNumberString(Integer.valueOf(-12)));
        assertEquals("Integer.MAX_VALUE", NumberFormatter.getNumberString(Integer.valueOf(Integer.MAX_VALUE)));
        assertEquals("Integer.MIN_VALUE", NumberFormatter.getNumberString(Integer.valueOf(Integer.MIN_VALUE)));
    }

    @Test
    public void formatsEnums() {
        assertEquals("NumberFormatter_ArrowTest.Sample.VALUE",
                NumberFormatter.getNumberString(Sample.VALUE));
    }

    @Test
    public void formatsBoolean() {
        assertEquals("true", NumberFormatter.getNumberString(Boolean.TRUE));
        assertEquals("false", NumberFormatter.getNumberString(Boolean.FALSE));
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
        assertEquals("char", NumberFormatter.getBoxedClassName(Character.valueOf('x')));
        assertEquals("String", NumberFormatter.getBoxedClassName("text"));
    }
}
