package de.monticore.generating.templateengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ObjectFactory_ArrowTest {

  public static class PublicConstructorTarget {
    private final String value;

    public PublicConstructorTarget(String value) {
      this.value = value;
    }
  }

  public static class SupertypeConstructorTarget {
    private final CharSequence value;

    public SupertypeConstructorTarget(CharSequence value) {
      this.value = value;
    }
  }

  @Test
  public void createsObjectWithNoArguments() {
    Object result = ObjectFactory.createObject(StringBuilder.class.getName());
    assertEquals("", result.toString());
  }

  @Test
  public void createsObjectWithInferredParameterTypes() {
    Object result = ObjectFactory.createObject(
        PublicConstructorTarget.class.getName(), Arrays.<Object>asList("value"));
    assertEquals("value", ((PublicConstructorTarget) result).value);
  }

  @Test
  public void createsObjectUsingAssignableConstructorParameter() {
    Object result = ObjectFactory.createObject(
        SupertypeConstructorTarget.class.getName(),
        Arrays.<Class<?>>asList(String.class),
        Arrays.<Object>asList("text"));
    assertEquals("text", ((SupertypeConstructorTarget) result).value.toString());
  }

  @Test
  public void returnsNullWhenClassCannotBeFound() {
    assertNull(ObjectFactory.createObject("missing.ObjectFactoryTarget"));
  }

  @Test
  public void returnsNullWhenNoConstructorMatches() {
    assertNull(ObjectFactory.createObject(
        PublicConstructorTarget.class.getName(),
        Arrays.<Class<?>>asList(Integer.class),
        Arrays.<Object>asList(Integer.valueOf(1))));
  }

  @Test
  public void infersTypesFromParameters() {
    List<Object> params = new ArrayList<Object>();
    params.add("text");
    params.add(Integer.valueOf(2));

    List<Class<?>> types = ObjectFactory.getTypes(params);

    assertEquals(Arrays.<Class<?>>asList(String.class, Integer.class), types);
  }

  @Test(expected = NullPointerException.class)
  public void inferringTypesFromNullParameterThrows() {
    List<Object> params = new ArrayList<Object>();
    params.add(null);
    ObjectFactory.getTypes(params);
  }
}
