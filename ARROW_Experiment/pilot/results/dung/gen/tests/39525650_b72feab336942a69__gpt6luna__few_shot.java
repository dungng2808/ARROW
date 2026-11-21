package io.wcm.caravan.jaxrs.publisher.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.glassfish.jersey.server.ServerProperties;
import org.junit.Test;

import io.wcm.caravan.jaxrs.publisher.JaxRsClassesProvider;
import io.wcm.caravan.jaxrs.publisher.JaxRsComponent;

public class JaxRsApplication_ArrowTest {

  @Test
  public void getSingletonsCombinesGlobalAndLocalComponents() {
    JaxRsComponent local = new JaxRsComponent() { };
    JaxRsComponent global = new JaxRsComponent() { };
    Set<JaxRsComponent> localComponents = new HashSet<>(Collections.singletonList(local));
    Set<JaxRsComponent> globalComponents = new HashSet<>(Collections.singletonList(global));

    JaxRsApplication application = new JaxRsApplication(localComponents, globalComponents,
        Collections.<JaxRsClassesProvider>emptySet(), Collections.<JaxRsClassesProvider>emptySet());

    Set<Object> singletons = application.getSingletons();

    assertEquals(2, singletons.size());
    assertTrue(singletons.contains(local));
    assertTrue(singletons.contains(global));
  }

  @Test
  public void getClassesCombinesClassesFromBothProviders() {
    JaxRsClassesProvider localProvider = new JaxRsClassesProvider() {
      @Override
      public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.<Class<?>>asList(String.class, Integer.class));
      }
    };
    JaxRsClassesProvider globalProvider = new JaxRsClassesProvider() {
      @Override
      public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Collections.<Class<?>>singletonList(Long.class));
      }
    };

    JaxRsApplication application = new JaxRsApplication(Collections.<JaxRsComponent>emptySet(),
        Collections.<JaxRsComponent>emptySet(), Collections.singleton(localProvider),
        Collections.singleton(globalProvider));

    Set<Class<?>> classes = application.getClasses();

    assertEquals(new HashSet<Class<?>>(Arrays.<Class<?>>asList(String.class, Integer.class, Long.class)), classes);
  }

  @Test
  public void getClassesReturnsEmptySetWhenThereAreNoProviders() {
    JaxRsApplication application = new JaxRsApplication(Collections.<JaxRsComponent>emptySet(),
        Collections.<JaxRsComponent>emptySet(), Collections.<JaxRsClassesProvider>emptySet(),
        Collections.<JaxRsClassesProvider>emptySet());

    assertTrue(application.getClasses().isEmpty());
  }

  @Test
  public void getPropertiesReturnsDefaultJerseyProperties() {
    JaxRsApplication application = new JaxRsApplication(Collections.<JaxRsComponent>emptySet(),
        Collections.<JaxRsComponent>emptySet(), Collections.<JaxRsClassesProvider>emptySet(),
        Collections.<JaxRsClassesProvider>emptySet());

    Map<String, Object> properties = application.getProperties();

    assertEquals(Boolean.FALSE, properties.get(ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE));
    assertEquals(Boolean.TRUE, properties.get(ServerProperties.FEATURE_AUTO_DISCOVERY_DISABLE));
    assertEquals(Boolean.TRUE, properties.get(ServerProperties.WADL_FEATURE_DISABLE));
    assertEquals(3, properties.size());
  }
}
