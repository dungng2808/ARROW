package com.alibaba.boot.nacos.actuate.endpoint;

import com.alibaba.boot.nacos.config.NacosConfigConstants;
import com.alibaba.nacos.spring.context.event.config.NacosConfigMetadataEvent;
import com.alibaba.nacos.spring.util.NacosUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.ClassUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.CONFIG_GLOBAL_NACOS_PROPERTIES_BEAN_NAME;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NacosConfigEndpoint_ArrowTest {

    private NacosConfigEndpoint endpoint;

    @Before
    public void setUp() {
        endpoint = new NacosConfigEndpoint();
    }

    @Test
    public void testInvoke_EnvironmentNotConfigurable() {
        ApplicationContext mockContext = mock(ApplicationContext.class);
        ConfigurableEnvironment mockEnv = mock(ConfigurableEnvironment.class);
        when(mockContext.getEnvironment()).thenReturn(mockEnv);
        when(ClassUtils.isAssignable(any(Class.class), any(Class.class))).thenReturn(false);

        endpoint.applicationContext = mockContext;

        Map<String, Object> result = endpoint.invoke();

        assertNotNull(result);
        assertTrue(result.containsKey("error"));
        assertEquals("environment type not match ConfigurableEnvironment: " + mockEnv.getClass().getName(), 
                     result.get("error"));
    }

    @Test
    public void testInvoke_EnvironmentIsConfigurable() throws Exception {
        ApplicationContext mockContext = mock(ApplicationContext.class);
        ConfigurableEnvironment mockEnv = mock(ConfigurableEnvironment.class);
        Properties mockProps = new Properties();
        mockProps.setProperty("test", "value");

        when(mockContext.getEnvironment()).thenReturn(mockEnv);
        when(ClassUtils.isAssignable(any(Class.class), any(Class.class))).thenReturn(true);
        when(mockContext.getBean(CONFIG_GLOBAL_NACOS_PROPERTIES_BEAN_NAME, Properties.class)).thenReturn(mockProps);

        endpoint.applicationContext = mockContext;

        Map<String, Object> result = endpoint.invoke();

        assertNotNull(result);
        assertTrue(result.containsKey("nacosConfigMetadata"));
        assertTrue(result.containsKey("nacosConfigGlobalProperties"));
        assertEquals(mockProps, result.get("nacosConfigGlobalProperties"));
    }

    @Test
    public void testOnApplicationEvent_WithXmlResource() {
        NacosConfigMetadataEvent event = new NacosConfigMetadataEvent(this, "group", "dataId", "xmlResource");
        event.setXmlResource("resource.xml");

        endpoint.onApplicationEvent(event);

        assertEquals(1, endpoint.nacosConfigMetadataMap.size());
        assertTrue(endpoint.nacosConfigMetadataMap.containsKey("group" + NacosUtils.SEPARATOR + "dataId" + NacosUtils.SEPARATOR + "resource.xml"));
    }

    @Test
    public void testOnApplicationEvent_WithBeanTypeAndAnnotatedElement() {
        NacosConfigMetadataEvent event = new NacosConfigMetadataEvent(this, "group", "dataId", "beanType");
        event.setBeanType(String.class);
        event.setAnnotatedElement("annotatedElement");

        endpoint.onApplicationEvent(event);

        assertEquals(1, endpoint.nacosConfigMetadataMap.size());
        assertTrue(endpoint.nacosConfigMetadataMap.containsKey("group" + NacosUtils.SEPARATOR + "dataId" + NacosUtils.SEPARATOR + String.class + NacosUtils.SEPARATOR + "annotatedElement"));
    }

    @Test
    public void testOnApplicationEvent_WithAnnotationMetadata() {
        AnnotationMetadata mockMetadata = mock(AnnotationMetadata.class);
        when(mockMetadata.getClassName()).thenReturn("com.example.TestClass");

        NacosConfigMetadataEvent event = new NacosConfigMetadataEvent(this, "group", "dataId", mockMetadata);
        event.setSource(mockMetadata);

        endpoint.onApplicationEvent(event);

        assertEquals(1, endpoint.nacosConfigMetadataMap.size());
        JsonNode node = endpoint.nacosConfigMetadataMap.values().iterator().next();
        assertEquals("NacosPropertySource", node.get("origin").asText());
        assertEquals("com.example.TestClass", node.get("target").asText());
    }

    @Test
    public void testOnApplicationEvent_WithNacosConfigListener() throws NoSuchMethodException {
        Method mockMethod = String.class.getMethod("toString");
        NacosConfigMetadataEvent event = new NacosConfigMetadataEvent(this, "group", "dataId", mockMethod);
        event.setSource(mockMethod);

        endpoint.onApplicationEvent(event);

        assertEquals(1, endpoint.nacosConfigMetadataMap.size());
        JsonNode node = endpoint.nacosConfigMetadataMap.values().iterator().next();
        assertEquals("NacosConfigListener", node.get("origin").asText());
        assertTrue(node.get("target").asText().contains("java.lang.String:"));
    }

    @Test
    public void testOnApplicationEvent_WithNacosConfigurationProperties() {
        NacosConfigMetadataEvent event = new NacosConfigMetadataEvent(this, "group", "dataId", "beanType");
        event.setSource(new Object() {});
        event.setBeanType(String.class);

        endpoint.onApplicationEvent(event);

        assertEquals(1, endpoint.nacosConfigMetadataMap.size());
        JsonNode node = endpoint.nacosConfigMetadataMap.values().iterator().next();
        assertEquals("NacosConfigurationProperties", node.get("origin").asText());
        assertEquals("java.lang.String", node.get("target").asText());
    }

    @Test
    public void testOnApplicationEvent_WithElement() {
        Element mockElement = mock(Element.class);
        when(mockElement.toString()).thenReturn("<element>");

        NacosConfigMetadataEvent event = new NacosConfigMetadataEvent(this, "group", "dataId", mockElement);
        event.setSource(mockElement);
        event.setXmlResource("resource.xml");

        endpoint.onApplicationEvent(event);

        assertEquals(1, endpoint.nacosConfigMetadataMap.size());
        JsonNode node = endpoint.nacosConfigMetadataMap.values().iterator().next();
        assertEquals("NacosPropertySource", node.get("origin").asText());
        assertEquals("resource.xml", node.get("target").asText());
    }

    @Test(expected = RuntimeException.class)
    public void testOnApplicationEvent_UnknownSourceType() {
        NacosConfigMetadataEvent event = new NacosConfigMetadataEvent(this, "group", "dataId", "unknown");
        event.setSource(new Object());

        endpoint.onApplicationEvent(event);
    }

    @Test
    public void testBuildMetadataKey_WithXmlResource() {
        NacosConfigMetadataEvent event = new NacosConfigMetadataEvent(this, "group", "dataId", "xmlResource");
        event.setXmlResource("resource.xml");

        String key = endpoint.buildMetadataKey(event);

        assertEquals("group" + NacosUtils.SEPARATOR + "dataId" + NacosUtils.SEPARATOR + "resource.xml", key);
    }

    @Test
    public void testBuildMetadataKey_WithoutXmlResource_BeanTypeNull() {
        NacosConfigMetadataEvent event = new NacosConfigMetadataEvent(this, "group", "dataId", "beanType");
        event.setBeanType(null);
        event.setAnnotatedElement(null);

        String key = endpoint.buildMetadataKey(event);

        assertEquals("", key);
    }

    @Test
    public void testBuildMetadataKey_WithoutXmlResource_BeanTypeNotNull() {
        NacosConfigMetadataEvent event = new NacosConfigMetadataEvent(this, "group", "dataId", "beanType");
        event.setBeanType(String.class);
        event.setAnnotatedElement("annotatedElement");

        String key = endpoint.buildMetadataKey(event);

        assertEquals("group" + NacosUtils.SEPARATOR + "dataId" + NacosUtils.SEPARATOR + String.class + NacosUtils.SEPARATOR + "annotatedElement", key);
    }
}
