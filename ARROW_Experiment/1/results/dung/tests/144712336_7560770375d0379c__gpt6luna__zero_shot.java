package com.alibaba.boot.nacos.actuate.endpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.alibaba.nacos.spring.context.event.config.NacosConfigMetadataEvent;

public class NacosConfigEndpoint_ArrowTest {

	@Test
	public void invokeWithoutApplicationContextReturnsError() throws Exception {
		NacosConfigEndpoint endpoint = new NacosConfigEndpoint();
		Field field = NacosConfigEndpoint.class.getDeclaredField("applicationContext");
		field.setAccessible(true);
		field.set(endpoint, new ApplicationContextStub());

		Map<String, Object> result = endpoint.invoke();

		assertEquals(1, result.size());
		assertTrue(((String) result.get("error"))
				.startsWith("environment type not match ConfigurableEnvironment: "));
	}

	@Test
	public void onApplicationEventWithNullEventThrowsNullPointerException() {
		NacosConfigEndpoint endpoint = new NacosConfigEndpoint();

		try {
			endpoint.onApplicationEvent(null);
		}
		catch (NullPointerException expected) {
			return;
		}
		throw new AssertionError("Expected NullPointerException");
	}

	private static class ApplicationContextStub implements ApplicationContext {
		@Override
		public org.springframework.core.env.Environment getEnvironment() {
			return new org.springframework.core.env.StandardEnvironment();
		}

		@Override
		public <T> T getBean(String name, Class<T> requiredType) {
			throw new AssertionError("Bean lookup should not occur for this environment");
		}

		@Override
		public String getId() {
			return "test";
		}

		@Override
		public String getApplicationName() {
			return "test";
		}

		@Override
		public String getDisplayName() {
			return "test";
		}

		@Override
		public long getStartupDate() {
			return 0L;
		}

		@Override
		public org.springframework.context.ApplicationContext getParent() {
			return null;
		}

		@Override
		public org.springframework.beans.factory.BeanFactory getAutowireCapableBeanFactory() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsBean(String name) {
			return false;
		}

		@Override
		public boolean isSingleton(String name) {
			return false;
		}

		@Override
		public boolean isPrototype(String name) {
			return false;
		}

		@Override
		public boolean isTypeMatch(String name, Class<?> targetType) {
			return false;
		}

		@Override
		public Class<?> getType(String name) {
			return null;
		}

		@Override
		public String[] getAliases(String name) {
			return new String[0];
		}

		@Override
		public Object getBean(String name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T getBean(String name, Class<T> requiredType, Object... args) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object getBean(String name, Object... args) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T getBean(Class<T> requiredType) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T getBean(Class<T> requiredType, Object... args) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsBeanDefinition(String beanName) {
			return false;
		}

		@Override
		public int getBeanDefinitionCount() {
			return 0;
		}

		@Override
		public String[] getBeanDefinitionNames() {
			return new String[0];
		}

		@Override
		public String[] getBeanNamesForType(org.springframework.core.ResolvableType type) {
			return new String[0];
		}

		@Override
		public String[] getBeanNamesForType(org.springframework.core.ResolvableType type,
				boolean includeNonSingletons, boolean allowEagerInit) {
			return new String[0];
		}

		@Override
		public String[] getBeanNamesForType(Class<?> type) {
			return new String[0];
		}

		@Override
		public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons,
				boolean allowEagerInit) {
			return new String[0];
		}

		@Override
		public <T> java.util.Map<String, T> getBeansOfType(Class<T> type) {
			return java.util.Collections.emptyMap();
		}

		@Override
		public <T> java.util.Map<String, T> getBeansOfType(Class<T> type,
				boolean includeNonSingletons, boolean allowEagerInit) {
			return java.util.Collections.emptyMap();
		}

		@Override
		public String[] getBeanNamesForAnnotation(Class<? extends java.lang.annotation.Annotation> annotationType) {
			return new String[0];
		}

		@Override
		public java.util.Map<String, Object> getBeansWithAnnotation(
				Class<? extends java.lang.annotation.Annotation> annotationType) {
			return java.util.Collections.emptyMap();
		}

		@Override
		public <A extends java.lang.annotation.Annotation> A findAnnotationOnBean(String beanName,
				Class<A> annotationType) {
			return null;
		}

		@Override
		public org.springframework.beans.factory.BeanFactory getParentBeanFactory() {
			return null;
		}

		@Override
		public boolean containsLocalBean(String name) {
			return false;
		}

		@Override
		public org.springframework.core.io.Resource getResource(String location) {
			return null;
		}

		@Override
		public ClassLoader getClassLoader() {
			return getClass().getClassLoader();
		}

		@Override
		public org.springframework.context.MessageSource getParentMessageSource() {
			return null;
		}

		@Override
		public org.springframework.context.MessageSource getMessageSource() {
			return null;
		}

		@Override
		public org.springframework.context.event.ApplicationEventMulticaster getApplicationEventMulticaster() {
			return null;
		}

		@Override
		public org.springframework.core.env.Environment getEnvironment() {
			return new org.springframework.core.env.StandardEnvironment();
		}

		@Override
		public void publishEvent(org.springframework.context.ApplicationEvent event) {
		}

		@Override
		public void publishEvent(Object event) {
		}

		@Override
		public void addBeanFactoryPostProcessor(
				org.springframework.beans.factory.config.BeanFactoryPostProcessor postProcessor) {
		}

		@Override
		public void addApplicationListener(
				org.springframework.context.ApplicationListener<?> listener) {
		}

		@Override
		public void refresh() {
		}

		@Override
		public void registerShutdownHook() {
		}

		@Override
		public void close() {
		}

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public org.springframework.beans.factory.config.AutowireCapableBeanFactory getAutowireCapableBeanFactory() {
			throw new UnsupportedOperationException();
		}
	}
}
