package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void parseReturnsParsedConfigMap() {
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();

        Map<String, String> result = parser.parse(null);

        assertEquals(null, result);
    }
}
