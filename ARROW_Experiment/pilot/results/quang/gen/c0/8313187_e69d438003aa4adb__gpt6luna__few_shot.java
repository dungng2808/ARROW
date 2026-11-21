package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void parseConfigMap() {
        ResourceObject file = new ResourceObject("{\"first\":\"one\",\"second\":\"two\"}");
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();

        Map<String, String> result = parser.parse(file);

        assertNotNull(result);
        assertEquals("one", result.get("first"));
        assertEquals("two", result.get("second"));
        assertEquals(2, result.size());
    }
}
