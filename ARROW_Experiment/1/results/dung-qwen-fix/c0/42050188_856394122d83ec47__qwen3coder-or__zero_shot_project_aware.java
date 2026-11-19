package de.monticore.generating.templateengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

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
    params.add(42);
    List<Class<?>> result = ObjectFactory.getTypes(params);
    assertEquals(2, result.size());
    assertEquals(String.class, result.get(0));
    assertEquals(Integer.class, result.get(1));
  }

  @Test
  public void testSetClassLoader() {
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      ObjectFactory.setClassLoader(null);
      // Should not throw exception
    } finally {
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    }
  }

  @Test
  public void testDoCreateObjectWithMatchingConstructor() {
    ObjectFactory factory = new ObjectFactory();
    List<Class<?>> paramTypes = new ArrayList<>();
    paramTypes.add(String.class);
    List<Object> params = new ArrayList<>();
    params.add("test");
    Object result = factory.doCreateObject("java.lang.String", paramTypes, params);
    assertNotNull(result);
    assertEquals(String.class, result.getClass());
  }

  @Test
  public void testDoCreateObjectWithNonMatchingConstructor() {
    ObjectFactory factory = new ObjectFactory();
    List<Class<?>> paramTypes = new ArrayList<>();
    paramTypes.add(Integer.class);
    List<Object> params = new ArrayList<>();
    params.add("invalid");
    Object result = factory.doCreateObject("java.lang.String", paramTypes, params);
    assertNull(result);
  }

  @Test
  public void testDoGetTypes() {
    ObjectFactory factory = new ObjectFactory();
    List<Object> params = new ArrayList<>();
    params.add("test");
    params.add(42);
    List<Class<?>> result = factory.doGetTypes(params);
    assertEquals(2, result.size());
    assertEquals(String.class, result.get(0));
    assertEquals(Integer.class, result.get(1));
  }

  @Test
  public void testDoSetClassLoader() {
    ObjectFactory factory = new ObjectFactory();
    ClassLoader mockClassLoader = new MockClassLoader();
    factory.doSetClassLoader(mockClassLoader);
    // We can't easily verify the internal state, but we ensure no exception is thrown
  }

  // Simple mock class loader for testing purposes
  private static class MockClassLoader extends ClassLoader {
    // Empty implementation for testing
  }
}
