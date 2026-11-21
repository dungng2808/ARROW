package org.fissore.jrecordbind.converters;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StringConverter_ArrowTest {

  @Test
  public void convertReturnsInputString() {
    StringConverter converter = new StringConverter();
    assertEquals("sample", converter.convert("sample"));
  }

  @Test
  public void convertReturnsNullForNullInput() {
    StringConverter converter = new StringConverter();
    assertNull(converter.convert(null));
  }

  @Test
  public void toStringReturnsValueString() {
    StringConverter converter = new StringConverter();
    assertEquals("42", converter.toString(Integer.valueOf(42)));
  }

  @Test
  public void toStringReturnsEmptyStringForNull() {
    StringConverter converter = new StringConverter();
    assertEquals("", converter.toString(null));
  }
}
