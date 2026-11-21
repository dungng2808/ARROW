package com.box.l10n.mojito.okapi.filters;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class FilterOptions_ArrowTest {

    @Test
    public void getStringPassesValueForExistingKey() {
        FilterOptions options = new FilterOptions(Arrays.asList("format=xml"));
        AtomicReference<String> result = new AtomicReference<>();

        options.getString("format", result::set);

        assertEquals("xml", result.get());
    }

    @Test
    public void getStringPreservesEqualsSignsInValue() {
        FilterOptions options = new FilterOptions(Arrays.asList("expression=a=b"));
        AtomicReference<String> result = new AtomicReference<>();

        options.getString("expression", result::set);

        assertEquals("a=b", result.get());
    }

    @Test
    public void getStringDoesNotCallConsumerForMissingKey() {
        FilterOptions options = new FilterOptions(Collections.<String>emptyList());
        AtomicReference<String> result = new AtomicReference<>();

        options.getString("missing", result::set);

        assertEquals(null, result.get());
    }

    @Test
    public void getBooleanPassesParsedValueForExistingKey() {
        FilterOptions options = new FilterOptions(Arrays.asList("enabled=true"));
        AtomicReference<Boolean> result = new AtomicReference<>();

        options.getBoolean("enabled", result::set);

        assertEquals(Boolean.TRUE, result.get());
    }

    @Test
    public void getBooleanParsesNonTrueValueAsFalse() {
        FilterOptions options = new FilterOptions(Arrays.asList("enabled=not-true"));
        AtomicReference<Boolean> result = new AtomicReference<>();

        options.getBoolean("enabled", result::set);

        assertEquals(Boolean.FALSE, result.get());
    }

    @Test
    public void constructorIgnoresEntriesWithoutEquals() {
        FilterOptions options = new FilterOptions(Arrays.asList("invalid"));
        AtomicReference<String> result = new AtomicReference<>();

        options.getString("invalid", result::set);

        assertFalse(result.get() != null);
    }

    @Test
    public void constructorAcceptsNullOptions() {
        FilterOptions options = new FilterOptions(null);
        AtomicReference<Boolean> result = new AtomicReference<>();

        options.getBoolean("enabled", result::set);

        assertEquals(null, result.get());
    }
}
