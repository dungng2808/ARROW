package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import com.github.resource4j.extras.config.ConfigResourceBundleParser;
import com.github.resource4j.resources.discovery.ContentType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void testParseReturnsParsedMap() {
        // Create a mock ResourceObject
        ResourceObject resourceObject = new ResourceObject() {
            @Override
            public Object parsedTo(ResourceBundleParser parser) {
                return new ResourceBundleParser() {
                    @Override
                    public Object asIs() {
                        Map<String, String> expectedMap = new HashMap<>();
                        expectedMap.put("key1", "value1");
                        expectedMap.put("key2", "value2");
                        return expectedMap;
                    }
                };
            }
        };
        
        // Create the parser instance
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();
        
        // Call the method under test
        Map<String, String> result = parser.parse(resourceObject);
        
        // Verify the result
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
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
