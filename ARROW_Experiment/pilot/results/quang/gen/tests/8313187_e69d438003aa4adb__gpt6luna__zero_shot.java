package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void parseReturnsParsedConfigMap() {
        ResourceObject file = new ResourceObject("{\"key\":\"value\"}");
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();

        Map<String, String> result = parser.parse(file);

        assertEquals("value", result.get("key"));
    }
}
