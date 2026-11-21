package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import com.github.resource4j.extras.config.ConfigResourceBundleParser;
import com.github.resource4j.resources.discovery.ContentType;
import com.github.resource4j.resources.discovery.ResourceBundleParser;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ConfigResourceBundleParser_ArrowTest {

    public void testParseReturnsParsedMap() {
        // Create a mock ResourceObject
        ResourceObject resourceObject = new ResourceObject() {
            @Override
            public ResourceBundleParser parsedTo(ResourceBundleParser parser) {
                return parser;
            }
            
            @Override
            public Object asIs() {
                Map<String, String> expectedMap = new HashMap<>();
                expectedMap.put("key1", "value1");
                expectedMap.put("key2", "value2");
                return expectedMap;
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
            public int size() {
                return 0;
            }
        };
        
        // Create the parser instance
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();
        
        // Call the method under test
        Map<String, String> result = parser.parse(resourceObject);
        
        // Verify the result
        assert result != null;
        assert result.size() == 2;
        assert "value1".equals(result.get("key1"));
        assert "value2".equals(result.get("key2"));
    }

    public void testContentTypeAnnotation() {
        // Check that the class has the correct ContentType annotation
        ContentType contentType = ConfigResourceBundleParser.class.getAnnotation(ContentType.class);
        assert contentType != null;
        assert "conf".equals(contentType.extension());
        assert "application/hocon".equals(contentType.mimeType());
    }
}
