package org.fissore.jrecordbind.converters;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StringConverter_ArrowTest {

  @Test
  public void convertReturnsTheInputString() {
    StringConverter converter = new StringConverter();

    Object result = converter.convert("hello");

    assertEquals("hello", result);
  }

  @Test
  public void convertReturnsNullForNullInput() {
    StringConverter converter = new StringConverter();

    Object result = converter.convert(null);

    assertNull(result);
  }

  @Test
  public void toStringReturnsTheValueString() {
    StringConverter converter = new StringConverter();

    String result = converter.toString(Integer.valueOf(42));

    assertEquals("42", result);
  }

  @Test
  public void toStringReturnsEmptyStringForNull() {
    StringConverter converter = new StringConverter();

    String result = converter.toString(null);

    assertEquals("", result);
  }
}
