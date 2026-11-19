package com.alibaba.boot.nacos.actuate.endpoint;

import com.alibaba.boot.nacos.common.PropertiesUtils;
import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.spring.context.event.config.NacosConfigMetadataEvent;
import com.alibaba.nacos.spring.util.NacosUtils;
import com.fasterxml.jackson.databind.JsonNode;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotationMetadata;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.CONFIG_GLOBAL_NACOS_PROPERTIES_BEAN_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NacosConfigEndpoint_ArrowTest {

    @Test
    public void invokeReturnsErrorForNonConfigurableEnvironment() {
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getEnvironment()).thenReturn(new org.springframework.core.env.StandardEnvironment());

        NacosConfigEndpoint endpoint = new NacosConfigEndpoint();
        setApplicationContext(endpoint, context);

        Map<String, Object> result = endpoint.invoke();

        assertEquals(1, result.size());
        assertEquals("environment type not match ConfigurableEnvironment: "
                + org.springframework.core.env.StandardEnvironment.class.getName(), result.get("error"));
    }

    @Test
    public void invokeReturnsMetadataAndSafeProperties() {
        ApplicationContext context = mock(ApplicationContext.class);
        ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);
        Properties properties = new Properties();
        properties.setProperty("plain", "value");
        when(context.getEnvironment()).thenReturn(environment);
        when(context.getBean(CONFIG_GLOBAL_NACOS_PROPERTIES_BEAN_NAME, Properties.class))
                .thenReturn(properties);

        NacosConfigEndpoint endpoint = new NacosConfigEndpoint();
        setApplicationContext(endpoint, context);

        Map<String, Object> result = endpoint.invoke();

        assertTrue(result.containsKey("nacosConfigMetadata"));
        assertTrue(result.get("nacosConfigMetadata") instanceof java.util.Collection);
        assertEquals(PropertiesUtils.extractSafeProperties(properties),
                result.get("nacosConfigGlobalProperties"));
    }

    @Test
    public void metadataEventFromAnnotationMetadataIsRecordedOnce() {
        NacosConfigEndpoint endpoint = new NacosConfigEndpoint();
        AnnotationMetadata source = mock(AnnotationMetadata.class);
        when(source.getClassName()).thenReturn("example.Config");

        NacosConfigMetadataEvent event = mock(NacosConfigMetadataEvent.class);
        when(event.getGroupId()).thenReturn("group");
        when(event.getDataId()).thenReturn("data");
        when(event.getSource()).thenReturn(source);
        when(event.getXmlResource()).thenReturn(null);
        when(event.getBeanType()).thenReturn(null);
        when(event.getAnnotatedElement()).thenReturn(null);

        endpoint.onApplicationEvent(event);
        endpoint.onApplicationEvent(event);

        Map<String, Object> result = endpoint.invoke();
        Object metadata = result.get("nacosConfigMetadata");
        assertNotNull(metadata);
        assertEquals(1, ((java.util.Collection<?>) metadata).size());
        JsonNode node = ((JsonNode) ((java.util.Collection<?>) metadata).iterator().next());
        assertEquals("group", node.get("groupId").asText());
        assertEquals("data", node.get("dataId").asText());
        assertEquals("NacosPropertySource", node.get("origin").asText());
        assertEquals("example.Config", node.get("target").asText());
    }

    @Test
    public void metadataEventWithNoKeyIsIgnored() {
        NacosConfigEndpoint endpoint = new NacosConfigEndpoint();
        NacosConfigMetadataEvent event = mock(NacosConfigMetadataEvent.class);
        when(event.getXmlResource()).thenReturn(null);
        when(event.getBeanType()).thenReturn(null);
        when(event.getAnnotatedElement()).thenReturn(null);

        endpoint.onApplicationEvent(event);

        assertTrue(endpoint.invoke().isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void metadataEventWithUnknownSourceThrows() {
        NacosConfigEndpoint endpoint = new NacosConfigEndpoint();
        NacosConfigMetadataEvent event = mock(NacosConfigMetadataEvent.class);
        when(event.getGroupId()).thenReturn("group");
        when(event.getDataId()).thenReturn("data");
        when(event.getXmlResource()).thenReturn(null);
        when(event.getBeanType()).thenReturn(String.class);
        when(event.getAnnotatedElement()).thenReturn("element");
        when(event.getSource()).thenReturn(new Object());

        endpoint.onApplicationEvent(event);
    }

    private static void setApplicationContext(NacosConfigEndpoint endpoint, ApplicationContext context) {
        try {
            java.lang.reflect.Field field = NacosConfigEndpoint.class.getDeclaredField("applicationContext");
            field.setAccessible(true);
            field.set(endpoint, context);
        }
        catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }
}
