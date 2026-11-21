package com.redis.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class MythTime_ArrowTest {

  @Test
  public void getTimeReturnsTimeWithExpectedFormat() {
    String result = MythTime.getTime();

    assertNotNull(result);
    assertEquals(11, result.length());
    assertEquals(':', result.charAt(2));
    assertEquals(':', result.charAt(5));
    assertEquals(':', result.charAt(8));
  }

  @Test
  public void getDateTimeReturnsDateTimeWithExpectedFormat() {
    String result = MythTime.getDateTime();

    assertNotNull(result);
    assertEquals(22, result.length());
    assertEquals('-', result.charAt(4));
    assertEquals('-', result.charAt(7));
    assertEquals(' ', result.charAt(10));
    assertEquals(':', result.charAt(13));
    assertEquals(':', result.charAt(16));
    assertEquals(':', result.charAt(19));
  }

  @Test
  public void getDateReturnsDateWithExpectedFormat() {
    String result = MythTime.getDate();

    assertNotNull(result);
    assertEquals(10, result.length());
    assertEquals('-', result.charAt(4));
    assertEquals('-', result.charAt(7));
  }
}
