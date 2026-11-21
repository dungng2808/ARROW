package com.oracle.wls.exporter.domain;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ExporterConfig_ArrowTest {

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
    public void testLoadConfigFromInputStream() {
        String yamlContent = "metricsNameSnakeCase: true\n" +
                "domainQualifier: true\n" +
                "restPort: 8080\n" +
                "queries:\n" +
                "  - mbean:\n" +
                "      type: TestMBean\n" +
                "      attributes:\n" +
                "        - name: testAttribute\n";
        InputStream inputStream = new ByteArrayInputStream(yamlContent.getBytes());

        ExporterConfig config = ExporterConfig.loadConfig(inputStream);
        assertNotNull(config);
        assertTrue(config.getMetricsNameSnakeCase());
        assertTrue(config.useDomainQualifier());
        assertEquals(Integer.valueOf(8080), config.getRestPort());
    }

    @Test
    public void testLoadConfigFromMap() {
        Map<String, Object> yamlMap = new HashMap<>();
        yamlMap.put("metricsNameSnakeCase", true);
        yamlMap.put("domainQualifier", false);
        yamlMap.put("restPort", 9090);
        yamlMap.put("queries", Collections.emptyList());

        ExporterConfig config = ExporterConfig.loadConfig(yamlMap);
        assertNotNull(config);
        assertTrue(config.getMetricsNameSnakeCase());
        assertFalse(config.useDomainQualifier());
        assertEquals(Integer.valueOf(9090), config.getRestPort());
    }

    @Test
    public void testGetEffectiveQueries() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();
        MBeanSelector[] effectiveQueries = config.getEffectiveQueries();
        assertNotNull(effectiveQueries);
        assertArrayEquals(new MBeanSelector[]{}, effectiveQueries);
    }

    @Test
    public void testGetQueries() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();
        MBeanSelector[] queries = config.getQueries();
        assertNotNull(queries);
        assertArrayEquals(new MBeanSelector[]{}, queries);
    }

    @Test
    public void testScrapeMetrics() {
        // Create a mock MBeanSelector and JsonObject for testing
        MBeanSelector selector = MBeanSelector.create(new HashMap<>());
        JsonObject response = JsonParser.parseString("{}").getAsJsonObject();

        ExporterConfig config = ExporterConfig.createEmptyConfig();
        Map<String, Object> metrics = config.scrapeMetrics(selector, response);
        assertNotNull(metrics);
    }

    @Test
    public void testAppend() {
        ExporterConfig config1 = ExporterConfig.createEmptyConfig();
        ExporterConfig config2 = ExporterConfig.createEmptyConfig();

        // Add a query to config2
        Map<String, Object> querySpec = new HashMap<>();
        querySpec.put("mbean", new HashMap<>());
        MBeanSelector selector = MBeanSelector.create(querySpec);
        config2.appendQuery(selector);

        int originalSize = config1.getQueries().length;
        config1.append(config2);
        assertEquals(originalSize + 1, config1.getQueries().length);
    }

    @Test
    public void testReplace() {
        ExporterConfig config1 = ExporterConfig.createEmptyConfig();
        ExporterConfig config2 = ExporterConfig.createEmptyConfig();

        // Set some values in config2
        config2.metricsNameSnakeCase = true;
        config2.useDomainQualifier = true;
        config2.restPort = 8080;

        // Add a query to config2
        Map<String, Object> querySpec = new HashMap<>();
        querySpec.put("mbean", new HashMap<>());
        MBeanSelector selector = MBeanSelector.create(querySpec);
        config2.appendQuery(selector);

        config1.replace(config2);
        assertTrue(config1.getMetricsNameSnakeCase());
        assertTrue(config1.useDomainQualifier());
        assertEquals(Integer.valueOf(8080), config1.getRestPort());
        assertEquals(1, config1.getQueries().length);
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
        assertTrue(result.contains("queries:"));
    }

    @Test
    public void testToQueryString() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();
        String result = config.toQueryString();
        assertNotNull(result);
    }

    @Test
    public void testSetDefaultMetricsNameSnakeCase() {
        ExporterConfig.setDefaultMetricsNameSnakeCase(true);
        ExporterConfig config = ExporterConfig.createEmptyConfig();
        assertTrue(config.getMetricsNameSnakeCase());

        ExporterConfig.setDefaultMetricsNameSnakeCase(false);
        ExporterConfig config2 = ExporterConfig.createEmptyConfig();
        assertFalse(config2.getMetricsNameSnakeCase());
    }

    @Test
    public void testUseDomainQualifier() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();
        assertFalse(config.useDomainQualifier());
    }

    @Test
    public void testGetDomainName() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();
        assertEquals(System.getProperty(ExporterConfig.DOMAIN_NAME_PROPERTY), config.getDomainName());
    }

    @Test
    public void testGetMetricsNameSnakeCase() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();
        assertFalse(config.getMetricsNameSnakeCase());
    }

    @Test
    public void testGetRestPort() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();
        assertNull(config.getRestPort());
    }

    @Test
    public void testGetQuerySyncConfiguration() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();
        assertNull(config.getQuerySyncConfiguration());
    }

    @Test
    public void testAppendQueriesWithMerge() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();
        Map<String, Object> querySpec1 = new HashMap<>();
        Map<String, Object> mbeanSpec1 = new HashMap<>();
        mbeanSpec1.put("type", "TestMBean");
        querySpec1.put("mbean", mbeanSpec1);
        MBeanSelector selector1 = MBeanSelector.create(querySpec1);

        Map<String, Object> querySpec2 = new HashMap<>();
        Map<String, Object> mbeanSpec2 = new HashMap<>();
        mbeanSpec2.put("type", "TestMBean");
        querySpec2.put("mbean", mbeanSpec2);
        MBeanSelector selector2 = MBeanSelector.create(querySpec2);

        config.appendQuery(selector1);
        config.appendQuery(selector2);

        assertEquals(1, config.getQueries().length);
    }

    @Test
    public void testAppendQueriesWithoutMerge() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();
        Map<String, Object> querySpec1 = new HashMap<>();
        Map<String, Object> mbeanSpec1 = new HashMap<>();
        mbeanSpec1.put("type", "TestMBean1");
        querySpec1.put("mbean", mbeanSpec1);
        MBeanSelector selector1 = MBeanSelector.create(querySpec1);

        Map<String, Object> querySpec2 = new HashMap<>();
        Map<String, Object> mbeanSpec2 = new HashMap<>();
        mbeanSpec2.put("type", "TestMBean2");
        querySpec2.put("mbean", mbeanSpec2);
        MBeanSelector selector2 = MBeanSelector.create(querySpec2);

        config.appendQuery(selector1);
        config.appendQuery(selector2);

        assertEquals(2, config.getQueries().length);
    }
}
