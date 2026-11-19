package org.vx68k.bitbucket.webapp;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

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
        userInfo = new UserInfo(sessionUser);
        when(sessionUser.getBitbucketClient()).thenReturn(bitbucketClient);
        FacesContext.setCurrentInstance(facesContext);
        when(UIComponent.getCurrentComponent(facesContext)).thenReturn(component);
        when(component.getClientId(facesContext)).thenReturn("testClientId");
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
        assertNull(userInfo.lookUp());
        assertFalse(userInfo.isFound());
        assertNull(userInfo.getUser());
    }

    @Test
    public void testLookUpUserNotFound() {
        userInfo.setName("nonExistentUser");
        when(bitbucketClient.getUserAccount("nonExistentUser")).thenReturn(null);
        
        assertNull(userInfo.lookUp());
        assertFalse(userInfo.isFound());
        assertNull(userInfo.getUser());
        
        verify(facesContext).addMessage(eq("testClientId"), any(FacesMessage.class));
    }

    @Test
    public void testLookUpUserFound() {
        BitbucketAccount account = new BitbucketAccount();
        userInfo.setName("existingUser");
        when(bitbucketClient.getUserAccount("existingUser")).thenReturn(account);
        
        assertNull(userInfo.lookUp());
        assertTrue(userInfo.isFound());
        assertEquals(account, userInfo.getUser());
    }

    @Test
    public void testGetBitbucketClient() {
        assertEquals(bitbucketClient, userInfo.getBitbucketClient());
    }
}
