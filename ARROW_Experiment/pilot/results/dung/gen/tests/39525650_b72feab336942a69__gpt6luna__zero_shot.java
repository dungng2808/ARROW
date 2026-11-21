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
    JaxRsComponent global = new JaxRsComponent() { };
    JaxRsComponent local = new JaxRsComponent() { };
    Set<JaxRsComponent> globals = Collections.singleton(global);
    Set<JaxRsComponent> locals = Collections.singleton(local);

    JaxRsApplication application = new JaxRsApplication(locals, globals,
        Collections.<JaxRsClassesProvider>emptySet(), Collections.<JaxRsClassesProvider>emptySet());

    Set<Object> expected = new HashSet<Object>(Arrays.<Object>asList(global, local));
    assertEquals(expected, application.getSingletons());
  }

  @Test
  public void getSingletonsDoesNotDuplicateComponentsPresentInBothSets() {
    JaxRsComponent component = new JaxRsComponent() { };
    Set<JaxRsComponent> components = Collections.singleton(component);

    JaxRsApplication application = new JaxRsApplication(components, components,
        Collections.<JaxRsClassesProvider>emptySet(), Collections.<JaxRsClassesProvider>emptySet());

    assertEquals(Collections.<Object>singleton(component), application.getSingletons());
  }

  @Test
  public void getClassesCombinesClassesFromBothProviderSets() {
    JaxRsClassesProvider localProvider = new JaxRsClassesProvider() {
      @Override
      public Set<Class<?>> getClasses() {
        return Collections.<Class<?>>singleton(String.class);
      }
    };
    JaxRsClassesProvider globalProvider = new JaxRsClassesProvider() {
      @Override
      public Set<Class<?>> getClasses() {
        return Collections.<Class<?>>singleton(Integer.class);
      }
    };

    JaxRsApplication application = new JaxRsApplication(Collections.<JaxRsComponent>emptySet(),
        Collections.<JaxRsComponent>emptySet(), Collections.singleton(localProvider),
        Collections.singleton(globalProvider));

    assertEquals(new HashSet<Class<?>>(Arrays.<Class<?>>asList(String.class, Integer.class)), application.getClasses());
  }

  @Test
  public void getClassesReturnsEmptySetWhenThereAreNoProviders() {
    JaxRsApplication application = new JaxRsApplication(Collections.<JaxRsComponent>emptySet(),
        Collections.<JaxRsComponent>emptySet(), Collections.<JaxRsClassesProvider>emptySet(),
        Collections.<JaxRsClassesProvider>emptySet());

    assertTrue(application.getClasses().isEmpty());
  }

  @Test
  public void getPropertiesReturnsDefaultConfiguration() {
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
