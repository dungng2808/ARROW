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
        final byte[] content = "key = value".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ResourceObject resource = new ResourceObject() {
            @Override
            public InputStream asStream() {
                return new ByteArrayInputStream(content);
            }

            @Override
            public long lastModified() {
                return 0L;
            }

            @Override
            public long size() {
                return content.length;
            }

            @Override
            public String actualName() {
                return "config.conf";
            }

            @Override
            public String name() {
                return "config.conf";
            }
        };

        Map<String, String> result = parser.parse(resource);

        assertEquals("value", result.get("key"));
    }
}
