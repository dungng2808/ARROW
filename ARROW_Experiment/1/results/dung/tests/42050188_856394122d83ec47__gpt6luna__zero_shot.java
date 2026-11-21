package de.monticore.generating.templateengine;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ObjectFactory_ArrowTest {

  public static class Sample {
    public Sample() {
    }

    public Sample(Number value) {
      this.value = value;
    }

    public Sample(String value) {
      this.text = value;
    }

    private Number value;
    private String text;
  }

  @Test
  public void createObjectWithNoArguments() {
    Object result = ObjectFactory.createObject(Sample.class.getName());
    assertEquals(Sample.class, result.getClass());
  }

  @Test
  public void createObjectWithExactConstructor() {
    List<Object> params = new ArrayList<Object>();
    params.add("hello");

    Object result = ObjectFactory.createObject(Sample.class.getName(), params);

    assertEquals(Sample.class, result.getClass());
    assertEquals("hello", ((Sample) result).text);
  }

  @Test
  public void createObjectWithAssignableConstructor() {
    List<Class<?>> paramTypes = new ArrayList<Class<?>>();
    paramTypes.add(Integer.class);
    List<Object> params = new ArrayList<Object>();
    params.add(Integer.valueOf(7));

    Object result = ObjectFactory.createObject(Sample.class.getName(), paramTypes, params);

    assertEquals(Sample.class, result.getClass());
    assertEquals(Integer.valueOf(7), ((Sample) result).value);
  }

  @Test
  public void createObjectForMissingClassReturnsNull() {
    Object result = ObjectFactory.createObject("missing.example.NoSuchClass");
    assertNull(result);
  }

  @Test
  public void createObjectWithoutMatchingConstructorReturnsNull() {
    List<Class<?>> paramTypes = new ArrayList<Class<?>>();
    paramTypes.add(Boolean.class);
    List<Object> params = new ArrayList<Object>();
    params.add(Boolean.TRUE);

    Object result = ObjectFactory.createObject(Sample.class.getName(), paramTypes, params);

    assertNull(result);
  }

  @Test
  public void createObjectWithEmptyParameterLists() {
    Object result = ObjectFactory.createObject(Sample.class.getName(),
        Collections.<Class<?>>emptyList(), Collections.<Object>emptyList());

    assertEquals(Sample.class, result.getClass());
  }

  @Test
  public void createObjectWithExplicitObjectConstructor() {
    List<Class<?>> paramTypes = Arrays.<Class<?>>asList(Object.class);
    List<Object> params = Arrays.<Object>asList("value");

    Object result = ObjectFactory.createObject(Sample.class.getName(), paramTypes, params);

    assertNull(result);
  }
}
