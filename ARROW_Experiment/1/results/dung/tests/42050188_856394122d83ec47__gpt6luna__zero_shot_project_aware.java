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
    private final String value;

    public Sample() {
      this.value = "default";
    }

    public Sample(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  public static class Parent {
    private final Object value;

    public Parent(Object value) {
      this.value = value;
    }

    public Object getValue() {
      return value;
    }
  }

  @Test
  public void createObjectWithoutParameters() {
    Object result = ObjectFactory.createObject(Sample.class.getName());
    assertEquals("default", ((Sample) result).getValue());
  }

  @Test
  public void createObjectWithInferredParameterType() {
    Object result = ObjectFactory.createObject(Sample.class.getName(),
        Collections.<Object>singletonList("hello"));
    assertEquals("hello", ((Sample) result).getValue());
  }

  @Test
  public void createObjectWithAssignableConstructorParameter() {
    Object value = new StringBuilder("value");
    Object result = ObjectFactory.createObject(Parent.class.getName(),
        Collections.<Class<?>>singletonList(CharSequence.class),
        Collections.<Object>singletonList(value));
    assertSame(value, ((Parent) result).getValue());
  }

  @Test
  public void createObjectForMissingClassReturnsNull() {
    assertNull(ObjectFactory.createObject("missing.example.NoSuchClass"));
  }

  @Test
  public void getTypesReturnsRuntimeTypes() {
    List<Object> params = Arrays.<Object>asList("text", Integer.valueOf(3));
    List<Class<?>> types = ObjectFactory.getTypes(params);
    assertEquals(Arrays.<Class<?>>asList(String.class, Integer.class), types);
  }

  @Test(expected = NullPointerException.class)
  public void getTypesRejectsNullElements() {
    List<Object> params = new ArrayList<Object>();
    params.add(null);
    ObjectFactory.getTypes(params);
  }
}
