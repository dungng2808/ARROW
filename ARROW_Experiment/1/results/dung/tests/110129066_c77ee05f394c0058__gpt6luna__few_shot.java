package com.oracle.wls.exporter.domain;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class ExporterConfig_ArrowTest {

    @Test
    public void emptyConfigHasNoQueriesAndDefaultSettings() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();

        assertEquals(0, config.getQueries().length);
        assertEquals(0, config.getEffectiveQueries().length);
        assertNull(config.getRestPort());
        assertNull(config.getQuerySyncConfiguration());
        assertFalse(config.getMetricsNameSnakeCase());
        assertFalse(config.useDomainQualifier());
    }

    @Test
    public void nullMapCreatesEmptyConfig() {
        ExporterConfig config = ExporterConfig.loadConfig((Map<String, Object>) null);

        assertEquals(0, config.getQueries().length);
        assertEquals("queries:\n", config.toString());
    }

    @Test
    public void readsRestPortAndBooleanSettings() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("restPort", 7001);
        yaml.put("metricsNameSnakeCase", true);
        yaml.put("domainQualifier", true);

        ExporterConfig config = ExporterConfig.loadConfig(yaml);

        assertEquals(Integer.valueOf(7001), config.getRestPort());
        assertTrue(config.getMetricsNameSnakeCase());
        assertTrue(config.useDomainQualifier());
        assertEquals("metricsNameSnakeCase: true\ndomainQualifier: true\nrestPort: 7001\nqueries:\n",
                config.toString());
    }

    @Test
    public void defaultSnakeCaseSettingAppliesToNewConfigs() {
        ExporterConfig.setDefaultMetricsNameSnakeCase(true);
        try {
            ExporterConfig config = ExporterConfig.createEmptyConfig();
            assertTrue(config.getMetricsNameSnakeCase());
            assertEquals("metricsNameSnakeCase: true\nqueries:\n", config.toString());
        } finally {
            ExporterConfig.setDefaultMetricsNameSnakeCase(false);
        }
    }

    @Test
    public void nullQueriesValueIsTreatedAsEmpty() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("queries", null);

        ExporterConfig config = ExporterConfig.loadConfig(yaml);

        assertEquals(0, config.getQueries().length);
        assertEquals("queries:\n", config.toString());
    }

    @Test
    public void resetDomainNameDisablesDomainQualifier() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("domainQualifier", true);
        ExporterConfig config = ExporterConfig.loadConfig(yaml);

        config.resetDomainName();

        assertTrue(config.useDomainQualifier());
    }

    @Test
    public void replaceCopiesDisplaySettingsAndRestPort() {
        Map<String, Object> originalYaml = new HashMap<>();
        originalYaml.put("restPort", 7001);
        originalYaml.put("metricsNameSnakeCase", true);
        ExporterConfig original = ExporterConfig.loadConfig(originalYaml);

        Map<String, Object> replacementYaml = new HashMap<>();
        replacementYaml.put("restPort", 8001);
        replacementYaml.put("domainQualifier", true);
        ExporterConfig replacement = ExporterConfig.loadConfig(replacementYaml);

        original.replace(replacement);

        assertEquals(Integer.valueOf(8001), original.getRestPort());
        assertFalse(original.getMetricsNameSnakeCase());
        assertTrue(original.useDomainQualifier());
        assertEquals("domainQualifier: true\nrestPort: 8001\nqueries:\n", original.toString());
    }

    @Test
    public void appendLeavesEmptyConfigurationsUnchanged() {
        ExporterConfig first = ExporterConfig.createEmptyConfig();
        ExporterConfig second = ExporterConfig.createEmptyConfig();

        first.append(second);

        assertArrayEquals(new MBeanSelector[0], first.getQueries());
        assertEquals("queries:\n", first.toString());
    }

    @Test
    public void effectiveQueriesAreEmptyWhenDomainQualifierIsDisabled() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();

        assertArrayEquals(new MBeanSelector[0], config.getEffectiveQueries());
    }

    @Test
    public void effectiveQueriesIncludeDomainSelectorWhenQualifierIsEnabled() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("domainQualifier", true);
        ExporterConfig config = ExporterConfig.loadConfig(yaml);

        MBeanSelector[] effectiveQueries = config.getEffectiveQueries();

        assertEquals(1, effectiveQueries.length);
        assertNotNull(effectiveQueries[0]);
    }

    @Test
    public void emptyQueryListIsAccepted() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("queries", new ArrayList<Map<String, Object>>());

        ExporterConfig config = ExporterConfig.loadConfig(yaml);

        assertEquals(0, config.getQueries().length);
        assertEquals("queries:\n", config.toString());
    }
}
