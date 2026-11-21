package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import com.github.resource4j.resources.discovery.ContentType;
import com.github.resource4j.extras.config.ConfigResourceBundleParser;
import com.github.resource4j.extras.config.ConfigMapParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void testParseReturnsParsedMap() {
        // Given
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();
        ResourceObject resource = new ResourceObject() {
            @Override
            public Object parsedTo(Object parser) {
                Map<String, String> expectedMap = new HashMap<>();
                expectedMap.put("key1", "value1");
                expectedMap.put("key2", "value2");
                return new ConfigMapParser().parse(expectedMap);
            }
            
            @Override
            public Object asIs() {
                return null;
            }
            
            @Override
            public InputStream asStream() {
                return null;
            }
            
            @Override
            public long lastModified() {
                return 0;
            }
        };
        
        // When
        Map<String, String> result = parser.parse(resource);

        // Then
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("key1", "value1");
        expectedMap.put("key2", "value2");
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
