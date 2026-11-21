package com.github.resource4j.extras.config;

import com.github.resource4j.OptionalValue;
import com.github.resource4j.ResourceObject;
import com.github.resource4j.objects.parsers.ResourceParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void parseReturnsMapForConfigResource() {
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();
        ResourceObject resource = new ResourceObject() {
            @Override
            public String name() {
                return "config.conf";
            }

            @Override
            public String actualName() {
                return "config.conf";
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
            public long size() {
                return 0L;
            }

            @Override
            public <T> OptionalValue<T> parsedTo(ResourceParser<T, ? extends OptionalValue<T>> parser) {
                return new OptionalValue<T>() {
                    @Override
                    public T asIs() {
                        return (T) Collections.singletonMap("key", "value");
                    }

                    @Override
                    public boolean notNull() {
                        return true;
                    }

                    @Override
                    public boolean isNull() {
                        return false;
                    }
                };
            }
        };

        Map<String, String> result = parser.parse(resource);

        assertEquals("value", result.get("key"));
    }
}
