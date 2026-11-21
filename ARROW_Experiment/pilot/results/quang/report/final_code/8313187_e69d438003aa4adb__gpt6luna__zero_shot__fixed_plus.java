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
        ResourceObject file = new ResourceObject() {
            @Override
            public String name() {
                return "config.conf";
            }

            @Override
            public String actualName() {
                return "config.conf";
            }

            @Override
            public long size() {
                return 13L;
            }

            @Override
            public long lastModified() {
                return 0L;
            }

            @Override
            public InputStream asStream() {
                return new ByteArrayInputStream("key = value".getBytes());
            }
        };

        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();

        Map<String, String> result = parser.parse(file);

        assertEquals("value", result.get("key"));
    }
}
