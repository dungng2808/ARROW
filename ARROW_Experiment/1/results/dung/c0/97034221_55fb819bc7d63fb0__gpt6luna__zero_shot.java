package com.ufoscout.properlty;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;

public class Properlty_ArrowTest {

    private Properlty properties(String... entries) {
        ProperltyBuilder builder = Properlty.builder();
        for (int i = 0; i < entries.length; i += 2) {
            builder.add(entries[i], entries[i + 1]);
        }
        return builder.build();
    }

    @Test
    public void getsStringAndDefault() {
        Properlty properties = properties("name", "value");
        assertEquals(Optional.of("value"), properties.get("name"));
        assertEquals("fallback", properties.get("missing", "fallback"));
    }

    @Test
    public void mapsStringValues() {
        Properlty properties = properties("number", "12");
        assertEquals(Optional.of(12), properties.get("number", Integer::parseInt));
        assertEquals(Integer.valueOf(12), properties.get("number", 0, Integer::parseInt));
        assertEquals(Integer.valueOf(7), properties.get("missing", 7, Integer::parseInt));
    }

    @Test
    public void getsIntegerValues() {
        Properlty properties = properties("number", "42");
        assertEquals(Optional.of(42), properties.getInt("number"));
        assertEquals(42, properties.getInt("number", 0));
        assertEquals(9, properties.getInt("missing", 9));
    }

    @Test
    public void getsBooleanValues() {
        Properlty properties = properties("yes", "TrUe", "no", "false");
        assertEquals(Optional.of(true), properties.getBoolean("yes"));
        assertEquals(Optional.of(false), properties.getBoolean("no"));
        assertTrue(properties.getBoolean("missing", true));
        assertFalse(properties.getBoolean("missing", false));
    }

    @Test
    public void rejectsInvalidBoolean() {
        Properlty properties = properties("flag", "yes");
        try {
            properties.getBoolean("flag");
            fail("Expected invalid boolean value to be rejected");
        } catch (RuntimeException expected) {
            assertEquals("Cannot parse boolean value: [yes]", expected.getMessage());
        }
    }

    @Test
    public void getsDoubleFloatAndLongValues() {
        Properlty properties = properties("double", "2.5", "float", "1.25", "long", "123456789");
        assertEquals(Optional.of(2.5d), properties.getDouble("double"));
        assertEquals(2.5d, properties.getDouble("double", 0.0d), 0.0d);
        assertEquals(Optional.of(1.25f), properties.getFloat("float"));
        assertEquals(1.25f, properties.getFloat("float", 0.0f), 0.0f);
        assertEquals(Optional.of(123456789L), properties.getLong("long"));
        assertEquals(123456789L, properties.getLong("long", 0L));
    }

    @Test
    public void getsBigNumberValues() {
        Properlty properties = properties("decimal", "123.45", "integer", "98765432101234567890");
        assertEquals(Optional.of(new BigDecimal("123.45")), properties.getBigDecimal("decimal"));
        assertEquals(new BigDecimal("123.45"), properties.getBigDecimal("decimal", BigDecimal.ZERO));
        assertEquals(Optional.of(new BigInteger("98765432101234567890")), properties.getBigInteger("integer"));
        assertEquals(new BigInteger("98765432101234567890"),
                properties.getBigInteger("integer", BigInteger.ZERO));
    }

    @Test
    public void getsEnumValues() {
        Properlty properties = properties("color", "GREEN");
        assertEquals(Optional.of(Color.GREEN), properties.getEnum("color", Color.class));
        assertEquals(Color.GREEN, properties.getEnum("color", Color.RED));
        assertEquals(Color.RED, properties.getEnum("missing", Color.RED));
    }

    @Test
    public void getsArraysAndLists() {
        Properlty properties = properties("items", "one,two,three", "custom", "a|b");
        assertArrayEquals(new String[] {"one", "two", "three"}, properties.getArray("items"));
        assertArrayEquals(new String[] {"a", "b"}, properties.getArray("custom", "\\|"));
        assertEquals(Arrays.asList("one", "two", "three"), properties.getList("items"));
        assertEquals(Arrays.asList("a", "b"), properties.getList("custom", "\\|"));
        assertEquals(Arrays.asList(3, 3, 5), properties.getList("items", String::length));
        assertEquals(Arrays.asList("a", "b"), properties.getList("custom", "\\|", value -> value));
    }

    @Test
    public void missingArrayAndListAreEmpty() {
        Properlty properties = properties();
        assertArrayEquals(new String[0], properties.getArray("missing"));
        assertEquals(Collections.emptyList(), properties.getList("missing"));
        assertEquals(Collections.emptyList(), properties.getList("missing", "\\|", value -> value));
    }

    private enum Color {
        RED,
        GREEN
    }
}
