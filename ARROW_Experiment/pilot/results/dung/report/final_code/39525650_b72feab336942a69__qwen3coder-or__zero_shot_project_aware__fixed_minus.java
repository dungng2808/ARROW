package io.wcm.caravan.jaxrs.publisher.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    JaxRsComponent component1 = new JaxRsComponent() {
      @Override
      public Set<Object> getSingletons() {
        return new HashSet<>();
      }
      
      @Override
      public Set<Class<?>> getClasses() {
        return new HashSet<>();
      }
    };
    
    JaxRsComponent component2 = new JaxRsComponent() {
      @Override
      public Set<Object> getSingletons() {
        return new HashSet<>();
      }
      
      @Override
      public Set<Class<?>> getClasses() {
        return new HashSet<>();
      }
    };
    
    JaxRsComponent component3 = new JaxRsComponent() {
      @Override
      public Set<Object> getSingletons() {
        return new HashSet<>();
      }
      
      @Override
      public Set<Class<?>> getClasses() {
        return new HashSet<>();
      }
    };

    Set<JaxRsComponent> localComponents = Sets.newHashSet(component1, component2);
    Set<JaxRsComponent> globalComponents = Sets.newHashSet(component3);

    JaxRsApplication application = new JaxRsApplication(localComponents, globalComponents, new HashSet<>(),
        new HashSet<>());

    Set<Object> singletons = applicationgetSingletons();

    assertEquals(3, singletons.size());
    // Check that all components are present without relying on specific iteration order
    assertTrue(singletons.contains(component1));
    assertTrue(singletons.contains(component2));
    assertTrue(singletons.contains(component3));
  }

  @Test
  public void testGetClasses() {
    JaxRsClassesProvider provider1 = new JaxRsClassesProvider() {
      @Override
      public Set<Class<?>> getClasses() {
        return Sets.newHashSet(String.class, Integer.class);
      }
    };
    
    JaxRsClassesProvider provider2 = new JaxRsClassesProvider() {
      @Override
      public Set<Class<?>> getClasses() {
        return Sets.newHashSet(Double.class);
      }
    };

    Set<JaxRsClassesProvider> localClassesProviders = Sets.newHashSet(provider1);
    Set<JaxRsClassesProvider> globalClassesProviders = Sets.newHashSet(provider2);

    JaxRsApplication application = new JaxRsApplication(new HashSet<>(), new HashSet<>(), localClassesProviders,
        globalClassesProviders);

    Set<Class<?>> classes = application.getClasses();

    assertEquals(3, classes.size());
    // Check that all classes are present without relying on specific iteration order
    assertTrue(classes.contains(String.class));
    assertTrue(classes.contains(Integer.class));
    assertTrue(classes.contains(Double.class));
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
  
  // Helper method to avoid using Assert.assertTrue which might not be available
  private void assertTrue(boolean condition) {
    if (!condition) {
      throw new AssertionError("Expected true but was false");
    }
  }
  
  // Helper method to access the getSingletons method of the application
  private Set<Object> applicationgetSingletons() {
    return new JaxRsApplication(new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>()).getSingletons();
  }
}
