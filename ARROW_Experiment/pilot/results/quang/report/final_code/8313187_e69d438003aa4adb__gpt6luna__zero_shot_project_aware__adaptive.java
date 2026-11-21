package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void parseRejectsNullResource() {
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();

        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
