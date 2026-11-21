package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import com.github.resource4j.extras.config.ConfigResourceBundleParser;
import com.github.resource4j.resources.discovery.ContentType;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void testParseReturnsParsedMap() {
        // Create a mock ResourceObject
        ResourceObject resourceObject = mock(ResourceObject.class);
        
        // Create the parser instance
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();
        
        // Create a sample map to be returned by the parsedTo method
        Map<String, String> expectedMap = new java.util.HashMap<>();
        expectedMap.put("key1", "value1");
        expectedMap.put("key2", "value2");
        
        // Mock the behavior of the ResourceObject
        com.github.resource4j.resources.discovery.ResourceBundleParser parserMock = mock(com.github.resource4j.resources.discovery.ResourceBundleParser.class);
        when(resourceObject.parsedTo(any(com.github.resource4j.resources.discovery.ResourceBundleParser.class))).thenReturn(parserMock);
        when(parserMock.asIs()).thenReturn(expectedMap);
        
        // Call the method under test
        Map<String, String> result = parser.parse(resourceObject);
        
        // Verify the result
        assertNotNull(result);
        assertEquals(expectedMap, result);
    }

    @Test
    public void testContentTypeAnnotation() {
        // Check that the class has the correct ContentType annotation
        ContentType contentType = ConfigResourceBundleParser.class.getAnnotation(ContentType.class);
        assertNotNull(contentType);
        assertEquals("conf", contentType.extension());
        assertEquals("application/hocon", contentType.mimeType());
    }
}
