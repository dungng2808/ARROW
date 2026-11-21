package org.sunbird.learner.util;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class UserFlagUtil_ArrowTest {

  @Test
  public void getFlagValueReturnsZeroForDisabledFlag() {
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
  public void assignUserFlagValuesReturnsFalseDefaultsForZero() {
    Map<String, Boolean> expected = new HashMap<>();
    expected.put(org.sunbird.common.models.util.JsonKey.EMAIL_VERIFIED, false);
    expected.put(org.sunbird.common.models.util.JsonKey.PHONE_VERIFIED, false);
    expected.put(org.sunbird.common.models.util.JsonKey.STATE_VALIDATED, false);

    assertEquals(expected, UserFlagUtil.assignUserFlagValues(0));
  }

  @Test
  public void assignUserFlagValuesReturnsTrueForAllFlags() {
    Map<String, Boolean> expected = new HashMap<>();
    expected.put(org.sunbird.common.models.util.JsonKey.EMAIL_VERIFIED, true);
    expected.put(org.sunbird.common.models.util.JsonKey.PHONE_VERIFIED, true);
    expected.put(org.sunbird.common.models.util.JsonKey.STATE_VALIDATED, true);

    assertEquals(expected, UserFlagUtil.assignUserFlagValues(
        UserFlagEnum.PHONE_VERIFIED.getUserFlagValue()
            | UserFlagEnum.EMAIL_VERIFIED.getUserFlagValue()
            | UserFlagEnum.STATE_VALIDATED.getUserFlagValue()));
  }
}
