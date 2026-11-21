package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import com.github.resource4j.resources.discovery.ContentType;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void testContentTypeAnnotation() {
        ContentType annotation = ConfigResourceBundleParser.class.getAnnotation(ContentType.class);
        assertNotNull(annotation);
        assertEquals("conf", annotation.extension());
        assertEquals("application/hocon", annotation.mimeType());
    }

    @Test
    public void testParseReturnsMap() {
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();
        
        // Create a mock ResourceObject that returns a known map when parsed
        ResourceObject mockFile = new ResourceObject() {
            @Override
            public Object parsedTo(Object parser) {
                // Return a simple map for testing
                return new SimpleParsedResult();
            }
            
            @Override
            public String name() {
                return "test.conf";
            }
            
            @Override
            public String path() {
                return "/test.conf";
            }
            
            @Override
            public InputStream asStream() {
                return null;
            }
            
            @Override
            public long lastModified() {
                return 0L;
            }
            
            @Override
            public long size() {
                return 0L;
            }
        };
        
        // Call the parse method
        Map<String, String> result = parser.parse(mockFile);
        
        // Since we're just testing the interface contract, we expect a non-null result
        assertNotNull(result);
    }

    // Helper class to simulate the parsed result
    private static class SimpleParsedResult {
        public Map<String, String> asIs() {
            return java.util.Collections.emptyMap();
        }
    }
}
