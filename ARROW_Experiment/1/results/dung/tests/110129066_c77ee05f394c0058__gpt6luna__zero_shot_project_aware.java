package com.oracle.wls.exporter.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class ExporterConfig_ArrowTest {

    @Test
    public void emptyConfigHasNoQueriesAndNoRestPort() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();

        assertEquals(0, config.getQueries().length);
        assertEquals(0, config.getEffectiveQueries().length);
        assertNull(config.getRestPort());
    }

    @Test
    public void nullYamlCreatesEmptyConfig() {
        ExporterConfig config = ExporterConfig.loadConfig((Map<String, Object>) null);

        assertEquals(0, config.getQueries().length);
        assertNull(config.getRestPort());
    }

    @Test
    public void readsRestPort() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("restPort", 7001);

        ExporterConfig config = ExporterConfig.loadConfig(yaml);

        assertEquals(Integer.valueOf(7001), config.getRestPort());
    }

    @Test
    public void domainQualifierAddsDomainNameQueryWhenDomainIsUnknown() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("domainQualifier", true);

        ExporterConfig config = ExporterConfig.loadConfig(yaml);

        assertTrue(config.useDomainQualifier());
        assertEquals(1, config.getEffectiveQueries().length);
        assertEquals(MBeanSelector.DOMAIN_NAME_SELECTOR, config.getEffectiveQueries()[0]);
    }

    @Test
    public void domainQualifierIsNotUsedWhenDomainNameIsKnown() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("domainQualifier", true);

        ExporterConfig config = ExporterConfig.loadConfig(yaml);
        config.resetDomainName();

        assertTrue(config.useDomainQualifier());
        assertEquals(1, config.getEffectiveQueries().length);
    }

    @Test
    public void resetDomainNameClearsDomainName() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();
        config.resetDomainName();

        assertNull(config.getDomainName());
    }

    @Test
    public void replaceCopiesDisplaySettingsAndRestPort() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("metricsNameSnakeCase", true);
        yaml.put("domainQualifier", true);
        yaml.put("restPort", 7001);

        ExporterConfig source = ExporterConfig.loadConfig(yaml);
        ExporterConfig target = ExporterConfig.createEmptyConfig();
        target.replace(source);

        assertTrue(target.getMetricsNameSnakeCase());
        assertTrue(target.useDomainQualifier());
        assertEquals(Integer.valueOf(7001), target.getRestPort());
        assertEquals("metricsNameSnakeCase: true\ndomainQualifier: true\nrestPort: 7001\nqueries:\n", target.toString());
    }

    @Test
    public void replaceWithEmptyConfigClearsSettings() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("restPort", 7001);
        yaml.put("metricsNameSnakeCase", true);

        ExporterConfig target = ExporterConfig.loadConfig(yaml);
        target.replace(ExporterConfig.createEmptyConfig());

        assertNull(target.getRestPort());
        assertFalse(target.getMetricsNameSnakeCase());
    }

    @Test
    public void appendEmptyConfigLeavesQueriesUnchanged() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();
        config.append(ExporterConfig.createEmptyConfig());

        assertEquals(0, config.getQueries().length);
        assertEquals("queries:\n", config.toString());
    }

    @Test
    public void emptyQueriesListIsAccepted() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("queries", Collections.emptyList());

        ExporterConfig config = ExporterConfig.loadConfig(yaml);

        assertNotNull(config.getQueries());
        assertEquals(0, config.getQueries().length);
    }

    @Test
    public void defaultSnakeCaseSettingCanBeEnabled() {
        ExporterConfig.setDefaultMetricsNameSnakeCase(true);
        try {
            ExporterConfig config = ExporterConfig.createEmptyConfig();
            assertTrue(config.getMetricsNameSnakeCase());
        } finally {
            ExporterConfig.setDefaultMetricsNameSnakeCase(false);
        }
    }

    @Test
    public void toStringIncludesQueriesHeaderForEmptyConfig() {
        assertEquals("queries:\n", ExporterConfig.createEmptyConfig().toString());
    }
}
