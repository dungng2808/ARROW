package org.vx68k.bitbucket.webapp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UserInfo_ArrowTest
{
    @Test
    public void constructorStoresSessionUser()
    {
        UserInfo userInfo = new UserInfo(null);

        assertNull(userInfo.getSessionUser());
        assertNull(userInfo.getName());
        assertNull(userInfo.getUser());
        assertFalse(userInfo.isFound());
    }

    @Test
    public void setNameStoresValue()
    {
        UserInfo userInfo = new UserInfo(null);
        userInfo.setName("alice");

        org.junit.Assert.assertEquals("alice", userInfo.getName());
    }

    @Test
    public void lookUpWithEmptyNameClearsUserAndReturnsNull()
    {
        UserInfo userInfo = new UserInfo(null);

        assertNull(userInfo.lookUp());
        assertNull(userInfo.getUser());
        assertFalse(userInfo.isFound());
    }
}
