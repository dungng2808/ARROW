package com.ufoscout.properlty;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;
import org.junit.Test;

public class Properlty_ArrowTest {

    private Properlty properties(String... entries) {
        ProperltyBuilder builder = Properlty.builder();
        for (String entry : entries) {
            int separator = entry.indexOf('=');
            builder.add(entry.substring(0, separator), entry.substring(separator + 1));
        }
        return builder.build();
    }

    private enum Mode {
        FAST,
        SLOW
    }

    @Test
    public void getsStringAndDefaultValues() {
        Properlty properties = properties("name=Alice");
        assertEquals(Optional.of("Alice"), properties.get("name"));
        assertEquals("Alice", properties.get("name", "unknown"));
        assertEquals("unknown", properties.get("missing", "unknown"));
    }

    @Test
    public void mapsStringValues() {
        Properlty properties = properties("count=12");
        assertEquals(Optional.of(12), properties.get("count", Integer::parseInt));
        assertEquals(Integer.valueOf(12), properties.get("count", Integer.valueOf(-1), Integer::parseInt));
        assertEquals(Integer.valueOf(-1), properties.get("missing", Integer.valueOf(-1), Integer::parseInt));
    }

    @Test
    public void getsIntegerValuesAndDefaults() {
        Properlty properties = properties("count=12");
        assertEquals(Optional.of(12), properties.getInt("count"));
        assertEquals(12, properties.getInt("count", -1));
        assertEquals(-1, properties.getInt("missing", -1));
    }

    @Test
    public void getsBooleanValuesAndDefaults() {
        Properlty properties = properties("enabled=TrUe", "disabled=false");
        assertEquals(Optional.of(true), properties.getBoolean("enabled"));
        assertEquals(Optional.of(false), properties.getBoolean("disabled"));
        assertTrue(properties.getBoolean("enabled", false));
        assertFalse(properties.getBoolean("missing", false));
    }

    @Test
    public void getsDoubleValuesAndDefaults() {
        Properlty properties = properties("value=2.5");
        assertEquals(Optional.of(2.5), properties.getDouble("value"));
        assertEquals(2.5, properties.getDouble("value", -1.0), 0.0);
        assertEquals(-1.0, properties.getDouble("missing", -1.0), 0.0);
    }

    @Test
    public void getsFloatValuesAndDefaults() {
        Properlty properties = properties("value=2.5");
        assertEquals(Optional.of(2.5f), properties.getFloat("value"));
        assertEquals(2.5f, properties.getFloat("value", -1.0f), 0.0f);
        assertEquals(-1.0f, properties.getFloat("missing", -1.0f), 0.0f);
    }

    @Test
    public void getsLongValuesAndDefaults() {
        Properlty properties = properties("value=123456789");
        assertEquals(Optional.of(123456789L), properties.getLong("value"));
        assertEquals(123456789L, properties.getLong("value", -1L));
        assertEquals(-1L, properties.getLong("missing", -1L));
    }

    @Test
    public void getsBigDecimalValuesAndDefaults() {
        Properlty properties = properties("value=12.50");
        assertEquals(Optional.of(new BigDecimal("12.50")), properties.getBigDecimal("value"));
        assertEquals(new BigDecimal("12.50"), properties.getBigDecimal("value", BigDecimal.ZERO));
        assertEquals(BigDecimal.ZERO, properties.getBigDecimal("missing", BigDecimal.ZERO));
    }

    @Test
    public void getsBigIntegerValuesAndDefaults() {
        Properlty properties = properties("value=12345678901234567890");
        assertEquals(Optional.of(new BigInteger("12345678901234567890")), properties.getBigInteger("value"));
        assertEquals(new BigInteger("12345678901234567890"),
                properties.getBigInteger("value", BigInteger.ZERO));
        assertEquals(BigInteger.ZERO, properties.getBigInteger("missing", BigInteger.ZERO));
    }

    @Test
    public void getsEnumValuesAndDefaults() {
        Properlty properties = properties("mode=FAST");
        assertEquals(Optional.of(Mode.FAST), properties.getEnum("mode", Mode.class));
        assertEquals(Mode.FAST, properties.getEnum("mode", Mode.SLOW));
        assertEquals(Mode.SLOW, properties.getEnum("missing", Mode.SLOW));
    }

    @Test
    public void getsArraysAndLists() {
        Properlty properties = properties("items=red,green,blue", "custom=one|two");
        assertArrayEquals(new String[] {"red", "green", "blue"}, properties.getArray("items"));
        assertArrayEquals(new String[] {"one", "two"}, properties.getArray("custom", "\\|"));
        assertEquals(Arrays.asList("red", "green", "blue"), properties.getList("items"));
        assertEquals(Arrays.asList("one", "two"), properties.getList("custom", "\\|"));
        assertEquals(new String[0], properties.getArray("missing"));
    }

    @Test
    public void mapsListElements() {
        Properlty properties = properties("items=1,2,3", "custom=4|5");
        assertEquals(Arrays.asList(1, 2, 3), properties.getList("items", Integer::parseInt));
        assertEquals(Arrays.asList(4, 5),
                properties.getList("custom", "\\|", Integer::parseInt));
    }
}
