package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import com.github.resource4j.resources.discovery.ContentType;
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
                if (parser instanceof ConfigMapParser) {
                    Map<String, String> expectedMap = new HashMap<>();
                    expectedMap.put("key1", "value1");
                    expectedMap.put("key2", "value2");
                    return new ParsingResult(expectedMap);
                }
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

            @Override
            public long size() {
                return 0;
            }

            @Override
            public String actualName() {
                return "test.conf";
            }

            @Override
            public String name() {
                return "test";
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
    
    // Mock class to simulate ParsingResult
    private static class ParsingResult {
        private final Map<String, String> map;
        
        public ParsingResult(Map<String, String> map) {
            this.map = map;
        }
        
        public Object asIs() {
            return map;
        }
    }
}
