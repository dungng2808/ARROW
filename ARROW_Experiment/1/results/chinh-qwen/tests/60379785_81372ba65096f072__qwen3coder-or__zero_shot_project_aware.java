package com.box.l10n.mojito.okapi.filters;

import net.sf.okapi.common.annotation.IAnnotation;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class FilterOptions_ArrowTest {

    @Test
    public void testImplementsIAnnotation() {
        FilterOptions filterOptions = new FilterOptions(null);
        assertTrue(filterOptions instanceof IAnnotation);
    }

    @Test
    public void testConstructorWithNullList() {
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
    public void testConstructorWithInvalidOptions() {
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
        
        final String[] capturedValue = {null};
        filterOptions.getString("key1", (value) -> capturedValue[0] = value);
        
        assertEquals("value1", capturedValue[0]);
    }

    @Test
    public void testGetStringWhenKeyDoesNotExist() {
        List<String> options = Arrays.asList("key1=value1", "key2=value2");
        FilterOptions filterOptions = new FilterOptions(options);
        
        final String[] capturedValue = {null};
        filterOptions.getString("nonexistent", (value) -> capturedValue[0] = value);
        
        assertNull(capturedValue[0]);
    }

    @Test
    public void testGetBooleanWhenKeyExists() {
        List<String> options = Arrays.asList("key1=true", "key2=false");
        FilterOptions filterOptions = new FilterOptions(options);
        
        final Boolean[] capturedValue = {null};
        filterOptions.getBoolean("key1", (value) -> capturedValue[0] = value);
        
        assertTrue(capturedValue[0]);
    }

    @Test
    public void testGetBooleanWhenKeyDoesNotExist() {
        List<String> options = Arrays.asList("key1=true", "key2=false");
        FilterOptions filterOptions = new FilterOptions(options);
        
        final Boolean[] capturedValue = {null};
        filterOptions.getBoolean("nonexistent", (value) -> capturedValue[0] = value);
        
        assertNull(capturedValue[0]);
    }

    @Test
    public void testGetBooleanWithInvalidValue() {
        List<String> options = Arrays.asList("key1=notabool");
        FilterOptions filterOptions = new FilterOptions(options);
        
        final Boolean[] capturedValue = {null};
        filterOptions.getBoolean("key1", (value) -> capturedValue[0] = value);
        
        assertFalse(capturedValue[0]);
    }
}
