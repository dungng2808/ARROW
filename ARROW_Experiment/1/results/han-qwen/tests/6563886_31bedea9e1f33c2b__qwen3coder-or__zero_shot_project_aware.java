package org.fissore.jrecordbind.converters;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringConverter_ArrowTest {

  @Test
  public void convertReturnsInputString() {
    StringConverter converter = new StringConverter();
    String input = "test string";
    Object result = converter.convert(input);
    assertEquals(input, result);
  }

  @Test
  public void toStringReturnsEmptyStringForNull() {
    StringConverter converter = new StringConverter();
    String result = converter.toString(null);
    assertEquals("", result);
  }

  @Test
  public void toStringReturnsStringRepresentationOfObject() {
    StringConverter converter = new StringConverter();
    Object input = new Object() {
      @Override
      public String toString() {
        return "object representation";
      }
    };
    String result = converter.toString(input);
    assertEquals("object representation", result);
  }

  @Test
  public void toStringReturnsStringRepresentationOfString() {
    StringConverter converter = new StringConverter();
    String input = "test string";
    String result = converter.toString(input);
    assertEquals("test string", result);
  }
}
