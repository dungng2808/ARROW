package com.github.resource4j.extras.config;

import com.github.resource4j.OptionalValue;
import com.github.resource4j.ResourceObject;
import com.github.resource4j.objects.parsers.ResourceParser;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void parseConfigMap() {
        final byte[] content = "first = one\nsecond = two\n".getBytes(StandardCharsets.UTF_8);
        ResourceObject file = new ResourceObject() {
            @Override
            public String name() {
                return "test.conf";
            }

            @Override
            public <T> OptionalValue<T> parsedTo(
                    ResourceParser<T, ? extends OptionalValue<T>> parser) {
                return parser.parse(asStream(), name());
            }

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
                return "test.conf";
            }
        };
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();

        Map<String, String> result = parser.parse(file);

        assertNotNull(result);
        assertEquals("one", result.get("first"));
        assertEquals("two", result.get("second"));
        assertEquals(2, result.size());
    }
}
