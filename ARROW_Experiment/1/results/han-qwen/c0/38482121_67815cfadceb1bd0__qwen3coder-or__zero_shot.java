package org.vx68k.bitbucket.webapp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.vx68k.bitbucket.BitbucketAccount;
import org.vx68k.bitbucket.client.BitbucketClient;

public class UserInfo_ArrowTest {

    @Mock
    private SessionUser sessionUser;

    @Mock
    private BitbucketClient bitbucketClient;

    @Mock
    private FacesContext facesContext;

    @Mock
    private UIComponent component;

    private UserInfo userInfo;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(sessionUser.getBitbucketClient()).thenReturn(bitbucketClient);
        userInfo = new UserInfo(sessionUser);
    }

    @Test
    public void testConstructor() {
        assertNotNull(userInfo.getSessionUser());
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
        userInfo.setName("");
        Object result = userInfo.lookUp();
        assertNull(result);
        assertNull(userInfo.getUser());
        assertFalse(userInfo.isFound());
    }

    @Test
    public void testLookUpUserNotFound() {
        userInfo.setName("nonExistentUser");
        when(bitbucketClient.getUserAccount("nonExistentUser")).thenReturn(null);
        
        // Mock FacesContext and UIComponent
        FacesContext.setCurrentInstance(facesContext);
        when(UIComponent.getCurrentComponent(facesContext)).thenReturn(component);
        when(component.getClientId(facesContext)).thenReturn("testClientId");
        
        Object result = userInfo.lookUp();
        assertNull(result);
        assertNull(userInfo.getUser());
        assertFalse(userInfo.isFound());
        
        // Verify that FacesMessage was added
        verify(facesContext).addMessage(eq("testClientId"), any(FacesMessage.class));
        
        // Clean up
        FacesContext.setCurrentInstance(null);
    }

    @Test
    public void testLookUpUserFound() {
        BitbucketAccount account = new BitbucketAccount();
        userInfo.setName("existingUser");
        when(bitbucketClient.getUserAccount("existingUser")).thenReturn(account);
        
        Object result = userInfo.lookUp();
        assertNull(result);
        assertEquals(account, userInfo.getUser());
        assertTrue(userInfo.isFound());
    }

    @Test
    public void testGetBitbucketClient() {
        assertEquals(bitbucketClient, userInfo.getBitbucketClient());
    }
}
