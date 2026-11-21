package io.wcm.caravan.jaxrs.publisher.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.junit.Test;

import com.google.common.collect.Sets;

import io.wcm.caravan.jaxrs.publisher.JaxRsClassesProvider;
import io.wcm.caravan.jaxrs.publisher.JaxRsComponent;

public class JaxRsApplication_ArrowTest {

  @Test
  public void testConstructor() {
    Set<JaxRsComponent> localComponents = new HashSet<>();
    Set<JaxRsComponent> globalComponents = new HashSet<>();
    Set<JaxRsClassesProvider> localClassesProviders = new HashSet<>();
    Set<JaxRsClassesProvider> globalClassesProviders = new HashSet<>();

    JaxRsApplication application = new JaxRsApplication(localComponents, globalComponents, localClassesProviders,
        globalClassesProviders);

    assertNotNull(application);
  }

  @Test
  public void testGetSingletons() {
    JaxRsComponent component1 = mock(JaxRsComponent.class);
    JaxRsComponent component2 = mock(JaxRsComponent.class);
    JaxRsComponent component3 = mock(JaxRsComponent.class);

    Set<JaxRsComponent> localComponents = Sets.newHashSet(component1, component2);
    Set<JaxRsComponent> globalComponents = Sets.newHashSet(component3);

    JaxRsApplication application = new JaxRsApplication(localComponents, globalComponents, new HashSet<>(),
        new HashSet<>());

    Set<Object> singletons = application.getSingletons();

    assertEquals(3, singletons.size());
    // Verify that all components are present without relying on iteration order
    assertTrueContainsObject(singletons, component1);
    assertTrueContainsObject(singletons, component2);
    assertTrueContainsObject(singletons, component3);
  }

  private void assertTrueContainsObject(Set<Object> set, Object element) {
    for (Object o : set) {
      if (o == element) {
        return;
      }
    }
    throw new AssertionError("Set does not contain " + element);
  }

  @Test
  public void testGetClasses() {
    JaxRsClassesProvider provider1 = mock(JaxRsClassesProvider.class);
    JaxRsClassesProvider provider2 = mock(JaxRsClassesProvider.class);

    Class<?> clazz1 = String.class;
    Class<?> clazz2 = Integer.class;
    Class<?> clazz3 = Double.class;

    when(provider1.getClasses()).thenReturn(Sets.newHashSet(clazz1, clazz2));
    when(provider2.getClasses()).thenReturn(Sets.newHashSet(clazz3));

    Set<JaxRsClassesProvider> localClassesProviders = Sets.newHashSet(provider1);
    Set<JaxRsClassesProvider> globalClassesProviders = Sets.newHashSet(provider2);

    JaxRsApplication application = new JaxRsApplication(new HashSet<>(), new HashSet<>(), localClassesProviders,
        globalClassesProviders);

    Set<Class<?>> classes = application.getClasses();

    assertEquals(3, classes.size());
    // Verify that all classes are present without relying on iteration order
    assertTrueContainsClass(classes, clazz1);
    assertTrueContainsClass(classes, clazz2);
    assertTrueContainsClass(classes, clazz3);
  }

  private void assertTrueContainsClass(Set<Class<?>> set, Class<?> element) {
    for (Class<?> o : set) {
      if (o == element) {
        return;
      }
    }
    throw new AssertionError("Set does not contain " + element);
  }

  @Test
  public void testGetProperties() {
    JaxRsApplication application = new JaxRsApplication(new HashSet<>(), new HashSet<>(), new HashSet<>(),
        new HashSet<>());

    Map<String, Object> properties = application.getProperties();

    assertNotNull(properties);
    assertEquals(3, properties.size());
    assertEquals(false, properties.get("jersey.config.server.provider.scanning.packages"));
    assertEquals(true, properties.get("jersey.config.server.feature.autoDiscovery.disable"));
    assertEquals(true, properties.get("jersey.config.server.wadl.disableWadl"));
  }
}
