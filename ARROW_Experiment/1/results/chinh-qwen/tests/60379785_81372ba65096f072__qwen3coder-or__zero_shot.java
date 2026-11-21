package com.box.l10n.mojito.okapi.filters;

import net.sf.okapi.common.annotation.IAnnotation;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class FilterOptions_ArrowTest {

    @Test
    public void testConstructorWithNull() {
        FilterOptions filterOptions = new FilterOptions(null);
        assertNotNull(filterOptions);
        assertTrue(filterOptions.options.isEmpty());
    }

    @Test
    public void testConstructorWithEmptyList() {
        FilterOptions filterOptions = new FilterOptions(Arrays.asList());
        assertNotNull(filterOptions);
        assertTrue(filterOptions.options.isEmpty());
    }

    @Test
    public void testConstructorWithValidOptions() {
        List<String> options = Arrays.asList("key1=value1", "key2=value2");
        FilterOptions filterOptions = new FilterOptions(options);
        assertNotNull(filterOptions);
        assertEquals(2, filterOptions.options.size());
        assertEquals("value1", filterOptions.options.get("key1"));
        assertEquals("value2", filterOptions.options.get("key2"));
    }

    @Test
    public void testConstructorWithInvalidOptionFormat() {
        List<String> options = Arrays.asList("key1=value1", "invalidOption", "key2=value2");
        FilterOptions filterOptions = new FilterOptions(options);
        assertNotNull(filterOptions);
        assertEquals(2, filterOptions.options.size());
        assertEquals("value1", filterOptions.options.get("key1"));
        assertEquals("value2", filterOptions.options.get("key2"));
    }

    @Test
    public void testGetStringWhenKeyExists() {
        List<String> options = Arrays.asList("key1=value1", "key2=value2");
        FilterOptions filterOptions = new FilterOptions(options);
        
        AtomicReference<String> capturedValue = new AtomicReference<>();
        filterOptions.getString("key1", capturedValue::set);
        
        assertEquals("value1", capturedValue.get());
    }

    @Test
    public void testGetStringWhenKeyDoesNotExist() {
        List<String> options = Arrays.asList("key1=value1", "key2=value2");
        FilterOptions filterOptions = new FilterOptions(options);
        
        AtomicReference<String> capturedValue = new AtomicReference<>();
        filterOptions.getString("nonexistent", capturedValue::set);
        
        assertNull(capturedValue.get());
    }

    @Test
    public void testGetBooleanWhenKeyExists() {
        List<String> options = Arrays.asList("key1=true", "key2=false");
        FilterOptions filterOptions = new FilterOptions(options);
        
        AtomicReference<Boolean> capturedValue = new AtomicReference<>();
        filterOptions.getBoolean("key1", capturedValue::set);
        
        assertEquals(Boolean.TRUE, capturedValue.get());
    }

    @Test
    public void testGetBooleanWhenKeyDoesNotExist() {
        List<String> options = Arrays.asList("key1=true", "key2=false");
        FilterOptions filterOptions = new FilterOptions(options);
        
        AtomicReference<Boolean> capturedValue = new AtomicReference<>();
        filterOptions.getBoolean("nonexistent", capturedValue::set);
        
        assertNull(capturedValue.get());
    }

    @Test
    public void testImplementsIAnnotation() {
        FilterOptions filterOptions = new FilterOptions(null);
        assertTrue(filterOptions instanceof IAnnotation);
    }
}
