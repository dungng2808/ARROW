package io.wcm.caravan.jaxrs.publisher.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
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
  public void testGetSingletons() {
    JaxRsComponent component1 = mock(JaxRsComponent.class);
    JaxRsComponent component2 = mock(JaxRsComponent.class);
    JaxRsComponent component3 = mock(JaxRsComponent.class);

    Set<JaxRsComponent> localComponents = Sets.newHashSet(component1, component2);
    Set<JaxRsComponent> globalComponents = Sets.newHashSet(component3);

    JaxRsApplication application = new JaxRsApplication(localComponents, globalComponents, Collections.emptySet(),
        Collections.emptySet());

    Set<Object> singletons = application.getSingletons();
    assertNotNull(singletons);
    assertEquals(3, singletons.size());
    assertTrue(singletons.contains(component1));
    assertTrue(singletons.contains(component2));
    assertTrue(singletons.contains(component3));
  }

  @Test
  public void testGetClasses() {
    JaxRsClassesProvider provider1 = mock(JaxRsClassesProvider.class);
    JaxRsClassesProvider provider2 = mock(JaxRsClassesProvider.class);

    Set<Class<?>> classes1 = Sets.newHashSet(String.class, Integer.class);
    Set<Class<?>> classes2 = Sets.newHashSet(Double.class, Boolean.class);

    when(provider1.getClasses()).thenReturn(classes1);
    when(provider2.getClasses()).thenReturn(classes2);

    Set<JaxRsClassesProvider> localClassesProviders = Sets.newHashSet(provider1);
    Set<JaxRsClassesProvider> globalClassesProviders = Sets.newHashSet(provider2);

    JaxRsApplication application = new JaxRsApplication(Collections.emptySet(), Collections.emptySet(),
        localClassesProviders, globalClassesProviders);

    Set<Class<?>> classes = application.getClasses();
    assertNotNull(classes);
    assertEquals(4, classes.size());
    assertTrue(classes.contains(String.class));
    assertTrue(classes.contains(Integer.class));
    assertTrue(classes.contains(Double.class));
    assertTrue(classes.contains(Boolean.class));
  }

  @Test
  public void testGetProperties() {
    JaxRsApplication application = new JaxRsApplication(Collections.emptySet(), Collections.emptySet(),
        Collections.emptySet(), Collections.emptySet());

    Map<String, Object> properties = application.getProperties();
    assertNotNull(properties);
    assertEquals(3, properties.size());
    assertTrue(properties.containsKey("jersey.config.server.provider.classnames"));
    assertTrue(properties.containsKey("jersey.config.server.feature.autoDiscovery.disable"));
    assertTrue(properties.containsKey("jersey.config.server.wadl.disableWadl"));
  }

  private void assertTrue(boolean condition) {
    if (!condition) {
      throw new AssertionError();
    }
  }
}
