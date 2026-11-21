package com.github.resource4j.extras.config;

import com.github.resource4j.OptionalValue;
import com.github.resource4j.ResourceObject;
import com.github.resource4j.objects.parsers.ResourceParser;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void parseConfigMap() {
        final Map<String, String> expected = new HashMap<String, String>();
        expected.put("key", "value");

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
            public <T> OptionalValue<T> parsedTo(
                    ResourceParser<T, ? extends OptionalValue<T>> parser) {
                return new OptionalValue<T>() {
                    @Override
                    public T asIs() {
                        return (T) expected;
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
        };

        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();
        Map<String, String> actual = parser.parse(file);

        assertEquals(expected, actual);
    }
}
