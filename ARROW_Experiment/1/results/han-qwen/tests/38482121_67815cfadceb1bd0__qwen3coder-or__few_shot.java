package org.vx68k.bitbucket.webapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.junit.Before;
import org.junit.Test;
import org.vx68k.bitbucket.BitbucketAccount;
import org.vx68k.bitbucket.client.BitbucketClient;

public class UserInfo_ArrowTest {

    private SessionUser sessionUser;
    private BitbucketClient bitbucketClient;
    private UserInfo userInfo;

    @Before
    public void setUp() {
        sessionUser = mock(SessionUser.class);
        bitbucketClient = mock(BitbucketClient.class);
        when(sessionUser.getBitbucketClient()).thenReturn(bitbucketClient);
        userInfo = new UserInfo(sessionUser);
    }

    @Test
    public void testConstructor() {
        assertEquals(sessionUser, userInfo.getSessionUser());
    }

    @Test
    public void testGetName() {
        assertEquals("", userInfo.getName());
    }

    @Test
    public void testSetName() {
        userInfo.setName("testUser");
        assertEquals("testUser", userInfo.getName());
    }

    @Test
    public void testGetUser() {
        assertNull(userInfo.getUser());
    }

    @Test
    public void testIsFound() {
        assertFalse(userInfo.isFound());
    }

    @Test
    public void testLookUpEmptyName() {
        Object result = userInfo.lookUp();
        assertNull(result);
        assertFalse(userInfo.isFound());
        assertNull(userInfo.getUser());
    }

    @Test
    public void testLookUpUserNotFound() {
        userInfo.setName("nonExistentUser");
        
        // Mock the FacesContext and UIComponent for the message
        FacesContext facesContext = mock(FacesContext.class);
        UIComponent component = mock(UIComponent.class);
        
        // Mock static method calls
        FacesContext.setCurrentInstance(facesContext);
        when(UIComponent.getCurrentComponent(facesContext)).thenReturn(component);
        when(component.getClientId(facesContext)).thenReturn("testClientId");
        
        // Mock the client to return null (user not found)
        when(bitbucketClient.getUserAccount("nonExistentUser")).thenReturn(null);
        
        Object result = userInfo.lookUp();
        assertNull(result);
        assertFalse(userInfo.isFound());
        assertNull(userInfo.getUser());
        
        // Verify that addMessage was called
        // Note: We cannot easily verify the exact message content due to static method usage
    }

    @Test
    public void testLookUpUserFound() {
        userInfo.setName("existingUser");
        
        BitbucketAccount account = mock(BitbucketAccount.class);
        when(bitbucketClient.getUserAccount("existingUser")).thenReturn(account);
        
        Object result = userInfo.lookUp();
        assertNull(result);
        assertTrue(userInfo.isFound());
        assertEquals(account, userInfo.getUser());
    }
}
