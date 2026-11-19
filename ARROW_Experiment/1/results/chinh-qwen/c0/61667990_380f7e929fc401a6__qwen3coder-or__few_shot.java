package com.intuit.wasabi.authenticationobjects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class LoginCredentials_ArrowTest {

    @Test
    public void testBuilderWithUsername() {
        UserInfo.Username username = new UserInfo.Username("testUser");
        LoginCredentials credentials = LoginCredentials.withUsername(username)
                .build();
        
        assertThat(credentials.getUsername(), is(username));
        assertThat(credentials.getPassword(), is(nullValue()));
        assertThat(credentials.getNamespaceId(), is(nullValue()));
    }

    @Test
    public void testBuilderWithUsernameAndPassword() {
        UserInfo.Username username = new UserInfo.Username("testUser");
        LoginCredentials credentials = LoginCredentials.withUsername(username)
                .withPassword("testPassword")
                .build();
        
        assertThat(credentials.getUsername(), is(username));
        assertThat(credentials.getPassword(), is("testPassword"));
        assertThat(credentials.getNamespaceId(), is(nullValue()));
    }

    @Test
    public void testBuilderWithUsernameAndPasswordAndNamespaceId() {
        UserInfo.Username username = new UserInfo.Username("testUser");
        LoginCredentials credentials = LoginCredentials.withUsername(username)
                .withPassword("testPassword")
                .withNamespaceId("testNamespace")
                .build();
        
        assertThat(credentials.getUsername(), is(username));
        assertThat(credentials.getPassword(), is("testPassword"));
        assertThat(credentials.getNamespaceId(), is("testNamespace"));
    }

    @Test
    public void testSetters() {
        LoginCredentials credentials = new LoginCredentials();
        UserInfo.Username username = new UserInfo.Username("testUser");
        String password = "testPassword";
        String namespaceId = "testNamespace";
        
        credentials.setUsername(username);
        credentials.setPassword(password);
        credentials.setNamespaceId(namespaceId);
        
        assertThat(credentials.getUsername(), is(username));
        assertThat(credentials.getPassword(), is(password));
        assertThat(credentials.getNamespaceId(), is(namespaceId));
    }

    @Test
    public void testToString() {
        UserInfo.Username username = new UserInfo.Username("testUser");
        LoginCredentials credentials = LoginCredentials.withUsername(username)
                .withPassword("testPassword")
                .withNamespaceId("testNamespace")
                .build();
        
        String toStringResult = credentials.toString();
        assertNotNull(toStringResult);
        assertThat(toStringResult.contains("testUser"), is(true));
        assertThat(toStringResult.contains("testNamespace"), is(true));
    }

    @Test
    public void testHashCode() {
        UserInfo.Username username1 = new UserInfo.Username("testUser");
        UserInfo.Username username2 = new UserInfo.Username("testUser");
        LoginCredentials credentials1 = LoginCredentials.withUsername(username1)
                .withPassword("testPassword")
                .withNamespaceId("testNamespace")
                .build();
        LoginCredentials credentials2 = LoginCredentials.withUsername(username2)
                .withPassword("testPassword")
                .withNamespaceId("testNamespace")
                .build();
        
        assertEquals(credentials1.hashCode(), credentials2.hashCode());
    }

    @Test
    public void testEquals() {
        UserInfo.Username username1 = new UserInfo.Username("testUser");
        UserInfo.Username username2 = new UserInfo.Username("testUser");
        LoginCredentials credentials1 = LoginCredentials.withUsername(username1)
                .withPassword("testPassword")
                .withNamespaceId("testNamespace")
                .build();
        LoginCredentials credentials2 = LoginCredentials.withUsername(username2)
                .withPassword("testPassword")
                .withNamespaceId("testNamespace")
                .build();
        
        assertEquals(credentials1, credentials2);
        assertEquals(credentials1, credentials1);
        assertNotEquals(credentials1, null);
        assertNotEquals(credentials1, "not a LoginCredentials");
    }
}
