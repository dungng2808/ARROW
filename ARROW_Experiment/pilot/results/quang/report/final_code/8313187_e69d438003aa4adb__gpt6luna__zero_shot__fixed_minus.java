package com.github.resource4j.extras.config;

import com.github.resource4j.OptionalValue;
import com.github.resource4j.ResourceKey;
import com.github.resource4j.ResourceObject;
import com.github.resource4j.objects.parsers.ResourceParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void parseReturnsParsedConfigMap() {
        final byte[] content = "key=value".getBytes(StandardCharsets.UTF_8);
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
            public <T> OptionalValue<T> parsedTo(ResourceParser<T, ? extends OptionalValue<T>> parser) {
                return parser.parse(new ResourceKey("config.conf"), this);
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
        };
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();

        Map<String, String> result = parser.parse(file);

        assertEquals("value", result.get("key"));
    }
}
