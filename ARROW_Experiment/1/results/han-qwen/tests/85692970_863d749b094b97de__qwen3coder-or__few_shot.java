package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NativeClientList_ArrowTest {

    private NativeClientList nativeClientList;

    @Before
    public void setUp() {
        nativeClientList = new NativeClientList();
    }

    @Test
    public void testConstructor() {
        assertNotNull(nativeClientList);
    }

    @Test
    public void testAddWhenClosed() {
        Client mockClient = mock(Client.class);
        nativeClientList.add(mockClient);
        verify(mockClient, never()).addDisconnectedHandler(any());
    }

    @Test
    public void testAddWhenOpen() {
        Client mockClient = mock(Client.class);
        nativeClientList.open();
        nativeClientList.add(mockClient);
        verify(mockClient).addDisconnectedHandler(any());
    }

    @Test
    public void testRemoveWhenClosed() {
        Client mockClient = mock(Client.class);
        nativeClientList.remove(mockClient);
        verify(mockClient, never()).removeDisconnectedHandler(any());
    }

    @Test
    public void testRemoveWhenOpen() {
        Client mockClient = mock(Client.class);
        nativeClientList.open();
        nativeClientList.remove(mockClient);
        verify(mockClient).removeDisconnectedHandler(any());
    }

    @Test
    public void testClose() {
        nativeClientList.close();
        assertFalse(nativeClientList.isOpen());
    }

    @Test
    public void testOpen() {
        nativeClientList.open();
        assertTrue(nativeClientList.isOpen());
    }

    @Test
    public void testIsEmptyWhenEmpty() {
        assertTrue(nativeClientList.isEmpty());
    }

    @Test
    public void testIsEmptyWhenNotEmpty() {
        Client mockClient = mock(Client.class);
        nativeClientList.open();
        nativeClientList.add(mockClient);
        assertFalse(nativeClientList.isEmpty());
    }

    @Test
    public void testClear() {
        Client mockClient = mock(Client.class);
        nativeClientList.open();
        nativeClientList.add(mockClient);
        assertFalse(nativeClientList.isEmpty());
        nativeClientList.clear();
        assertTrue(nativeClientList.isEmpty());
    }

    @Test
    public void testSnapShot() {
        Client mockClient = mock(Client.class);
        nativeClientList.open();
        nativeClientList.add(mockClient);
        Collection<Client> snapshot = nativeClientList.snapShot();
        assertNotNull(snapshot);
        assertEquals(1, snapshot.size());
    }

    @Test
    public void testGetClientBySession() {
        Session mockSession = mock(Session.class);
        Client mockClient = mock(Client.class);
        when(mockClient.getSession()).thenReturn(mockSession);
        nativeClientList.open();
        nativeClientList.add(mockClient);
        Optional<Client> result = nativeClientList.getClient(mockSession);
        assertTrue(result.isPresent());
        assertEquals(mockClient, result.get());
    }

    @Test
    public void testGetClientBySessionNotFound() {
        Session mockSession = mock(Session.class);
        Client mockClient = mock(Client.class);
        when(mockClient.getSession()).thenReturn(mockSession);
        nativeClientList.open();
        nativeClientList.add(mockClient);
        Session differentSession = mock(Session.class);
        Optional<Client> result = nativeClientList.getClient(differentSession);
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetClientByID() {
        ClientID mockClientID = mock(ClientID.class);
        Client mockClient = mock(Client.class);
        when(mockClient.getID()).thenReturn(mockClientID);
        nativeClientList.open();
        nativeClientList.add(mockClient);
        Optional<Client> result = nativeClientList.getClient(mockClientID);
        assertTrue(result.isPresent());
        assertEquals(mockClient, result.get());
    }

    @Test
    public void testGetClientByIDNotFound() {
        ClientID mockClientID = mock(ClientID.class);
        Client mockClient = mock(Client.class);
        when(mockClient.getID()).thenReturn(mockClientID);
        nativeClientList.open();
        nativeClientList.add(mockClient);
        ClientID differentID = mock(ClientID.class);
        Optional<Client> result = nativeClientList.getClient(differentID);
        assertFalse(result.isPresent());
    }

    @Test
    public void testStream() {
        Client mockClient = mock(Client.class);
        nativeClientList.open();
        nativeClientList.add(mockClient);
        Stream<Client> stream = nativeClientList.stream();
        assertNotNull(stream);
    }

    @Test
    public void testSessionStream() {
        Session mockSession = mock(Session.class);
        Client mockClient = mock(Client.class);
        when(mockClient.getSession()).thenReturn(mockSession);
        nativeClientList.open();
        nativeClientList.add(mockClient);
        Stream<Session> sessionStream = nativeClientList.sessionStream();
        assertNotNull(sessionStream);
    }

    @Test
    public void testIterator() {
        Client mockClient = mock(Client.class);
        nativeClientList.open();
        nativeClientList.add(mockClient);
        Iterator<Client> iterator = nativeClientList.iterator();
        assertNotNull(iterator);
    }

    @Test
    public void testToString() {
        String result = nativeClientList.toString();
        assertNotNull(result);
        assertTrue(result.contains("NativeClientList"));
    }
}
