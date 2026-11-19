package com.intuit.wasabi.authenticationobjects;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Test;

public class LoginCredentials_ArrowTest {

    @Test
    public void testBuilderWithUsername() {
        UserInfo.Username username = new UserInfo.Username("testUser");
        LoginCredentials credentials = LoginCredentials.withUsername(username).build();
        
        assertThat(credentials, notNullValue());
        assertThat(credentials.getUsername(), equalTo(username));
        assertThat(credentials.getPassword(), equalTo(null));
        assertThat(credentials.getNamespaceId(), equalTo(null));
    }

    @Test
    public void testBuilderWithUsernameAndPassword() {
        UserInfo.Username username = new UserInfo.Username("testUser");
        String password = "testPassword";
        LoginCredentials credentials = LoginCredentials.withUsername(username).withPassword(password).build();
        
        assertThat(credentials, notNullValue());
        assertThat(credentials.getUsername(), equalTo(username));
        assertThat(credentials.getPassword(), equalTo(password));
        assertThat(credentials.getNamespaceId(), equalTo(null));
    }

    @Test
    public void testBuilderWithUsernameAndPasswordAndNamespaceId() {
        UserInfo.Username username = new UserInfo.Username("testUser");
        String password = "testPassword";
        String namespaceId = "testNamespace";
        LoginCredentials credentials = LoginCredentials.withUsername(username).withPassword(password).withNamespaceId(namespaceId).build();
        
        assertThat(credentials, notNullValue());
        assertThat(credentials.getUsername(), equalTo(username));
        assertThat(credentials.getPassword(), equalTo(password));
        assertThat(credentials.getNamespaceId(), equalTo(namespaceId));
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
        
        assertThat(credentials.getUsername(), equalTo(username));
        assertThat(credentials.getPassword(), equalTo(password));
        assertThat(credentials.getNamespaceId(), equalTo(namespaceId));
    }

    @Test
    public void testHashCode() {
        UserInfo.Username username = new UserInfo.Username("testUser");
        String password = "testPassword";
        String namespaceId = "testNamespace";
        
        LoginCredentials credentials1 = LoginCredentials.withUsername(username).withPassword(password).withNamespaceId(namespaceId).build();
        LoginCredentials credentials2 = LoginCredentials.withUsername(username).withPassword(password).withNamespaceId(namespaceId).build();
        
        int hash1 = credentials1.hashCode();
        int hash2 = credentials2.hashCode();
        
        assertThat(hash1, equalTo(hash2));
    }

    @Test
    public void testEquals() {
        UserInfo.Username username = new UserInfo.Username("testUser");
        String password = "testPassword";
        String namespaceId = "testNamespace";
        
        LoginCredentials credentials1 = LoginCredentials.withUsername(username).withPassword(password).withNamespaceId(namespaceId).build();
        LoginCredentials credentials2 = LoginCredentials.withUsername(username).withPassword(password).withNamespaceId(namespaceId).build();
        
        assertTrue(EqualsBuilder.reflectionEquals(credentials1, credentials2));
    }

    @Test
    public void testToString() {
        UserInfo.Username username = new UserInfo.Username("testUser");
        String namespaceId = "testNamespace";
        
        LoginCredentials credentials = LoginCredentials.withUsername(username).withNamespaceId(namespaceId).build();
        String toStringResult = credentials.toString();
        
        assertThat(toStringResult, notNullValue());
        assertTrue(toStringResult.contains("testUser"));
        assertTrue(toStringResult.contains("testNamespace"));
    }
}
