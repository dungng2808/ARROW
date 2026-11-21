package org.sunbird.learner.util;

import org.junit.Test;
import org.sunbird.common.models.util.JsonKey;

import java.util.Map;

import static org.junit.Assert.*;

public class UserFlagUtil_ArrowTest {

    @Test
    public void testGetFlagValue_PhoneVerifiedTrue() {
        int result = UserFlagUtil.getFlagValue(UserFlagEnum.PHONE_VERIFIED.getUserFlagType(), true);
        assertEquals(UserFlagEnum.PHONE_VERIFIED.getUserFlagValue(), result);
    }

    @Test
    public void testGetFlagValue_PhoneVerifiedFalse() {
        int result = UserFlagUtil.getFlagValue(UserFlagEnum.PHONE_VERIFIED.getUserFlagType(), false);
        assertEquals(0, result);
    }

    @Test
    public void testGetFlagValue_EmailVerifiedTrue() {
        int result = UserFlagUtil.getFlagValue(UserFlagEnum.EMAIL_VERIFIED.getUserFlagType(), true);
        assertEquals(UserFlagEnum.EMAIL_VERIFIED.getUserFlagValue(), result);
    }

    @Test
    public void testGetFlagValue_EmailVerifiedFalse() {
        int result = UserFlagUtil.getFlagValue(UserFlagEnum.EMAIL_VERIFIED.getUserFlagType(), false);
        assertEquals(0, result);
    }

    @Test
    public void testGetFlagValue_StateValidatedTrue() {
        int result = UserFlagUtil.getFlagValue(UserFlagEnum.STATE_VALIDATED.getUserFlagType(), true);
        assertEquals(UserFlagEnum.STATE_VALIDATED.getUserFlagValue(), result);
    }

    @Test
    public void testGetFlagValue_StateValidatedFalse() {
        int result = UserFlagUtil.getFlagValue(UserFlagEnum.STATE_VALIDATED.getUserFlagType(), false);
        assertEquals(0, result);
    }

    @Test
    public void testGetFlagValue_InvalidFlagType() {
        int result = UserFlagUtil.getFlagValue("invalid_flag", true);
        assertEquals(0, result);
    }

    @Test
    public void testAssignUserFlagValues_NoFlagsSet() {
        Map<String, Boolean> result = UserFlagUtil.assignUserFlagValues(0);
        assertNotNull(result);
        assertFalse(result.get(JsonKey.EMAIL_VERIFIED));
        assertFalse(result.get(JsonKey.PHONE_VERIFIED));
        assertFalse(result.get(JsonKey.STATE_VALIDATED));
    }

    @Test
    public void testAssignUserFlagValues_PhoneVerifiedOnly() {
        int flagsValue = UserFlagEnum.PHONE_VERIFIED.getUserFlagValue();
        Map<String, Boolean> result = UserFlagUtil.assignUserFlagValues(flagsValue);
        assertNotNull(result);
        assertFalse(result.get(JsonKey.EMAIL_VERIFIED));
        assertTrue(result.get(JsonKey.PHONE_VERIFIED));
        assertFalse(result.get(JsonKey.STATE_VALIDATED));
    }

    @Test
    public void testAssignUserFlagValues_EmailVerifiedOnly() {
        int flagsValue = UserFlagEnum.EMAIL_VERIFIED.getUserFlagValue();
        Map<String, Boolean> result = UserFlagUtil.assignUserFlagValues(flagsValue);
        assertNotNull(result);
        assertTrue(result.get(JsonKey.EMAIL_VERIFIED));
        assertFalse(result.get(JsonKey.PHONE_VERIFIED));
        assertFalse(result.get(JsonKey.STATE_VALIDATED));
    }

    @Test
    public void testAssignUserFlagValues_StateValidatedOnly() {
        int flagsValue = UserFlagEnum.STATE_VALIDATED.getUserFlagValue();
        Map<String, Boolean> result = UserFlagUtil.assignUserFlagValues(flagsValue);
        assertNotNull(result);
        assertFalse(result.get(JsonKey.EMAIL_VERIFIED));
        assertFalse(result.get(JsonKey.PHONE_VERIFIED));
        assertTrue(result.get(JsonKey.STATE_VALIDATED));
    }

    @Test
    public void testAssignUserFlagValues_AllFlagsSet() {
        int flagsValue = UserFlagEnum.PHONE_VERIFIED.getUserFlagValue() |
                         UserFlagEnum.EMAIL_VERIFIED.getUserFlagValue() |
                         UserFlagEnum.STATE_VALIDATED.getUserFlagValue();
        Map<String, Boolean> result = UserFlagUtil.assignUserFlagValues(flagsValue);
        assertNotNull(result);
        assertTrue(result.get(JsonKey.EMAIL_VERIFIED));
        assertTrue(result.get(JsonKey.PHONE_VERIFIED));
        assertTrue(result.get(JsonKey.STATE_VALIDATED));
    }

    @Test
    public void testAssignUserFlagValues_MixedFlags() {
        int flagsValue = UserFlagEnum.PHONE_VERIFIED.getUserFlagValue() |
                         UserFlagEnum.STATE_VALIDATED.getUserFlagValue();
        Map<String, Boolean> result = UserFlagUtil.assignUserFlagValues(flagsValue);
        assertNotNull(result);
        assertFalse(result.get(JsonKey.EMAIL_VERIFIED));
        assertTrue(result.get(JsonKey.PHONE_VERIFIED));
        assertTrue(result.get(JsonKey.STATE_VALIDATED));
    }
}
