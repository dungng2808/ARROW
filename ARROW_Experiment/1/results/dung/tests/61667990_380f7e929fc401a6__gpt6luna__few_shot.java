package com.intuit.wasabi.authenticationobjects;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LoginCredentials_ArrowTest {

    @Test
    public void builderSetsAllFields() {
        UserInfo.Username username = new UserInfo.Username("user@example.com");
        LoginCredentials credentials = LoginCredentials.withUsername(username)
                .withPassword("secret")
                .withNamespaceId("QBO")
                .build();

        assertEquals(username, credentials.getUsername());
        assertEquals("secret", credentials.getPassword());
        assertEquals("QBO", credentials.getNamespaceId());
    }

    @Test
    public void builderAllowsOptionalFieldsToBeOmitted() {
        UserInfo.Username username = new UserInfo.Username("user@example.com");
        LoginCredentials credentials = LoginCredentials.withUsername(username).build();

        assertEquals(username, credentials.getUsername());
        assertNull(credentials.getPassword());
        assertNull(credentials.getNamespaceId());
    }

    @Test
    public void settersUpdateFields() {
        UserInfo.Username username = new UserInfo.Username("user@example.com");
        LoginCredentials credentials = LoginCredentials.withUsername(username).build();
        UserInfo.Username updatedUsername = new UserInfo.Username("other@example.com");

        credentials.setUsername(updatedUsername);
        credentials.setPassword("new-secret");
        credentials.setNamespaceId("QBO");

        assertEquals(updatedUsername, credentials.getUsername());
        assertEquals("new-secret", credentials.getPassword());
        assertEquals("QBO", credentials.getNamespaceId());
    }

    @Test
    public void equalCredentialsHaveEqualHashCodes() {
        UserInfo.Username username = new UserInfo.Username("user@example.com");
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
    public void credentialsWithDifferentPasswordsAreNotEqual() {
        UserInfo.Username username = new UserInfo.Username("user@example.com");
        LoginCredentials first = LoginCredentials.withUsername(username).withPassword("secret").build();
        LoginCredentials second = LoginCredentials.withUsername(username).withPassword("different").build();

        assertFalse(first.equals(second));
    }

    @Test
    public void equalsHandlesNullAndDifferentTypes() {
        LoginCredentials credentials = LoginCredentials.withUsername(
                new UserInfo.Username("user@example.com")).build();

        assertFalse(credentials.equals(null));
        assertFalse(credentials.equals("credentials"));
    }

    @Test
    public void toStringIncludesUsernameAndNamespaceButNotPassword() {
        UserInfo.Username username = new UserInfo.Username("user@example.com");
        LoginCredentials credentials = LoginCredentials.withUsername(username)
                .withPassword("secret")
                .withNamespaceId("QBO")
                .build();

        String result = credentials.toString();

        assertNotNull(result);
        assertTrue(result.contains("username=" + username));
        assertTrue(result.contains("namespaceId=QBO"));
        assertFalse(result.contains("secret"));
    }

    @Test(expected = NullPointerException.class)
    public void builderRejectsNullUsername() {
        LoginCredentials.withUsername(null);
    }
}
