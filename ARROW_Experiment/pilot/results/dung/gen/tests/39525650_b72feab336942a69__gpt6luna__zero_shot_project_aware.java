package io.wcm.caravan.jaxrs.publisher.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ServerProperties;
import org.junit.Test;

import io.wcm.caravan.jaxrs.publisher.JaxRsClassesProvider;
import io.wcm.caravan.jaxrs.publisher.JaxRsComponent;

public class JaxRsApplication_ArrowTest {

  @Test
  public void getSingletonsCombinesGlobalAndLocalComponents() {
    JaxRsComponent global = mock(JaxRsComponent.class);
    JaxRsComponent local = mock(JaxRsComponent.class);
    Set<JaxRsComponent> globals = new HashSet<JaxRsComponent>(Collections.singleton(global));
    Set<JaxRsComponent> locals = new HashSet<JaxRsComponent>(Collections.singleton(local));

    Application application = new JaxRsApplication(locals, globals,
        Collections.<JaxRsClassesProvider>emptySet(), Collections.<JaxRsClassesProvider>emptySet());

    Set<Object> expected = new HashSet<Object>();
    expected.add(global);
    expected.add(local);
    assertEquals(expected, application.getSingletons());
  }

  @Test
  public void getSingletonsIncludesComponentsOnlyOnce() {
    JaxRsComponent component = mock(JaxRsComponent.class);
    Set<JaxRsComponent> components = new HashSet<JaxRsComponent>(Collections.singleton(component));

    Application application = new JaxRsApplication(components, components,
        Collections.<JaxRsClassesProvider>emptySet(), Collections.<JaxRsClassesProvider>emptySet());

    assertEquals(Collections.<Object>singleton(component), application.getSingletons());
  }

  @Test
  public void getClassesCombinesClassesFromLocalAndGlobalProviders() {
    JaxRsClassesProvider local = mock(JaxRsClassesProvider.class);
    JaxRsClassesProvider global = mock(JaxRsClassesProvider.class);
    when(local.getClasses()).thenReturn(new HashSet<Class<?>>(Arrays.<Class<?>>asList(String.class, Integer.class)));
    when(global.getClasses()).thenReturn(new HashSet<Class<?>>(Arrays.<Class<?>>asList(Integer.class, Long.class)));

    Set<JaxRsClassesProvider> locals = new HashSet<JaxRsClassesProvider>(Collections.singleton(local));
    Set<JaxRsClassesProvider> globals = new HashSet<JaxRsClassesProvider>(Collections.singleton(global));
    Application application = new JaxRsApplication(Collections.<JaxRsComponent>emptySet(),
        Collections.<JaxRsComponent>emptySet(), locals, globals);

    assertEquals(new HashSet<Class<?>>(Arrays.<Class<?>>asList(String.class, Integer.class, Long.class)),
        application.getClasses());
  }

  @Test
  public void getClassesReturnsEmptySetWhenThereAreNoProviders() {
    Application application = new JaxRsApplication(Collections.<JaxRsComponent>emptySet(),
        Collections.<JaxRsComponent>emptySet(), Collections.<JaxRsClassesProvider>emptySet(),
        Collections.<JaxRsClassesProvider>emptySet());

    assertTrue(application.getClasses().isEmpty());
  }

  @Test
  public void getPropertiesReturnsDefaultJerseyProperties() {
    Application application = new JaxRsApplication(Collections.<JaxRsComponent>emptySet(),
        Collections.<JaxRsComponent>emptySet(), Collections.<JaxRsClassesProvider>emptySet(),
        Collections.<JaxRsClassesProvider>emptySet());

    Map<String, Object> properties = application.getProperties();
    assertEquals(Boolean.FALSE, properties.get(ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE));
    assertEquals(Boolean.TRUE, properties.get(ServerProperties.FEATURE_AUTO_DISCOVERY_DISABLE));
    assertEquals(Boolean.TRUE, properties.get(ServerProperties.WADL_FEATURE_DISABLE));
    assertEquals(3, properties.size());
  }
}
