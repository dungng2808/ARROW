package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void parseConfigMap() {
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();
        ResourceObject file = new ResourceObject() {
            @Override
            public InputStream asStream() {
                return new ByteArrayInputStream(
                        "first=one\nsecond=two".getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public long lastModified() {
                return 0L;
            }

            @Override
            public long size() {
                return 0L;
            }

            @Override
            public String actualName() {
                return "test.conf";
            }

            @Override
            public String name() {
                return "test.conf";
            }

            public <T> T parsedTo(Object parser) {
                throw new UnsupportedOperationException();
            }
        };

        Map<String, String> result = parser.parse(file);

        assertEquals("one", result.get("first"));
        assertEquals("two", result.get("second"));
        assertEquals(2, result.size());
    }
}
