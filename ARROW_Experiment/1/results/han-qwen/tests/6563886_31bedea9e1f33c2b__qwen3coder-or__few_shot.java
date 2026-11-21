package org.fissore.jrecordbind.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class StringConverter_ArrowTest {

  @Test
  public void testConvert() {
    StringConverter converter = new StringConverter();
    String input = "test string";
    Object result = converter.convert(input);
    assertEquals(input, result);
  }

  @Test
  public void testToStringWithNull() {
    StringConverter converter = new StringConverter();
    String result = converter.toString(null);
    assertEquals("", result);
  }

  @Test
  public void testToStringWithObject() {
    StringConverter converter = new StringConverter();
    Object input = new Object() {
      @Override
      public String toString() {
        return "object string representation";
      }
    };
    String result = converter.toString(input);
    assertEquals("object string representation", result);
  }

  @Test
  public void testToStringWithString() {
    StringConverter converter = new StringConverter();
    String input = "test string";
    String result = converter.toString(input);
    assertEquals(input, result);
  }
}
