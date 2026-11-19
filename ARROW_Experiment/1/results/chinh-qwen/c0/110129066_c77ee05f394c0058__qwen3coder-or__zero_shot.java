package com.oracle.wls.exporter.domain;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public class ExporterConfig_ArrowTest {

    @Test
    public void testCreateEmptyConfig() {
        ExporterConfig config = ExporterConfig.createEmptyConfig();
        assertNotNull(config);
        assertArrayEquals(new MBeanSelector[]{}, config.getQueries());
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
    public void testLoadConfigWithValidYaml() {
        String yamlContent = "domainQualifier: true\nmetricsNameSnakeCase: false\nrestPort: 8080\nqueries:\n  - mbean:\n      type: TestType\n      attributes:\n        - name: testAttr\n          alias: testAlias";
        InputStream inputStream = new ByteArrayInputStream(yamlContent.getBytes());

        ExporterConfig config = ExporterConfig.loadConfig(inputStream);
        assertNotNull(config);
        assertEquals(Integer.valueOf(8080), config.getRestPort());
        assertTrue(config.useDomainQualifier());
        assertFalse(config.getMetricsNameSnakeCase());
    }

    @Test
    public void testLoadConfigWithEmptyYaml() {
        Map<String, Object> yamlMap = new HashMap<>();
        ExporterConfig config = ExporterConfig.loadConfig(yamlMap);
        assertNotNull(config);
        assertNull(config.getRestPort());
        assertFalse(config.useDomainQualifier());
        assertFalse(config.getMetricsNameSnakeCase());
    }

    @Test
    public void testGetEffectiveQueriesWithNoQueries() {
        Map<String, Object> yamlMap = new HashMap<>();
        ExporterConfig config = new ExporterConfig(yamlMap);
        MBeanSelector[] effectiveQueries = config.getEffectiveQueries();
        assertNotNull(effectiveQueries);
        assertArrayEquals(new MBeanSelector[]{}, effectiveQueries);
    }

    @Test
    public void testGetEffectiveQueriesWithDomainQualifier() {
        Map<String, Object> yamlMap = new HashMap<>();
        yamlMap.put("domainQualifier", true);
        ExporterConfig config = new ExporterConfig(yamlMap);
        MBeanSelector[] effectiveQueries = config.getEffectiveQueries();
        assertNotNull(effectiveQueries);
        assertEquals(1, effectiveQueries.length);
        assertEquals(MBeanSelector.DOMAIN_NAME_SELECTOR, effectiveQueries[0]);
    }

    @Test
    public void testGetQueriesReturnsCopy() {
        Map<String, Object> yamlMap = new HashMap<>();
        yamlMap.put("queries", Collections.singletonList(Collections.singletonMap("mbean", 
            Collections.singletonMap("type", "TestType"))));
        ExporterConfig config = new ExporterConfig(yamlMap);
        MBeanSelector[] queries = config.getQueries();
        assertNotNull(queries);
        assertEquals(1, queries.length);
    }

    @Test
    public void testGetMetricsNameSnakeCase() {
        Map<String, Object> yamlMap = new HashMap<>();
        yamlMap.put("metricsNameSnakeCase", true);
        ExporterConfig config = new ExporterConfig(yamlMap);
        assertTrue(config.getMetricsNameSnakeCase());
    }

    @Test
    public void testUseDomainQualifier() {
        Map<String, Object> yamlMap = new HashMap<>();
        yamlMap.put("domainQualifier", true);
        ExporterConfig config = new ExporterConfig(yamlMap);
        assertTrue(config.useDomainQualifier());
    }

    @Test
    public void testGetRestPort() {
        Map<String, Object> yamlMap = new HashMap<>();
        yamlMap.put("restPort", 9090);
        ExporterConfig config = new ExporterConfig(yamlMap);
        assertEquals(Integer.valueOf(9090), config.getRestPort());
    }

    @Test
    public void testAppend() {
        Map<String, Object> yamlMap1 = new HashMap<>();
        yamlMap1.put("queries", Collections.singletonList(Collections.singletonMap("mbean", 
            Collections.singletonMap("type", "TestType1"))));
        ExporterConfig config1 = new ExporterConfig(yamlMap1);

        Map<String, Object> yamlMap2 = new HashMap<>();
        yamlMap2.put("queries", Collections.singletonList(Collections.singletonMap("mbean", 
            Collections.singletonMap("type", "TestType2"))));
        ExporterConfig config2 = new ExporterConfig(yamlMap2);

        config1.append(config2);
        MBeanSelector[] queries = config1.getQueries();
        assertEquals(2, queries.length);
    }

    @Test
    public void testReplace() {
        Map<String, Object> yamlMap1 = new HashMap<>();
        yamlMap1.put("metricsNameSnakeCase", true);
        yamlMap1.put("domainQualifier", true);
        yamlMap1.put("restPort", 8080);
        ExporterConfig config1 = new ExporterConfig(yamlMap1);

        Map<String, Object> yamlMap2 = new HashMap<>();
        yamlMap2.put("metricsNameSnakeCase", false);
        yamlMap2.put("domainQualifier", false);
        yamlMap2.put("restPort", 9090);
        yamlMap2.put("queries", Collections.singletonList(Collections.singletonMap("mbean", 
            Collections.singletonMap("type", "TestType"))));
        ExporterConfig config2 = new ExporterConfig(yamlMap2);

        config1.replace(config2);
        assertFalse(config1.getMetricsNameSnakeCase());
        assertFalse(config1.useDomainQualifier());
        assertEquals(Integer.valueOf(9090), config1.getRestPort());
        assertEquals(1, config1.getQueries().length);
    }

    @Test
    public void testResetDomainName() {
        Map<String, Object> yamlMap = new HashMap<>();
        yamlMap.put("domainQualifier", true);
        ExporterConfig config = new ExporterConfig(yamlMap);
        config.resetDomainName();
        assertNull(config.getDomainName());
    }

    @Test
    public void testToString() {
        Map<String, Object> yamlMap = new HashMap<>();
        yamlMap.put("metricsNameSnakeCase", true);
        yamlMap.put("domainQualifier", true);
        yamlMap.put("restPort", 8080);
        ExporterConfig config = new ExporterConfig(yamlMap);
        String result = config.toString();
        assertNotNull(result);
        assertTrue(result.contains("metricsNameSnakeCase: true"));
        assertTrue(result.contains("domainQualifier: true"));
        assertTrue(result.contains("restPort: 8080"));
    }

    @Test
    public void testToQueryString() {
        Map<String, Object> yamlMap = new HashMap<>();
        yamlMap.put("queries", Collections.singletonList(Collections.singletonMap("mbean", 
            Collections.singletonMap("type", "TestType"))));
        ExporterConfig config = new ExporterConfig(yamlMap);
        String result = config.toQueryString();
        assertNotNull(result);
        assertTrue(result.contains("- mbean:"));
    }

    @Test
    public void testSetDefaultMetricsNameSnakeCase() {
        ExporterConfig.setDefaultMetricsNameSnakeCase(true);
        Map<String, Object> yamlMap = new HashMap<>();
        ExporterConfig config = new ExporterConfig(yamlMap);
        assertTrue(config.getMetricsNameSnakeCase());
    }
}
