package com.oracle.wls.exporter.domain;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

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
    public void readsRestPortAndDomainQualifier() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("restPort", 7001);
        yaml.put("domainQualifier", true);

        ExporterConfig config = ExporterConfig.loadConfig(yaml);

        assertEquals(Integer.valueOf(7001), config.getRestPort());
        assertTrue(config.useDomainQualifier());
        assertEquals(1, config.getEffectiveQueries().length);
        assertEquals(MBeanSelector.DOMAIN_NAME_SELECTOR, config.getEffectiveQueries()[0]);
    }

    @Test
    public void domainQualifierIsNotUsedWhenDomainNameIsPresent() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("domainQualifier", true);
        ExporterConfig config = ExporterConfig.loadConfig(yaml);

        config.resetDomainName();

        assertTrue(config.useDomainQualifier());
        assertArrayEquals(new MBeanSelector[] { MBeanSelector.DOMAIN_NAME_SELECTOR },
                config.getEffectiveQueries());
    }

    @Test
    public void resetDomainNameEnablesConfiguredQualifier() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("domainQualifier", true);
        ExporterConfig config = ExporterConfig.loadConfig(yaml);

        config.resetDomainName();

        assertTrue(config.useDomainQualifier());
    }

    @Test
    public void replaceCopiesDisplaySettingsAndResetsDomainName() {
        Map<String, Object> originalYaml = new HashMap<>();
        originalYaml.put("restPort", 7001);
        originalYaml.put("domainQualifier", true);
        ExporterConfig original = ExporterConfig.loadConfig(originalYaml);

        Map<String, Object> replacementYaml = new HashMap<>();
        replacementYaml.put("restPort", 8001);
        replacementYaml.put("domainQualifier", false);
        replacementYaml.put("metricsNameSnakeCase", true);
        ExporterConfig replacement = ExporterConfig.loadConfig(replacementYaml);

        original.replace(replacement);

        assertEquals(Integer.valueOf(8001), original.getRestPort());
        assertFalse(original.useDomainQualifier());
        assertTrue(original.getMetricsNameSnakeCase());
        assertEquals(0, original.getQueries().length);
    }

    @Test
    public void appendCombinesQueriesFromConfigurations() {
        ExporterConfig first = ExporterConfig.createEmptyConfig();
        ExporterConfig second = ExporterConfig.createEmptyConfig();

        first.append(second);

        assertEquals(0, first.getQueries().length);
    }

    @Test
    public void toStringFormatsEmptyConfiguration() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();

        assertEquals("queries:\n", config.toString());
    }

    @Test
    public void toQueryStringIsEmptyWithoutQueries() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();

        assertEquals("", config.toQueryString());
    }

    @Test
    public void resetDomainNameClearsDomainName() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();

        config.resetDomainName();

        assertNull(config.getDomainName());
    }
}
