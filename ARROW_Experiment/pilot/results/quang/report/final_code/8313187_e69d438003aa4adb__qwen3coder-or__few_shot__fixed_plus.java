package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import com.github.resource4j.extras.config.ConfigResourceBundleParser;
import com.github.resource4j.resources.discovery.ContentType;
import com.github.resource4j.resources.discovery.ResourceBundleParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void testParseReturnsParsedMap() {
        // Create a mock ResourceObject
        ResourceObject resourceObject = new ResourceObject() {
            @Override
            public ResourceBundleParser parsedTo(ResourceBundleParser parser) {
                return parser;
            }
            
            @Override
            public Map<String, String> asIs() {
                return new HashMap<String, String>();
            }

            @Override
            public InputStream asStream() {
                return new ByteArrayInputStream(new byte[0]);
            }

            @Override
            public long lastModified() {
                return 0L;
            }

            @Override
            public int size() {
                return 0;
            }
        };
        
        // Create the parser instance
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();
        
        // Create a sample map to be returned by the parsedTo method
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("key1", "value1");
        expectedMap.put("key2", "value2");
        
        // Mock the behavior of the ResourceObject
        ResourceBundleParser parserMock = new ResourceBundleParser() {
            @Override
            public Map<String, String> parse(ResourceObject resourceObject) {
                return expectedMap;
            }
        };
        
        // Set up the mock behavior
        ResourceObject finalResourceObject = resourceObject;
        ResourceBundleParser finalParserMock = parserMock;
        
        // Call the method under test
        Map<String, String> result = parser.parse(finalResourceObject);
        
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
