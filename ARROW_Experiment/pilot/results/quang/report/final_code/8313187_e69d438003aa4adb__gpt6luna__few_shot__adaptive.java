package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigResourceBundleParser_ArrowTest {

    @Test
    public void parseConfigMapRejectsNullResource() {
        ConfigResourceBundleParser parser = new ConfigResourceBundleParser();

        assertThrows(NullPointerException.class, () -> parser.parse((ResourceObject) null));
    }
}
