package com.alibaba.boot.nacos.actuate.endpoint;

import com.alibaba.nacos.spring.context.event.config.NacosConfigMetadataEvent;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.Before;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NacosConfigEndpoint_ArrowTest {

    private NacosConfigEndpoint endpoint;
    private ApplicationContext applicationContext;
    private ConfigurableEnvironment environment;

    @Before
    public void setUp() {
        endpoint = new NacosConfigEndpoint();
        applicationContext = mock(ApplicationContext.class);
        environment = mock(ConfigurableEnvironment.class);
        
        // Set up the application context to return our mock environment
        when(applicationContext.getEnvironment()).thenReturn(environment);
        
        // Inject the mocked application context
        endpoint.applicationContext = applicationContext;
    }

    @Test
    public void testInvokeWithNonConfigurableEnvironment() {
        // Mock environment that is not assignable to ConfigurableEnvironment
        when(environment.getClass()).thenReturn(Object.class);
        
        Map<String, Object> result = endpoint.invoke();
        
        assertNotNull(result);
        assertTrue(result.containsKey("error"));
        assertEquals("environment type not match ConfigurableEnvironment: java.lang.Object", 
                     result.get("error"));
    }

    @Test
    public void testInvokeWithConfigurableEnvironment() {
        // Mock environment that is assignable to ConfigurableEnvironment
        when(environment.getClass()).thenReturn(ConfigurableEnvironment.class);
        
        // Mock global properties bean
        Properties globalProperties = new Properties();
        globalProperties.setProperty("test.key", "test.value");
        when(applicationContext.getBean(eq("nacos.global.properties"), eq(Properties.class)))
                .thenReturn(globalProperties);
        
        Map<String, Object> result = endpoint.invoke();
        
        assertNotNull(result);
        assertTrue(result.containsKey("nacosConfigMetadata"));
        assertTrue(result.containsKey("nacosConfigGlobalProperties"));
    }

    @Test
    public void testOnApplicationEventWithAnnotationMetadata() {
        AnnotationMetadata source = mock(AnnotationMetadata.class);
        when(source.getClassName()).thenReturn("com.example.TestClass");
        
        NacosConfigMetadataEvent event = new NacosConfigMetadataEvent(this, 
            "group1", "dataId1", source, null, null, null, null);
        
        endpoint.onApplicationEvent(event);
        
        assertFalse(endpoint.nacosConfigMetadataMap.isEmpty());
        JsonNode node = endpoint.nacosConfigMetadataMap.values().iterator().next();
        assertEquals("NacosPropertySource", node.get("origin").asText());
        assertEquals("com.example.TestClass", node.get("target").asText());
    }

    @Test
    public void testOnApplicationEventWithNacosConfigListener() throws Exception {
        Method mockMethod = String.class.getMethod("toString");
        
        NacosConfigMetadataEvent event = new NacosConfigMetadataEvent(this, 
            "group1", "dataId1", mockMethod, mockMethod, null, null, null);
        
        endpoint.onApplicationEvent(event);
        
        assertFalse(endpoint.nacosConfigMetadataMap.isEmpty());
        JsonNode node = endpoint.nacosConfigMetadataMap.values().iterator().next();
        assertEquals("NacosConfigListener", node.get("origin").asText());
        assertTrue(node.get("target").asText().contains("java.lang.String"));
    }

    @Test
    public void testOnApplicationEventWithNacosConfigurationProperties() {
        Class<?> beanType = String.class;
        
        NacosConfigMetadataEvent event = new NacosConfigMetadataEvent(this, 
            "group1", "dataId1", null, null, beanType, null, null);
        
        endpoint.onApplicationEvent(event);
        
        assertFalse(endpoint.nacosConfigMetadataMap.isEmpty());
        JsonNode node = endpoint.nacosConfigMetadataMap.values().iterator().next();
        assertEquals("NacosConfigurationProperties", node.get("origin").asText());
        assertEquals("java.lang.String", node.get("target").asText());
    }

    @Test
    public void testBuildMetadataKeyWithXmlResource() {
        String xmlResource = "test.xml";
        NacosConfigMetadataEvent event = new NacosConfigMetadataEvent(this, 
            "group1", "dataId1", null, null, null, xmlResource, null);
        
        String key = endpoint.buildMetadataKey(event);
        assertEquals("group1:dataId1:test.xml", key);
    }

    @Test
    public void testBuildMetadataKeyWithoutXmlResource() {
        Class<?> beanType = String.class;
        Object annotatedElement = "test";
        
        NacosConfigMetadataEvent event = new NacosConfigMetadataEvent(this, 
            "group1", "dataId1", null, annotatedElement, beanType, null, null);
        
        String key = endpoint.buildMetadataKey(event);
        assertTrue(key.contains("group1:dataId1:java.lang.String:test"));
    }

    @Test
    public void testBuildMetadataKeyWithNullElements() {
        NacosConfigMetadataEvent event = new NacosConfigMetadataEvent(this, 
            "group1", "dataId1", null, null, null, null, null);
        
        String key = endpoint.buildMetadataKey(event);
        assertEquals("", key);
    }
}
