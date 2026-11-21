package de.monticore.generating.templateengine;

import de.se_rwth.commons.logging.Log;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ObjectFactory_ArrowTest {

  @Test
  public void testCreateObjectWithNoParams() {
    Object result = ObjectFactory.createObject("java.lang.String");
    assertNotNull(result);
    assertEquals(String.class, result.getClass());
  }

  @Test
  public void testCreateObjectWithParams() {
    List<Object> params = new ArrayList<>();
    params.add("test");
    Object result = ObjectFactory.createObject("java.lang.String", params);
    assertNotNull(result);
    assertEquals(String.class, result.getClass());
  }

  @Test
  public void testCreateObjectWithParamTypesAndParams() {
    List<Class<?>> paramTypes = new ArrayList<>();
    paramTypes.add(String.class);
    List<Object> params = new ArrayList<>();
    params.add("test");
    Object result = ObjectFactory.createObject("java.lang.String", paramTypes, params);
    assertNotNull(result);
    assertEquals(String.class, result.getClass());
  }

  @Test
  public void testCreateObjectWithNonExistentClass() {
    Object result = ObjectFactory.createObject("non.existent.Class");
    assertNull(result);
  }

  @Test
  public void testGetTypes() {
    List<Object> params = new ArrayList<>();
    params.add("test");
    params.add(123);
    List<Class<?>> result = ObjectFactory.getTypes(params);
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(String.class, result.get(0));
    assertEquals(Integer.class, result.get(1));
  }

  @Test
  public void testSetClassLoader() {
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      ClassLoader mockClassLoader = new MockClassLoader();
      ObjectFactory.setClassLoader(mockClassLoader);
      // Just verify that the method doesn't throw an exception
      assertTrue(true);
    } finally {
      // Restore original class loader
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    }
  }

  @Test
  public void testSetClassLoaderNull() {
    ObjectFactory.setClassLoader(null);
    // Just verify that the method doesn't throw an exception
    assertTrue(true);
  }

  // Helper class for testing class loader
  private static class MockClassLoader extends ClassLoader {
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
      return super.loadClass(name);
    }
  }
}
