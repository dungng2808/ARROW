package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void parseReturnsMapForConfigResource() {
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();
        ResourceObject resource = new ResourceObject() {
            @Override
            public InputStream asStream() {
                return new ByteArrayInputStream("key = value".getBytes());
            }

            @Override
            public long lastModified() {
                return 0L;
            }

            @Override
            public long size() {
                return "key = value".length();
            }

            @Override
            public String actualName() {
                return "resource.conf";
            }

            @Override
            public String name() {
                return "resource.conf";
            }
        };

        Map<String, String> result = parser.parse(resource);

        assertEquals("value", result.get("key"));
    }
}
