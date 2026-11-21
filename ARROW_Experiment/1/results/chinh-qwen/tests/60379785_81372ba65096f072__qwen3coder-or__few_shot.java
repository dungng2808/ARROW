package com.box.l10n.mojito.okapi.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.sf.okapi.common.annotation.IAnnotation;
import org.junit.Test;

public class FilterOptions_ArrowTest {

    @Test
    public void testFilterOptionsWithNull() {
        FilterOptions filterOptions = new FilterOptions(null);
        assertEquals(0, filterOptions.options.size());
    }

    @Test
    public void testFilterOptionsWithEmptyList() {
        FilterOptions filterOptions = new FilterOptions(Lists.newArrayList());
        assertEquals(0, filterOptions.options.size());
    }

    @Test
    public void testFilterOptionsWithValidOptions() {
        List<String> options = Lists.newArrayList("key1=value1", "key2=value2");
        FilterOptions filterOptions = new FilterOptions(options);
        assertEquals(2, filterOptions.options.size());
        assertEquals("value1", filterOptions.options.get("key1"));
        assertEquals("value2", filterOptions.options.get("key2"));
    }

    @Test
    public void testFilterOptionsWithInvalidOption() {
        List<String> options = Lists.newArrayList("key1=value1", "invalidOption", "key2=value2");
        FilterOptions filterOptions = new FilterOptions(options);
        assertEquals(2, filterOptions.options.size());
        assertEquals("value1", filterOptions.options.get("key1"));
        assertEquals("value2", filterOptions.options.get("key2"));
    }

    @Test
    public void testGetStringWhenKeyExists() {
        List<String> options = Lists.newArrayList("key1=value1", "key2=value2");
        FilterOptions filterOptions = new FilterOptions(options);
        AtomicReference<String> result = new AtomicReference<>();
        filterOptions.getString("key1", result::set);
        assertEquals("value1", result.get());
    }

    @Test
    public void testGetStringWhenKeyDoesNotExist() {
        List<String> options = Lists.newArrayList("key1=value1", "key2=value2");
        FilterOptions filterOptions = new FilterOptions(options);
        AtomicReference<String> result = new AtomicReference<>();
        filterOptions.getString("nonexistent", result::set);
        assertNull(result.get());
    }

    @Test
    public void testGetBooleanWhenKeyExists() {
        List<String> options = Lists.newArrayList("key1=true", "key2=false");
        FilterOptions filterOptions = new FilterOptions(options);
        AtomicReference<Boolean> result = new AtomicReference<>();
        filterOptions.getBoolean("key1", result::set);
        assertEquals(Boolean.TRUE, result.get());
    }

    @Test
    public void testGetBooleanWhenKeyDoesNotExist() {
        List<String> options = Lists.newArrayList("key1=true", "key2=false");
        FilterOptions filterOptions = new FilterOptions(options);
        AtomicReference<Boolean> result = new AtomicReference<>();
        filterOptions.getBoolean("nonexistent", result::set);
        assertNull(result.get());
    }

    @Test
    public void testIAnnotationInterface() {
        FilterOptions filterOptions = new FilterOptions(null);
        assertEquals(IAnnotation.class, filterOptions.getClass().getInterfaces()[0]);
    }
}
