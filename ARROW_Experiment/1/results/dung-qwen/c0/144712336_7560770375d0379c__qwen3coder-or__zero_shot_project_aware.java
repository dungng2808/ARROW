package com.alibaba.boot.nacos.actuate.endpoint;

import com.alibaba.boot.nacos.common.PropertiesUtils;
import com.alibaba.boot.nacos.config.NacosConfigConstants;
import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.spring.context.event.config.NacosConfigMetadataEvent;
import com.alibaba.nacos.spring.util.NacosUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.ServletContext;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {NacosConfigEndpoint.class, ServerProperties.class})
public class NacosConfigEndpoint_ArrowTest {

    @Autowired
    private NacosConfigEndpoint endpoint;

    @Mock
    private ApplicationContext mockApplicationContext;

    @Mock
    private ConfigurableEnvironment mockEnvironment;

    @Mock
    private AnnotationMetadata mockAnnotationMetadata;

    @Mock
    private NacosConfigListener mockNacosConfigListener;

    @Mock
    private NacosConfigurationProperties mockNacosConfigurationProperties;

    @Mock
    private Method mockMethod;

    @Mock
    private ServletContext mockServletContext;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        // Inject the mocked ApplicationContext into the endpoint
        try {
            java.lang.reflect.Field field = NacosConfigEndpoint.class.getDeclaredField("applicationContext");
            field.setAccessible(true);
            field.set(endpoint, mockApplicationContext);
        } catch (Exception e) {
            fail("Failed to inject mock ApplicationContext: " + e.getMessage());
        }
    }

    @Test
    public void testInvoke_EnvironmentNotConfigurable() {
        when(mockApplicationContext.getEnvironment()).thenReturn(mockEnvironment);
        when(mockEnvironment.getClass()).thenReturn(Object.class);
        when(ClassUtils.isAssignable(any(Class.class), eq(ConfigurableEnvironment.class))).thenReturn(false);

        Map<String, Object> result = endpoint.invoke();

        assertNotNull(result);
        assertTrue(result.containsKey("error"));
        assertEquals("environment type not match ConfigurableEnvironment: " + Object.class.getName(), 
                     result.get("error"));
    }

    @Test
    public void testInvoke_EnvironmentIsConfigurable() throws Exception {
        Properties globalProperties = new Properties();
        globalProperties.setProperty("test.key", "test.value");

        when(mockApplicationContext.getEnvironment()).thenReturn(mockEnvironment);
        when(mockEnvironment.getClass()).thenReturn(ConfigurableEnvironment.class);
        when(ClassUtils.isAssignable(any(Class.class), eq(ConfigurableEnvironment.class))).thenReturn(true);
        when(mockApplicationContext.getBean(eq(NacosConfigConstants.CONFIG_GLOBAL_NACOS_PROPERTIES_BEAN_NAME), 
                                           eq(Properties.class))).thenReturn(globalProperties);
        when(PropertiesUtils.extractSafeProperties(any(Properties.class))).thenReturn(globalProperties);

        Map<String, Object> result = endpoint.invoke();

        assertNotNull(result);
        assertTrue(result.containsKey("nacosConfigMetadata"));
        assertTrue(result.containsKey("nacosConfigGlobalProperties"));
        assertEquals(globalProperties, result.get("nacosConfigGlobalProperties"));
    }

    @Test
    public void testOnApplicationEvent_AnnotationMetadataOrigin() {
        NacosConfigMetadataEvent event = mock(NacosConfigMetadataEvent.class);
        when(event.getGroupId()).thenReturn("group1");
        when(event.getDataId()).thenReturn("data1");
        when(event.getSource()).thenReturn(mockAnnotationMetadata);
        when(event.getAnnotatedElement()).thenReturn(null);
        when(event.getBeanType()).thenReturn(null);
        when(event.getXmlResource()).thenReturn(null);
        when(ClassUtils.isAssignable(any(Class.class), eq(AnnotationMetadata.class))).thenReturn(true);
        when(mockAnnotationMetadata.getClassName()).thenReturn("com.example.TestClass");

        endpoint.onApplicationEvent(event);

        assertEquals(1, endpoint.nacosConfigMetadataMap.size());
        JsonNode node = endpoint.nacosConfigMetadataMap.values().iterator().next();
        assertEquals("NacosPropertySource", node.get("origin").asText());
        assertEquals("com.example.TestClass", node.get("target").asText());
    }

    @Test
    public void testOnApplicationEvent_NacosConfigListenerOrigin() {
        NacosConfigMetadataEvent event = mock(NacosConfigMetadataEvent.class);
        when(event.getGroupId()).thenReturn("group1");
        when(event.getDataId()).thenReturn("data1");
        when(event.getSource()).thenReturn(mockNacosConfigListener);
        when(event.getAnnotatedElement()).thenReturn(mockMethod);
        when(event.getBeanType()).thenReturn(null);
        when(event.getXmlResource()).thenReturn(null);
        when(ClassUtils.isAssignable(any(Class.class), eq(NacosConfigListener.class))).thenReturn(true);
        when(mockMethod.getDeclaringClass()).thenReturn(String.class);
        when(mockMethod.toString()).thenReturn("toString()");

        endpoint.onApplicationEvent(event);

        assertEquals(1, endpoint.nacosConfigMetadataMap.size());
        JsonNode node = endpoint.nacosConfigMetadataMap.values().iterator().next();
        assertEquals("NacosConfigListener", node.get("origin").asText());
        assertEquals("java.lang.String:toString()", node.get("target").asText());
    }

    @Test
    public void testOnApplicationEvent_NacosConfigurationPropertiesOrigin() {
        NacosConfigMetadataEvent event = mock(NacosConfigMetadataEvent.class);
        when(event.getGroupId()).thenReturn("group1");
        when(event.getDataId()).thenReturn("data1");
        when(event.getSource()).thenReturn(mockNacosConfigurationProperties);
        when(event.getAnnotatedElement()).thenReturn(null);
        when(event.getBeanType()).thenReturn(String.class);
        when(event.getXmlResource()).thenReturn(null);
        when(ClassUtils.isAssignable(any(Class.class), eq(NacosConfigurationProperties.class))).thenReturn(true);

        endpoint.onApplicationEvent(event);

        assertEquals(1, endpoint.nacosConfigMetadataMap.size());
        JsonNode node = endpoint.nacosConfigMetadataMap.values().iterator().next();
        assertEquals("NacosConfigurationProperties", node.get("origin").asText());
        assertEquals("java.lang.String", node.get("target").asText());
    }

    @Test
    public void testOnApplicationEvent_XmlResourceOrigin() {
        NacosConfigMetadataEvent event = mock(NacosConfigMetadataEvent.class);
        when(event.getGroupId()).thenReturn("group1");
        when(event.getDataId()).thenReturn("data1");
        when(event.getSource()).thenReturn(mockServletContext);
        when(event.getAnnotatedElement()).thenReturn(null);
        when(event.getBeanType()).thenReturn(null);
        when(event.getXmlResource()).thenReturn("xmlResource");
        when(ClassUtils.isAssignable(any(Class.class), eq(org.w3c.dom.Element.class))).thenReturn(true);

        endpoint.onApplicationEvent(event);

        assertEquals(1, endpoint.nacosConfigMetadataMap.size());
        JsonNode node = endpoint.nacosConfigMetadataMap.values().iterator().next();
        assertEquals("NacosPropertySource", node.get("origin").asText());
        assertEquals("xmlResource", node.get("target").asText());
    }

    @Test(expected = RuntimeException.class)
    public void testOnApplicationEvent_UnknownOrigin() {
        NacosConfigMetadataEvent event = mock(NacosConfigMetadataEvent.class);
        when(event.getGroupId()).thenReturn("group1");
        when(event.getDataId()).thenReturn("data1");
        when(event.getSource()).thenReturn(new Object());
        when(event.getAnnotatedElement()).thenReturn(null);
        when(event.getBeanType()).thenReturn(null);
        when(event.getXmlResource()).thenReturn(null);
        when(ClassUtils.isAssignable(any(Class.class), any(Class.class))).thenReturn(false);

        endpoint.onApplicationEvent(event);
    }

    @Test
    public void testBuildMetadataKey_WithXmlResource() {
        NacosConfigMetadataEvent event = mock(NacosConfigMetadataEvent.class);
        when(event.getGroupId()).thenReturn("group1");
        when(event.getDataId()).thenReturn("data1");
        when(event.getXmlResource()).thenReturn("xmlResource");

        String key = endpoint.buildMetadataKey(event);

        assertEquals("group1" + NacosUtils.SEPARATOR + "data1" + NacosUtils.SEPARATOR + "xmlResource", key);
    }

    @Test
    public void testBuildMetadataKey_WithoutXmlResource_BeanTypeAndAnnotatedElement() {
        NacosConfigMetadataEvent event = mock(NacosConfigMetadataEvent.class);
        when(event.getGroupId()).thenReturn("group1");
        when(event.getDataId()).thenReturn("data1");
        when(event.getXmlResource()).thenReturn(null);
        when(event.getBeanType()).thenReturn(String.class);
        when(event.getAnnotatedElement()).thenReturn("annotatedElement");

        String key = endpoint.buildMetadataKey(event);

        assertEquals("group1" + NacosUtils.SEPARATOR + "data1" + NacosUtils.SEPARATOR + 
                     "java.lang.String" + NacosUtils.SEPARATOR + "annotatedElement", key);
    }

    @Test
    public void testBuildMetadataKey_WithoutXmlResource_NullBeanTypeAndAnnotatedElement() {
        NacosConfigMetadataEvent event = mock(NacosConfigMetadataEvent.class);
        when(event.getGroupId()).thenReturn("group1");
        when(event.getDataId()).thenReturn("data1");
        when(event.getXmlResource()).thenReturn(null);
        when(event.getBeanType()).thenReturn(null);
        when(event.getAnnotatedElement()).thenReturn(null);

        String key = endpoint.buildMetadataKey(event);

        assertEquals(StringUtils.EMPTY, key);
    }
}
