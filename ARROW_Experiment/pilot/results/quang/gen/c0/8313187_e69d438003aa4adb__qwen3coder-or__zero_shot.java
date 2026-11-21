package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import com.github.resource4j.resources.discovery.ContentType;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void testParseReturnsParsedMap() {
        // Given
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();
        ResourceObject resource = mock(ResourceObject.class);
        Map<String, String> expectedMap = new java.util.HashMap<>();
        expectedMap.put("key1", "value1");
        expectedMap.put("key2", "value2");

        // When
        when(resource.parsedTo(any(ConfigMapParser.class))).thenReturn(new com.github.resource4j.resources.parsers.ParsingResult(expectedMap));
        
        Map<String, String> result = parser.parse(resource);

        // Then
        assertEquals(expectedMap, result);
    }

    @Test
    public void testContentTypeAnnotation() {
        // Given
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();
        
        // When
        ContentType contentType = parser.getClass().getAnnotation(ContentType.class);
        
        // Then
        assertNotNull(contentType);
        assertEquals("conf", contentType.extension());
        assertEquals("application/hocon", contentType.mimeType());
    }
}
