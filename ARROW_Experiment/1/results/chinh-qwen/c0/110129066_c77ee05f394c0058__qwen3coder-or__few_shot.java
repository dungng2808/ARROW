package com.oracle.wls.exporter.domain;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ExporterConfig_ArrowTest {

    @Before
    public void setUp() {
        ExporterConfig.setDefaultMetricsNameSnakeCase(false);
    }

    @Test
    public void testCreateEmptyConfig() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();
        assertNotNull(config);
        assertArrayEquals(new MBeanSelector[]{}, config.getQueries());
        assertNull(config.getRestPort());
        assertFalse(config.getMetricsNameSnakeCase());
        assertFalse(config.useDomainQualifier());
        assertEquals(System.getProperty(ExporterConfig.DOMAIN_NAME_PROPERTY), config.getDomainName());
    }

    @Test
    public void testLoadConfigWithNullInputStream() {
        try {
            ExporterConfig.loadConfig((InputStream) null);
            fail("Expected exception was not thrown");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testLoadConfigWithEmptyYaml() {
        InputStream inputStream = new ByteArrayInputStream("".getBytes());
        ExporterConfig config = ExporterConfig.loadConfig(inputStream);
        assertNotNull(config);
    }

    @Test
    public void testLoadConfigWithValidYaml() {
        String yamlContent = "restPort: 8080\n" +
                "metricsNameSnakeCase: true\n" +
                "domainQualifier: true\n" +
                "queries:\n" +
                "  - type: testType\n" +
                "    attributes:\n" +
                "      - attr1\n";
        InputStream inputStream = new ByteArrayInputStream(yamlContent.getBytes());
        ExporterConfig config = ExporterConfig.loadConfig(inputStream);
        assertNotNull(config);
        assertEquals(Integer.valueOf(8080), config.getRestPort());
        assertTrue(config.getMetricsNameSnakeCase());
        assertTrue(config.useDomainQualifier());
    }

    @Test
    public void testGetEffectiveQueriesWithNullQueries() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("domainQualifier", true);
        ExporterConfig config = new ExporterConfig(yaml);
        MBeanSelector[] effectiveQueries = config.getEffectiveQueries();
        assertNotNull(effectiveQueries);
        assertEquals(0, effectiveQueries.length);
    }

    @Test
    public void testGetEffectiveQueriesWithDomainQualifier() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("domainQualifier", true);
        ExporterConfig config = new ExporterConfig(yaml);
        MBeanSelector[] effectiveQueries = config.getEffectiveQueries();
        assertNotNull(effectiveQueries);
        assertEquals(1, effectiveQueries.length);
        assertEquals(MBeanSelector.DOMAIN_NAME_SELECTOR, effectiveQueries[0]);
    }

    @Test
    public void testGetEffectiveQueriesWithoutDomainQualifier() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("domainQualifier", false);
        ExporterConfig config = new ExporterConfig(yaml);
        MBeanSelector[] effectiveQueries = config.getEffectiveQueries();
        assertNotNull(effectiveQueries);
        assertEquals(0, effectiveQueries.length);
    }

    @Test
    public void testGetQueriesWithNullQueries() {
        Map<String, Object> yaml = new HashMap<>();
        ExporterConfig config = new ExporterConfig(yaml);
        MBeanSelector[] queries = config.getQueries();
        assertNotNull(queries);
        assertEquals(0, queries.length);
    }

    @Test
    public void testGetRestPort() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("restPort", 9090);
        ExporterConfig config = new ExporterConfig(yaml);
        assertEquals(Integer.valueOf(9090), config.getRestPort());
    }

    @Test
    public void testGetMetricsNameSnakeCase() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("metricsNameSnakeCase", true);
        ExporterConfig config = new ExporterConfig(yaml);
        assertTrue(config.getMetricsNameSnakeCase());
    }

    @Test
    public void testUseDomainQualifier() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("domainQualifier", true);
        ExporterConfig config = new ExporterConfig(yaml);
        assertTrue(config.useDomainQualifier());
    }

    @Test
    public void testUseDomainQualifierWithDomainNameSet() {
        Map<String, Object> yaml = new HashMap<>();
        yaml.put("domainQualifier", true);
        ExporterConfig config = new ExporterConfig(yaml);
        // Domain name is set by default, so useDomainQualifier should return false
        assertFalse(config.useDomainQualifier());
    }

    @Test
    public void testAppend() {
        ExporterConfig config1 = ExporterConfig.createEmptyConfig();
        ExporterConfig config2 = ExporterConfig.createEmptyConfig();
        config2.append(config1);
        // Should not throw exception
    }

    @Test
    public void testReplace() {
        ExporterConfig config1 = ExporterConfig.createEmptyConfig();
        ExporterConfig config2 = ExporterConfig.createEmptyConfig();
        config1.replace(config2);
        // Should not throw exception
    }

    @Test
    public void testResetDomainName() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();
        config.resetDomainName();
        assertNull(config.getDomainName());
    }

    @Test
    public void testToString() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();
        String result = config.toString();
        assertNotNull(result);
    }

    @Test
    public void testToQueryString() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();
        String result = config.toQueryString();
        assertNotNull(result);
    }
}
