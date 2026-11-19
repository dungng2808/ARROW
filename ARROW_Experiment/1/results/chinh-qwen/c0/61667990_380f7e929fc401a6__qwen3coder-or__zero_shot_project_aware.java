package com.intuit.wasabi.authenticationobjects;

import com.google.common.base.Preconditions;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class LoginCredentials_ArrowTest {

    @Test
    public void testBuilderWithUsername() {
        UserInfo.Username username = new UserInfo.Username("testUser");
        LoginCredentials credentials = LoginCredentials.withUsername(username).build();
        
        assertThat(credentials.getUsername(), is(username));
        assertThat(credentials.getPassword(), is(nullValue()));
        assertThat(credentials.getNamespaceId(), is(nullValue()));
    }

    @Test
    public void testBuilderWithUsernameAndPassword() {
        UserInfo.Username username = new UserInfo.Username("testUser");
        String password = "testPassword";
        LoginCredentials credentials = LoginCredentials.withUsername(username)
                .withPassword(password)
                .build();
        
        assertThat(credentials.getUsername(), is(username));
        assertThat(credentials.getPassword(), is(password));
        assertThat(credentials.getNamespaceId(), is(nullValue()));
    }

    @Test
    public void testBuilderWithUsernameAndPasswordAndNamespaceId() {
        UserInfo.Username username = new UserInfo.Username("testUser");
        String password = "testPassword";
        String namespaceId = "testNamespace";
        LoginCredentials credentials = LoginCredentials.withUsername(username)
                .withPassword(password)
                .withNamespaceId(namespaceId)
                .build();
        
        assertThat(credentials.getUsername(), is(username));
        assertThat(credentials.getPassword(), is(password));
        assertThat(credentials.getNamespaceId(), is(namespaceId));
    }

    @Test
    public void testGettersAndSetters() {
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
        String namespaceId = "testNamespace";
        LoginCredentials credentials = LoginCredentials.withUsername(username)
                .withNamespaceId(namespaceId)
                .build();
        
        String result = credentials.toString();
        assertThat(result, containsString("username=testUser"));
        assertThat(result, containsString("namespaceId=testNamespace"));
    }

    @Test
    public void testHashCode() {
        UserInfo.Username username = new UserInfo.Username("testUser");
        LoginCredentials credentials1 = LoginCredentials.withUsername(username).build();
        LoginCredentials credentials2 = LoginCredentials.withUsername(username).build();
        
        assertThat(credentials1.hashCode(), is(credentials2.hashCode()));
    }

    @Test
    public void testEquals() {
        UserInfo.Username username = new UserInfo.Username("testUser");
        LoginCredentials credentials1 = LoginCredentials.withUsername(username).build();
        LoginCredentials credentials2 = LoginCredentials.withUsername(username).build();
        
        assertThat(credentials1.equals(credentials2), is(true));
        assertThat(credentials1.equals(null), is(false));
        assertThat(credentials1.equals(new Object()), is(false));
    }

    @Test(expected = NullPointerException.class)
    public void testBuilderWithNullUsername() {
        LoginCredentials.withUsername(null);
    }

    @Test
    public void testBuilderReuse() {
        UserInfo.Username username = new UserInfo.Username("testUser");
        LoginCredentials.Builder builder = LoginCredentials.withUsername(username);
        
        LoginCredentials credentials1 = builder.build();
        LoginCredentials credentials2 = builder.build();
        
        assertNotNull(credentials1);
        assertNotNull(credentials2);
        // The second build should return a new instance
        assertThat(credentials1, not(sameInstance(credentials2)));
    }
}
