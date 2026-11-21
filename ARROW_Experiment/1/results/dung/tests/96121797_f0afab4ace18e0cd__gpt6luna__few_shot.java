package org.sunbird.learner.util;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserFlagUtil_ArrowTest {

  @Test
  public void getFlagValueReturnsZeroForDisabledPhoneFlag() {
    assertEquals(0, UserFlagUtil.getFlagValue("phoneVerified", false));
  }

  @Test
  public void getFlagValueReturnsZeroForUnknownFlag() {
    assertEquals(0, UserFlagUtil.getFlagValue("unknown", true));
  }

  @Test
  public void getFlagValueReturnsZeroForNullFlag() {
    try {
      UserFlagUtil.getFlagValue(null, true);
    } catch (NullPointerException expected) {
      return;
    }
    throw new AssertionError("Expected NullPointerException");
  }

  @Test
  public void assignUserFlagValuesReturnsAllFalseForZero() {
    Map<String, Boolean> expected = new HashMap<>();
    expected.put("emailVerified", false);
    expected.put("phoneVerified", false);
    expected.put("stateValidated", false);

    assertEquals(expected, UserFlagUtil.assignUserFlagValues(0));
  }

  @Test
  public void assignUserFlagValuesReturnsFalseForNegativeOne() {
    Map<String, Boolean> expected = new HashMap<>();
    expected.put("emailVerified", true);
    expected.put("phoneVerified", true);
    expected.put("stateValidated", true);

    assertEquals(expected, UserFlagUtil.assignUserFlagValues(-1));
  }
}
