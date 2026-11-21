package io.wcm.caravan.jaxrs.publisher.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.junit.Test;

import com.google.common.collect.Sets;

import io.wcm.caravan.jaxrs.publisher.JaxRsClassesProvider;
import io.wcm.caravan.jaxrs.publisher.JaxRsComponent;

public class JaxRsApplication_ArrowTest {

  @Test
  public void testGetSingletons() {
    JaxRsComponent component1 = org.mockito.Mockito.mock(JaxRsComponent.class);
    JaxRsComponent component2 = org.mockito.Mockito.mock(JaxRsComponent.class);
    JaxRsComponent component3 = org.mockito.Mockito.mock(JaxRsComponent.class);

    Set<JaxRsComponent> localComponents = Sets.newHashSet(component1, component2);
    Set<JaxRsComponent> globalComponents = Sets.newHashSet(component3);

    JaxRsApplication application = new JaxRsApplication(localComponents, globalComponents, Collections.emptySet(),
        Collections.emptySet());

    Set<Object> singletons = application.getSingletons();

    assertNotNull(singletons);
    assertEquals(3, singletons.size());
    // Check that the set contains the expected components without relying on iteration order
    assertTrueContains(singletons, component1);
    assertTrueContains(singletons, component2);
    assertTrueContains(singletons, component3);
  }

  private void assertTrueContains(Set<Object> set, Object element) {
    for (Object o : set) {
      if (o == element) {
        return;
      }
    }
    org.junit.Assert.fail("Set does not contain " + element);
  }

  @Test
  public void testGetClasses() {
    JaxRsClassesProvider provider1 = org.mockito.Mockito.mock(JaxRsClassesProvider.class);
    JaxRsClassesProvider provider2 = org.mockito.Mockito.mock(JaxRsClassesProvider.class);

    Class<?> clazz1 = String.class;
    Class<?> clazz2 = Integer.class;
    Class<?> clazz3 = Double.class;

    org.mockito.Mockito.when(provider1.getClasses()).thenReturn(Sets.newHashSet(clazz1, clazz2));
    org.mockito.Mockito.when(provider2.getClasses()).thenReturn(Sets.newHashSet(clazz3));

    Set<JaxRsClassesProvider> localClassesProviders = Sets.newHashSet(provider1);
    Set<JaxRsClassesProvider> globalClassesProviders = Sets.newHashSet(provider2);

    JaxRsApplication application = new JaxRsApplication(Collections.emptySet(), Collections.emptySet(),
        localClassesProviders, globalClassesProviders);

    Set<Class<?>> classes = application.getClasses();

    assertNotNull(classes);
    assertEquals(3, classes.size());
    // Check that the set contains the expected classes without relying on iteration order
    assertTrueContainsClass(classes, clazz1);
    assertTrueContainsClass(classes, clazz2);
    assertTrueContainsClass(classes, clazz3);
  }

  private void assertTrueContainsClass(Set<Class<?>> set, Class<?> element) {
    for (Class<?> c : set) {
      if (c == element) {
        return;
      }
    }
    org.junit.Assert.fail("Set does not contain " + element);
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
