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

    private Properlty properties() {
        return Properlty.builder()
                .withProperty("name", "Alice")
                .withProperty("integer", "42")
                .withProperty("boolean", "true")
                .withProperty("double", "3.5")
                .withProperty("float", "2.25")
                .withProperty("long", "123456789")
                .withProperty("decimal", "12.50")
                .withProperty("bigInteger", "9876543210123456789")
                .withProperty("color", "RED")
                .withProperty("items", "one,two,three")
                .build();
    }

    @Test
    public void getReturnsPropertyValue() {
        assertEquals(Optional.of("Alice"), properties().get("name"));
    }

    @Test
    public void getReturnsEmptyForMissingProperty() {
        assertEquals(Optional.empty(), properties().get("missing"));
    }

    @Test
    public void getWithDefaultReturnsDefaultForMissingProperty() {
        assertEquals("fallback", properties().get("missing", "fallback"));
    }

    @Test
    public void getMapsPropertyValue() {
        assertEquals(Optional.of(5), properties().get("name", String::length));
    }

    @Test
    public void getWithMappedDefaultReturnsMappedValue() {
        assertEquals(Integer.valueOf(5), properties().get("name", Integer.valueOf(-1), String::length));
    }

    @Test
    public void getIntParsesValue() {
        assertEquals(Optional.of(42), properties().getInt("integer"));
    }

    @Test
    public void getIntWithDefaultReturnsDefaultForMissingProperty() {
        assertEquals(7, properties().getInt("missing", 7));
    }

    @Test
    public void getBooleanParsesTrue() {
        assertEquals(Optional.of(true), properties().getBoolean("boolean"));
    }

    @Test
    public void getBooleanWithDefaultReturnsDefaultForMissingProperty() {
        assertFalse(properties().getBoolean("missing", false));
    }

    @Test
    public void getDoubleParsesValue() {
        assertEquals(Optional.of(3.5), properties().getDouble("double"));
    }

    @Test
    public void getDoubleWithDefaultReturnsDefaultForMissingProperty() {
        assertEquals(1.25, properties().getDouble("missing", 1.25), 0.0);
    }

    @Test
    public void getFloatParsesValue() {
        assertEquals(Optional.of(2.25f), properties().getFloat("float"));
    }

    @Test
    public void getFloatWithDefaultReturnsDefaultForMissingProperty() {
        assertEquals(1.5f, properties().getFloat("missing", 1.5f), 0.0f);
    }

    @Test
    public void getLongParsesValue() {
        assertEquals(Optional.of(123456789L), properties().getLong("long"));
    }

    @Test
    public void getLongWithDefaultReturnsDefaultForMissingProperty() {
        assertEquals(9L, properties().getLong("missing", 9L));
    }

    @Test
    public void getBigDecimalParsesValue() {
        assertEquals(Optional.of(new BigDecimal("12.50")), properties().getBigDecimal("decimal"));
    }

    @Test
    public void getBigDecimalWithDefaultReturnsDefaultForMissingProperty() {
        BigDecimal fallback = new BigDecimal("1.25");
        assertEquals(fallback, properties().getBigDecimal("missing", fallback));
    }

    @Test
    public void getBigIntegerParsesValue() {
        assertEquals(Optional.of(new BigInteger("9876543210123456789")), properties().getBigInteger("bigInteger"));
    }

    @Test
    public void getBigIntegerWithDefaultReturnsDefaultForMissingProperty() {
        BigInteger fallback = BigInteger.TEN;
        assertEquals(fallback, properties().getBigInteger("missing", fallback));
    }

    @Test
    public void getEnumParsesValue() {
        assertEquals(Optional.of(Color.RED), properties().getEnum("color", Color.class));
    }

    @Test
    public void getEnumWithDefaultReturnsDefaultForMissingProperty() {
        assertEquals(Color.BLUE, properties().getEnum("missing", Color.BLUE));
    }

    @Test
    public void getArraySplitsUsingDefaultSeparator() {
        assertArrayEquals(new String[] {"one", "two", "three"}, properties().getArray("items"));
    }

    @Test
    public void getArraySplitsUsingSpecifiedSeparator() {
        Properlty props = Properlty.builder().withProperty("items", "one|two").build();
        assertArrayEquals(new String[] {"one", "two"}, props.getArray("items", "\\|"));
    }

    @Test
    public void getArrayReturnsEmptyArrayForMissingProperty() {
        assertArrayEquals(new String[0], properties().getArray("missing"));
    }

    @Test
    public void getListSplitsUsingDefaultSeparator() {
        assertEquals(Arrays.asList("one", "two", "three"), properties().getList("items"));
    }

    @Test
    public void getListSplitsUsingSpecifiedSeparator() {
        Properlty props = Properlty.builder().withProperty("items", "one|two").build();
        assertEquals(Arrays.asList("one", "two"), props.getList("items", "\\|"));
    }

    @Test
    public void getListMapsElements() {
        assertEquals(Arrays.asList(3, 3, 5), properties().getList("items", String::length));
    }

    @Test
    public void getListMapsElementsUsingSpecifiedSeparator() {
        Properlty props = Properlty.builder().withProperty("items", "1|2|3").build();
        assertEquals(Arrays.asList(1, 2, 3), props.getList("items", "\\|", Integer::parseInt));
    }

    private enum Color {
        RED, BLUE
    }
}
