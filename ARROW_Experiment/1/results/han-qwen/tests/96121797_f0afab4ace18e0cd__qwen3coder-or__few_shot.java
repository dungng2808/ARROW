package org.sunbird.learner.util;

import org.junit.Test;
import org.sunbird.common.models.util.JsonKey;

import java.util.Map;

import static org.junit.Assert.*;

public class UserFlagUtil_ArrowTest {

    @Test
    public void testGetFlagValuePhoneVerifiedEnabled() {
        int result = UserFlagUtil.getFlagValue(UserFlagEnum.PHONE_VERIFIED.getUserFlagType(), true);
        assertEquals(UserFlagEnum.PHONE_VERIFIED.getUserFlagValue(), result);
    }

    @Test
    public void testGetFlagValuePhoneVerifiedDisabled() {
        int result = UserFlagUtil.getFlagValue(UserFlagEnum.PHONE_VERIFIED.getUserFlagType(), false);
        assertEquals(0, result);
    }

    @Test
    public void testGetFlagValueEmailVerifiedEnabled() {
        int result = UserFlagUtil.getFlagValue(UserFlagEnum.EMAIL_VERIFIED.getUserFlagType(), true);
        assertEquals(UserFlagEnum.EMAIL_VERIFIED.getUserFlagValue(), result);
    }

    @Test
    public void testGetFlagValueEmailVerifiedDisabled() {
        int result = UserFlagUtil.getFlagValue(UserFlagEnum.EMAIL_VERIFIED.getUserFlagType(), false);
        assertEquals(0, result);
    }

    @Test
    public void testGetFlagValueStateValidatedEnabled() {
        int result = UserFlagUtil.getFlagValue(UserFlagEnum.STATE_VALIDATED.getUserFlagType(), true);
        assertEquals(UserFlagEnum.STATE_VALIDATED.getUserFlagValue(), result);
    }

    @Test
    public void testGetFlagValueStateValidatedDisabled() {
        int result = UserFlagUtil.getFlagValue(UserFlagEnum.STATE_VALIDATED.getUserFlagType(), false);
        assertEquals(0, result);
    }

    @Test
    public void testGetFlagValueUnknownFlag() {
        int result = UserFlagUtil.getFlagValue("unknown_flag", true);
        assertEquals(0, result);
    }

    @Test
    public void testAssignUserFlagValuesAllFlagsSet() {
        int flagsValue = UserFlagEnum.PHONE_VERIFIED.getUserFlagValue() |
                         UserFlagEnum.EMAIL_VERIFIED.getUserFlagValue() |
                         UserFlagEnum.STATE_VALIDATED.getUserFlagValue();
        
        Map<String, Boolean> result = UserFlagUtil.assignUserFlagValues(flagsValue);
        
        assertTrue(result.get(JsonKey.PHONE_VERIFIED));
        assertTrue(result.get(JsonKey.EMAIL_VERIFIED));
        assertTrue(result.get(JsonKey.STATE_VALIDATED));
    }

    @Test
    public void testAssignUserFlagValuesNoFlagsSet() {
        int flagsValue = 0;
        
        Map<String, Boolean> result = UserFlagUtil.assignUserFlagValues(flagsValue);
        
        assertFalse(result.get(JsonKey.PHONE_VERIFIED));
        assertFalse(result.get(JsonKey.EMAIL_VERIFIED));
        assertFalse(result.get(JsonKey.STATE_VALIDATED));
    }

    @Test
    public void testAssignUserFlagValuesOnlyPhoneVerified() {
        int flagsValue = UserFlagEnum.PHONE_VERIFIED.getUserFlagValue();
        
        Map<String, Boolean> result = UserFlagUtil.assignUserFlagValues(flagsValue);
        
        assertTrue(result.get(JsonKey.PHONE_VERIFIED));
        assertFalse(result.get(JsonKey.EMAIL_VERIFIED));
        assertFalse(result.get(JsonKey.STATE_VALIDATED));
    }

    @Test
    public void testAssignUserFlagValuesOnlyEmailVerified() {
        int flagsValue = UserFlagEnum.EMAIL_VERIFIED.getUserFlagValue();
        
        Map<String, Boolean> result = UserFlagUtil.assignUserFlagValues(flagsValue);
        
        assertFalse(result.get(JsonKey.PHONE_VERIFIED));
        assertTrue(result.get(JsonKey.EMAIL_VERIFIED));
        assertFalse(result.get(JsonKey.STATE_VALIDATED));
    }

    @Test
    public void testAssignUserFlagValuesOnlyStateValidated() {
        int flagsValue = UserFlagEnum.STATE_VALIDATED.getUserFlagValue();
        
        Map<String, Boolean> result = UserFlagUtil.assignUserFlagValues(flagsValue);
        
        assertFalse(result.get(JsonKey.PHONE_VERIFIED));
        assertFalse(result.get(JsonKey.EMAIL_VERIFIED));
        assertTrue(result.get(JsonKey.STATE_VALIDATED));
    }

    @Test
    public void testAssignUserFlagValuesPartialFlagsSet() {
        int flagsValue = UserFlagEnum.PHONE_VERIFIED.getUserFlagValue() |
                         UserFlagEnum.EMAIL_VERIFIED.getUserFlagValue();
        
        Map<String, Boolean> result = UserFlagUtil.assignUserFlagValues(flagsValue);
        
        assertTrue(result.get(JsonKey.PHONE_VERIFIED));
        assertTrue(result.get(JsonKey.EMAIL_VERIFIED));
        assertFalse(result.get(JsonKey.STATE_VALIDATED));
    }
}
