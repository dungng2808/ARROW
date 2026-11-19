package com.intuit.wasabi.authenticationobjects;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class LoginCredentials_ArrowTest {

    @Test
    public void builderSetsAllFields() {
        UserInfo.Username username = new UserInfo.Username("user");
        LoginCredentials credentials = LoginCredentials.withUsername(username)
                .withPassword("secret")
                .withNamespaceId("QBO")
                .build();

        assertSame(username, credentials.getUsername());
        assertEquals("secret", credentials.getPassword());
        assertEquals("QBO", credentials.getNamespaceId());
    }

    @Test
    public void settersAndGettersHandleNullValues() {
        LoginCredentials credentials = LoginCredentials.withUsername(new UserInfo.Username("user")).build();

        credentials.setUsername(null);
        credentials.setPassword(null);
        credentials.setNamespaceId(null);

        assertNull(credentials.getUsername());
        assertNull(credentials.getPassword());
        assertNull(credentials.getNamespaceId());
    }

    @Test
    public void builderAllowsNullOptionalValues() {
        UserInfo.Username username = new UserInfo.Username("user");
        LoginCredentials credentials = LoginCredentials.withUsername(username)
                .withPassword(null)
                .withNamespaceId(null)
                .build();

        assertSame(username, credentials.getUsername());
        assertNull(credentials.getPassword());
        assertNull(credentials.getNamespaceId());
    }

    @Test(expected = NullPointerException.class)
    public void builderRejectsNullUsername() {
        LoginCredentials.withUsername(null);
    }

    @Test
    public void equalCredentialsHaveEqualHashCodes() {
        UserInfo.Username username = new UserInfo.Username("user");
        LoginCredentials first = LoginCredentials.withUsername(username)
                .withPassword("secret")
                .withNamespaceId("QBO")
                .build();
        LoginCredentials second = LoginCredentials.withUsername(username)
                .withPassword("secret")
                .withNamespaceId("QBO")
                .build();

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void credentialsWithDifferentPasswordAreNotEqual() {
        UserInfo.Username username = new UserInfo.Username("user");
        LoginCredentials first = LoginCredentials.withUsername(username).withPassword("one").build();
        LoginCredentials second = LoginCredentials.withUsername(username).withPassword("two").build();

        assertNotEquals(first, second);
    }

    @Test
    public void toStringIncludesUsernameAndNamespaceButNotPassword() {
        UserInfo.Username username = new UserInfo.Username("user");
        LoginCredentials credentials = LoginCredentials.withUsername(username)
                .withPassword("secret")
                .withNamespaceId("QBO")
                .build();

        String result = credentials.toString();

        assertEquals("LoginCredentials{username=" + username + ", namespaceId=QBO}", result);
        assertTrue(!result.contains("secret"));
    }
}
