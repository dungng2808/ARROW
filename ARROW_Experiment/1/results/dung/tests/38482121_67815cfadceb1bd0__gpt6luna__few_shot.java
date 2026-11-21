package org.vx68k.bitbucket.webapp;

import org.junit.Test;
import org.vx68k.bitbucket.BitbucketAccount;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;

public class UserInfo_ArrowTest {

    @Test
    public void constructorStoresSessionUserAndStartsWithEmptyName() {
        UserInfo userInfo = new UserInfo(null);

        assertNull(userInfo.getSessionUser());
        assertEquals("", userInfo.getName());
        assertNull(userInfo.getUser());
        assertFalse(userInfo.isFound());
    }

    @Test
    public void setNameStoresValue() {
        UserInfo userInfo = new UserInfo(null);

        userInfo.setName("alice");

        assertEquals("alice", userInfo.getName());
    }

    @Test
    public void lookupWithEmptyNameClearsUserAndReturnsNull() {
        UserInfo userInfo = new UserInfo(null);

        Object result = userInfo.lookUp();

        assertNull(result);
        assertNull(userInfo.getUser());
        assertFalse(userInfo.isFound());
    }
}
