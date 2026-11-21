package com.redis.utils;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MythTime_ArrowTest {

  @Test
  public void getTimeReturnsFormattedTime() {
    String result = MythTime.getTime();
    assertTrue(result.matches("\\d{2}:\\d{2}:\\d{2}:\\d{2}"));
  }

  @Test
  public void getDateTimeReturnsFormattedDateAndTime() {
    String result = MythTime.getDateTime();
    assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{2}"));
  }

  @Test
  public void getDateReturnsFormattedDate() {
    String result = MythTime.getDate();
    assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
  }
}
