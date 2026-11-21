package org.vx68k.bitbucket.webapp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserInfo_ArrowTest
{
    @Test
    public void constructorStoresSessionUser()
    {
        UserInfo userInfo = new UserInfo(null);

        assertNull(userInfo.getSessionUser());
        assertEquals("", userInfo.getName());
        assertNull(userInfo.getUser());
        assertFalse(userInfo.isFound());
    }

    @Test
    public void setNameUpdatesName()
    {
        UserInfo userInfo = new UserInfo(null);

        userInfo.setName("alice");

        assertEquals("alice", userInfo.getName());
    }

    @Test
    public void lookUpWithEmptyNameClearsUserAndReturnsNull()
    {
        UserInfo userInfo = new UserInfo(null);
        userInfo.setName("");

        Object result = userInfo.lookUp();

        assertNull(result);
        assertNull(userInfo.getUser());
        assertFalse(userInfo.isFound());
    }

    @Test
    public void setNameAcceptsNull()
    {
        UserInfo userInfo = new UserInfo(null);

        userInfo.setName(null);

        assertNull(userInfo.getName());
    }
}
