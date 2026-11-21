package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void parseReturnsMapForConfigResource() {
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();
        ResourceObject resource = new ResourceObject("key = value");

        Map<String, String> result = parser.parse(resource);

        assertNotNull(result);
    }
}
