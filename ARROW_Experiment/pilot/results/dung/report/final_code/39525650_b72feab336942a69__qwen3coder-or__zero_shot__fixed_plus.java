package io.wcm.caravan.jaxrs.publisher.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.junit.Test;
import org.mockito.Mockito;

import io.wcm.caravan.jaxrs.publisher.JaxRsClassesProvider;
import io.wcm.caravan.jaxrs.publisher.JaxRsComponent;

public class JaxRsApplication_ArrowTest {

  @Test
  public void testGetSingletons() {
    JaxRsComponent component1 = Mockito.mock(JaxRsComponent.class);
    JaxRsComponent component2 = Mockito.mock(JaxRsComponent.class);
    JaxRsComponent component3 = Mockito.mock(JaxRsComponent.class);

    Set<JaxRsComponent> localComponents = new HashSet<JaxRsComponent>();
    localComponents.add(component1);
    localComponents.add(component2);
    Set<JaxRsComponent> globalComponents = new HashSet<JaxRsComponent>();
    globalComponents.add(component3);

    JaxRsApplication application = new JaxRsApplication(localComponents, globalComponents, Collections.emptySet(),
        Collections.emptySet());

    Set<Object> singletons = application.getSingletons();

    assertNotNull(singletons);
    assertEquals(3, singletons.size());
    assertTrueContains(singletons, component1);
    assertTrueContains(singletons, component2);
    assertTrueContains(singletons, component3);
  }

  private void assertTrueContains(Set<Object> singletons, JaxRsComponent component) {
    boolean found = false;
    for (Object obj : singletons) {
      if (obj == component) {
        found = true;
        break;
      }
    }
    if (!found) {
      org.junit.Assert.fail("Expected set to contain " + component);
    }
  }

  @Test
  public void testGetClasses() {
    JaxRsClassesProvider provider1 = Mockito.mock(JaxRsClassesProvider.class);
    JaxRsClassesProvider provider2 = Mockito.mock(JaxRsClassesProvider.class);

    Class<?> clazz1 = String.class;
    Class<?> clazz2 = Integer.class;
    Class<?> clazz3 = Double.class;

    Set<Class<?>> classes1 = new HashSet<Class<?>>();
    classes1.add(clazz1);
    classes1.add(clazz2);
    Set<Class<?>> classes2 = new HashSet<Class<?>>();
    classes2.add(clazz3);

    Mockito.when(provider1.getClasses()).thenReturn(classes1);
    Mockito.when(provider2.getClasses()).thenReturn(classes2);

    Set<JaxRsClassesProvider> localClassesProviders = new HashSet<JaxRsClassesProvider>();
    localClassesProviders.add(provider1);
    Set<JaxRsClassesProvider> globalClassesProviders = new HashSet<JaxRsClassesProvider>();
    globalClassesProviders.add(provider2);

    JaxRsApplication application = new JaxRsApplication(Collections.emptySet(), Collections.emptySet(),
        localClassesProviders, globalClassesProviders);

    Set<Class<?>> classes = application.getClasses();

    assertNotNull(classes);
    assertEquals(3, classes.size());
    assertTrueContainsClass(classes, clazz1);
    assertTrueContainsClass(classes, clazz2);
    assertTrueContainsClass(classes, clazz3);
  }

  private void assertTrueContainsClass(Set<Class<?>> classes, Class<?> clazz) {
    boolean found = false;
    for (Class<?> c : classes) {
      if (c == clazz) {
        found = true;
        break;
      }
    }
    if (!found) {
      org.junit.Assert.fail("Expected set to contain " + clazz);
    }
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
