package com.box.l10n.mojito.okapi.filters;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FilterOptions_ArrowTest {

    @Test
    public void getStringReturnsValueForExistingKey() {
        FilterOptions options = new FilterOptions(Arrays.asList("name=value"));
        AtomicReference<String> result = new AtomicReference<>();

        options.getString("name", result::set);

        assertEquals("value", result.get());
    }

    @Test
    public void getStringPreservesEqualsSignsInValue() {
        FilterOptions options = new FilterOptions(Arrays.asList("name=value=more"));
        AtomicReference<String> result = new AtomicReference<>();

        options.getString("name", result::set);

        assertEquals("value=more", result.get());
    }

    @Test
    public void getStringDoesNotCallConsumerForMissingKey() {
        FilterOptions options = new FilterOptions(Arrays.asList("name=value"));
        AtomicReference<String> result = new AtomicReference<>();

        options.getString("missing", result::set);

        assertNull(result.get());
    }

    @Test
    public void getBooleanConvertsExistingValue() {
        FilterOptions options = new FilterOptions(Arrays.asList("enabled=true"));
        AtomicReference<Boolean> result = new AtomicReference<>();

        options.getBoolean("enabled", result::set);

        assertEquals(Boolean.TRUE, result.get());
    }

    @Test
    public void getBooleanConvertsNonTrueValueToFalse() {
        FilterOptions options = new FilterOptions(Arrays.asList("enabled=not-true"));
        AtomicReference<Boolean> result = new AtomicReference<>();

        options.getBoolean("enabled", result::set);

        assertEquals(Boolean.FALSE, result.get());
    }

    @Test
    public void getBooleanDoesNotCallConsumerForMissingKey() {
        FilterOptions options = new FilterOptions(Arrays.asList("enabled=true"));
        AtomicReference<Boolean> result = new AtomicReference<>();

        options.getBoolean("missing", result::set);

        assertNull(result.get());
    }

    @Test
    public void constructorAcceptsNullOptions() {
        FilterOptions options = new FilterOptions(null);
        AtomicReference<String> result = new AtomicReference<>();

        options.getString("name", result::set);

        assertNull(result.get());
    }

    @Test
    public void constructorIgnoresOptionsWithoutEqualsSign() {
        FilterOptions options = new FilterOptions(Collections.singletonList("name"));
        AtomicReference<String> result = new AtomicReference<>();

        options.getString("name", result::set);

        assertNull(result.get());
    }

    @Test
    public void laterDuplicateOptionReplacesEarlierValue() {
        FilterOptions options = new FilterOptions(Arrays.asList("name=first", "name=second"));
        AtomicReference<String> result = new AtomicReference<>();

        options.getString("name", result::set);

        assertEquals("second", result.get());
    }
}
