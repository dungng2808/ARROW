package io.wcm.caravan.jaxrs.publisher.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Sets;

import io.wcm.caravan.jaxrs.publisher.JaxRsClassesProvider;
import io.wcm.caravan.jaxrs.publisher.JaxRsComponent;

public class JaxRsApplication_ArrowTest {

  @Test
  public void testGetSingletons() {
    JaxRsComponent component1 = Mockito.mock(JaxRsComponent.class);
    JaxRsComponent component2 = Mockito.mock(JaxRsComponent.class);
    JaxRsComponent component3 = Mockito.mock(JaxRsComponent.class);

    Set<JaxRsComponent> localComponents = Sets.newHashSet(component1, component2);
    Set<JaxRsComponent> globalComponents = Sets.newHashSet(component3);

    JaxRsApplication application = new JaxRsApplication(localComponents, globalComponents, Collections.emptySet(),
        Collections.emptySet());

    Set<Object> singletons = application.getSingletons();

    assertNotNull(singletons);
    assertEquals(3, singletons.size());
    // Check that the set contains all expected components without relying on iteration order
    assertTrueContainsAll(singletons, component1, component2, component3);
  }

  private void assertTrueContainsAll(Set<Object> actual, Object... expected) {
    assertEquals(expected.length, actual.size());
    for (Object obj : expected) {
      assertTrue(actual.contains(obj));
    }
  }

  private void assertTrue(boolean condition) {
    if (!condition) {
      throw new AssertionError("Expected true but was false");
    }
  }

  @Test
  public void testGetClasses() {
    JaxRsClassesProvider provider1 = Mockito.mock(JaxRsClassesProvider.class);
    JaxRsClassesProvider provider2 = Mockito.mock(JaxRsClassesProvider.class);

    Class<?> clazz1 = String.class;
    Class<?> clazz2 = Integer.class;
    Class<?> clazz3 = Double.class;

    Mockito.when(provider1.getClasses()).thenReturn(Sets.newHashSet(clazz1, clazz2));
    Mockito.when(provider2.getClasses()).thenReturn(Sets.newHashSet(clazz3));

    Set<JaxRsClassesProvider> localClassesProviders = Sets.newHashSet(provider1);
    Set<JaxRsClassesProvider> globalClassesProviders = Sets.newHashSet(provider2);

    JaxRsApplication application = new JaxRsApplication(Collections.emptySet(), Collections.emptySet(),
        localClassesProviders, globalClassesProviders);

    Set<Class<?>> classes = application.getClasses();

    assertNotNull(classes);
    assertEquals(3, classes.size());
    assertTrue(classes.contains(clazz1));
    assertTrue(classes.contains(clazz2));
    assertTrue(classes.contains(clazz3));
  }

  @Test
  public void testGetProperties() {
    JaxRsApplication application = new JaxRsApplication(Collections.emptySet(), Collections.emptySet(),
        Collections.emptySet(), Collections.emptySet());

    Map<String, Object> properties = application.getProperties();

    assertNotNull(properties);
    assertEquals(3, properties.size());
    assertEquals(false, properties.get("jersey.config.server.provider.scanning.packages"));
    assertEquals(true, properties.get("jersey.config.server.provider.scanning.disable"));
    assertEquals(true, properties.get("jersey.config.server.wadl.disableWadl"));
  }
}
