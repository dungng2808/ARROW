package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import com.github.resource4j.resources.discovery.ContentType;
import com.github.resource4j.resources.parsers.ParsingResult;
import com.github.resource4j.resources.parsers.ConfigMapParser;
import com.github.resource4j.resources.discovery.ResourceBundleParser;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ConfigResourceBundleParser_ArrowTest {

    @org.junit.Test
    public void testParseReturnsParsedMap() {
        // Given
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();
        ResourceObject resource = mock(ResourceObject.class);
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("key1", "value1");
        expectedMap.put("key2", "value2");

        // When
        when(resource.parsedTo(any(ConfigMapParser.class))).thenReturn(new ParsingResult(expectedMap));
        
        Map<String, String> result = parser.parse(resource);

        // Then
        assertEquals(expectedMap, result);
    }

    @org.junit.Test
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
