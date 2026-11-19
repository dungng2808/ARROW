package org.evosuite.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NumberFormatter_ArrowTest {

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
    public void formatsString() {
        assertEquals("\"hello\"", NumberFormatter.getNumberString("hello"));
    }

    @Test
    public void escapesStringContents() {
        assertEquals("\"line\\nbreak\"", NumberFormatter.getNumberString("line\nbreak"));
    }

    @Test
    public void formatsPositiveFloat() {
        assertEquals("1.5F", NumberFormatter.getNumberString(Float.valueOf(1.5F)));
    }

    @Test
    public void formatsNegativeFloat() {
        assertEquals("(-1.5F)", NumberFormatter.getNumberString(Float.valueOf(-1.5F)));
    }

    @Test
    public void formatsFloatSpecialValues() {
        assertEquals("Float.NaN", NumberFormatter.getNumberString(Float.valueOf(Float.NaN)));
        assertEquals("Float.POSITIVE_INFINITY",
                NumberFormatter.getNumberString(Float.valueOf(Float.POSITIVE_INFINITY)));
        assertEquals("Float.NEGATIVE_INFINITY",
                NumberFormatter.getNumberString(Float.valueOf(Float.NEGATIVE_INFINITY)));
    }

    @Test
    public void formatsPositiveDouble() {
        assertEquals("2.5", NumberFormatter.getNumberString(Double.valueOf(2.5)));
    }

    @Test
    public void formatsNegativeDouble() {
        assertEquals("(-2.5)", NumberFormatter.getNumberString(Double.valueOf(-2.5)));
    }

    @Test
    public void formatsDoubleSpecialValues() {
        assertEquals("Double.NaN", NumberFormatter.getNumberString(Double.valueOf(Double.NaN)));
        assertEquals("Double.POSITIVE_INFINITY",
                NumberFormatter.getNumberString(Double.valueOf(Double.POSITIVE_INFINITY)));
        assertEquals("Double.NEGATIVE_INFINITY",
                NumberFormatter.getNumberString(Double.valueOf(Double.NEGATIVE_INFINITY)));
    }

    @Test
    public void formatsPositiveLong() {
        assertEquals("12L", NumberFormatter.getNumberString(Long.valueOf(12L)));
    }

    @Test
    public void formatsNegativeLong() {
        assertEquals("(-12L)", NumberFormatter.getNumberString(Long.valueOf(-12L)));
    }

    @Test
    public void formatsPositiveByte() {
        assertEquals("(byte)12", NumberFormatter.getNumberString(Byte.valueOf((byte) 12)));
    }

    @Test
    public void formatsNegativeByte() {
        assertEquals("(byte) (-12)", NumberFormatter.getNumberString(Byte.valueOf((byte) -12)));
    }

    @Test
    public void formatsPositiveShort() {
        assertEquals("(short)12", NumberFormatter.getNumberString(Short.valueOf((short) 12)));
    }

    @Test
    public void formatsNegativeShort() {
        assertEquals("(short) (-12)", NumberFormatter.getNumberString(Short.valueOf((short) -12)));
    }

    @Test
    public void formatsIntegerBoundaries() {
        assertEquals("Integer.MAX_VALUE",
                NumberFormatter.getNumberString(Integer.valueOf(Integer.MAX_VALUE)));
        assertEquals("Integer.MIN_VALUE",
                NumberFormatter.getNumberString(Integer.valueOf(Integer.MIN_VALUE)));
    }

    @Test
    public void formatsPositiveInteger() {
        assertEquals("12", NumberFormatter.getNumberString(Integer.valueOf(12)));
    }

    @Test
    public void formatsNegativeInteger() {
        assertEquals("(-12)", NumberFormatter.getNumberString(Integer.valueOf(-12)));
    }

    @Test
    public void formatsBoolean() {
        assertEquals("true", NumberFormatter.getNumberString(Boolean.TRUE));
    }

    @Test
    public void formatsEnum() {
        assertEquals("Thread.State.RUNNABLE",
                NumberFormatter.getNumberString(Thread.State.RUNNABLE));
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
    }

    @Test
    public void getsSimpleClassNameForOtherObjects() {
        assertEquals("StringBuilder", NumberFormatter.getBoxedClassName(new StringBuilder()));
    }
}
