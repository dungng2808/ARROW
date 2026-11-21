package org.sunbird.learner.util;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserFlagUtil_ArrowTest {

  @Test
  public void getFlagValueReturnsZeroForUnknownFlag() {
    assertEquals(0, UserFlagUtil.getFlagValue("unknown", true));
  }

  @Test
  public void getFlagValueReturnsZeroWhenFlagIsDisabled() {
    assertEquals(0, UserFlagUtil.getFlagValue("phoneVerified", false));
  }

  @Test
  public void getFlagValueReturnsPhoneFlagValueWhenEnabled() {
    assertEquals(
        UserFlagEnum.PHONE_VERIFIED.getUserFlagValue(),
        UserFlagUtil.getFlagValue(UserFlagEnum.PHONE_VERIFIED.getUserFlagType(), true));
  }

  @Test
  public void getFlagValueReturnsEmailFlagValueWhenEnabled() {
    assertEquals(
        UserFlagEnum.EMAIL_VERIFIED.getUserFlagValue(),
        UserFlagUtil.getFlagValue(UserFlagEnum.EMAIL_VERIFIED.getUserFlagType(), true));
  }

  @Test
  public void getFlagValueReturnsStateValidatedFlagValueWhenEnabled() {
    assertEquals(
        UserFlagEnum.STATE_VALIDATED.getUserFlagValue(),
        UserFlagUtil.getFlagValue(UserFlagEnum.STATE_VALIDATED.getUserFlagType(), true));
  }

  @Test(expected = NullPointerException.class)
  public void getFlagValueThrowsForNullFlagType() {
    UserFlagUtil.getFlagValue(null, true);
  }

  @Test
  public void assignUserFlagValuesReturnsAllFalseForZero() {
    Map<String, Boolean> expected = new HashMap<>();
    expected.put(org.sunbird.common.models.util.JsonKey.EMAIL_VERIFIED, false);
    expected.put(org.sunbird.common.models.util.JsonKey.PHONE_VERIFIED, false);
    expected.put(org.sunbird.common.models.util.JsonKey.STATE_VALIDATED, false);

    assertEquals(expected, UserFlagUtil.assignUserFlagValues(0));
  }

  @Test
  public void assignUserFlagValuesReturnsEnabledFlags() {
    int flags =
        UserFlagEnum.PHONE_VERIFIED.getUserFlagValue()
            | UserFlagEnum.STATE_VALIDATED.getUserFlagValue();
    Map<String, Boolean> expected = new HashMap<>();
    expected.put(org.sunbird.common.models.util.JsonKey.EMAIL_VERIFIED, false);
    expected.put(org.sunbird.common.models.util.JsonKey.PHONE_VERIFIED, true);
    expected.put(org.sunbird.common.models.util.JsonKey.STATE_VALIDATED, true);

    assertEquals(expected, UserFlagUtil.assignUserFlagValues(flags));
  }

  @Test
  public void assignUserFlagValuesReturnsAllTrueWhenAllBitsAreSet() {
    int flags =
        UserFlagEnum.PHONE_VERIFIED.getUserFlagValue()
            | UserFlagEnum.EMAIL_VERIFIED.getUserFlagValue()
            | UserFlagEnum.STATE_VALIDATED.getUserFlagValue();
    Map<String, Boolean> expected = new HashMap<>();
    expected.put(org.sunbird.common.models.util.JsonKey.EMAIL_VERIFIED, true);
    expected.put(org.sunbird.common.models.util.JsonKey.PHONE_VERIFIED, true);
    expected.put(org.sunbird.common.models.util.JsonKey.STATE_VALIDATED, true);

    assertEquals(expected, UserFlagUtil.assignUserFlagValues(flags));
  }
}
