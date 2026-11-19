package org.sunbird.learner.util;

import org.junit.Test;
import org.sunbird.common.models.util.JsonKey;

import java.util.Map;

import static org.junit.Assert.*;

public class UserFlagUtil_ArrowTest {

    @Test
    public void testGetFlagValue_PhoneVerifiedTrue() {
        int result = UserFlagUtil.getFlagValue("phone_verified", true);
        assertEquals(1, result);
    }

    @Test
    public void testGetFlagValue_PhoneVerifiedFalse() {
        int result = UserFlagUtil.getFlagValue("phone_verified", false);
        assertEquals(0, result);
    }

    @Test
    public void testGetFlagValue_EmailVerifiedTrue() {
        int result = UserFlagUtil.getFlagValue("email_verified", true);
        assertEquals(2, result);
    }

    @Test
    public void testGetFlagValue_EmailVerifiedFalse() {
        int result = UserFlagUtil.getFlagValue("email_verified", false);
        assertEquals(0, result);
    }

    @Test
    public void testGetFlagValue_StateValidatedTrue() {
        int result = UserFlagUtil.getFlagValue("state_validated", true);
        assertEquals(4, result);
    }

    @Test
    public void testGetFlagValue_StateValidatedFalse() {
        int result = UserFlagUtil.getFlagValue("state_validated", false);
        assertEquals(0, result);
    }

    @Test
    public void testGetFlagValue_InvalidFlag() {
        int result = UserFlagUtil.getFlagValue("invalid_flag", true);
        assertEquals(0, result);
    }

    @Test
    public void testAssignUserFlagValues_Zero() {
        Map<String, Boolean> result = UserFlagUtil.assignUserFlagValues(0);
        assertNotNull(result);
        assertFalse(result.get(JsonKey.EMAIL_VERIFIED));
        assertFalse(result.get(JsonKey.PHONE_VERIFIED));
        assertFalse(result.get(JsonKey.STATE_VALIDATED));
    }

    @Test
    public void testAssignUserFlagValues_PhoneVerifiedOnly() {
        Map<String, Boolean> result = UserFlagUtil.assignUserFlagValues(1);
        assertNotNull(result);
        assertFalse(result.get(JsonKey.EMAIL_VERIFIED));
        assertTrue(result.get(JsonKey.PHONE_VERIFIED));
        assertFalse(result.get(JsonKey.STATE_VALIDATED));
    }

    @Test
    public void testAssignUserFlagValues_EmailVerifiedOnly() {
        Map<String, Boolean> result = UserFlagUtil.assignUserFlagValues(2);
        assertNotNull(result);
        assertTrue(result.get(JsonKey.EMAIL_VERIFIED));
        assertFalse(result.get(JsonKey.PHONE_VERIFIED));
        assertFalse(result.get(JsonKey.STATE_VALIDATED));
    }

    @Test
    public void testAssignUserFlagValues_StateValidatedOnly() {
        Map<String, Boolean> result = UserFlagUtil.assignUserFlagValues(4);
        assertNotNull(result);
        assertFalse(result.get(JsonKey.EMAIL_VERIFIED));
        assertFalse(result.get(JsonKey.PHONE_VERIFIED));
        assertTrue(result.get(JsonKey.STATE_VALIDATED));
    }

    @Test
    public void testAssignUserFlagValues_PhoneAndEmail() {
        Map<String, Boolean> result = UserFlagUtil.assignUserFlagValues(3);
        assertNotNull(result);
        assertTrue(result.get(JsonKey.EMAIL_VERIFIED));
        assertTrue(result.get(JsonKey.PHONE_VERIFIED));
        assertFalse(result.get(JsonKey.STATE_VALIDATED));
    }

    @Test
    public void testAssignUserFlagValues_AllFlags() {
        Map<String, Boolean> result = UserFlagUtil.assignUserFlagValues(7);
        assertNotNull(result);
        assertTrue(result.get(JsonKey.EMAIL_VERIFIED));
        assertTrue(result.get(JsonKey.PHONE_VERIFIED));
        assertTrue(result.get(JsonKey.STATE_VALIDATED));
    }
}
