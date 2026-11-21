package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void parseReturnsParsedConfigMap() {
        final String content = "{\"key\":\"value\"}";
        ResourceObject file = new ResourceObject() {
            @Override
            public InputStream asStream() {
                return new ByteArrayInputStream(content.getBytes());
            }

            @Override
            public long lastModified() {
                return 0L;
            }

            @Override
            public long size() {
                return content.length();
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
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();

        Map<String, String> result = parser.parse(file);

        assertEquals("value", result.get("key"));
    }
}
