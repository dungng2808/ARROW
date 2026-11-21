package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NativeClientList_ArrowTest {

    private NativeClientList nativeClientList;
    private Client mockClient;
    private Session mockSession;
    private ClientID mockClientID;

    @Before
    public void setUp() {
        nativeClientList = new NativeClientList();
        mockClient = mock(Client.class);
        mockSession = mock(Session.class);
        mockClientID = mock(ClientID.class);
    }

    @Test
    public void testConstructor() {
        assertNotNull(nativeClientList);
    }

    @Test
    public void testAddWhenClosed() {
        nativeClientList.close();
        nativeClientList.add(mockClient);
        verifyZeroInteractions(mockClient);
    }

    @Test
    public void testAddWhenOpen() {
        nativeClientList.open();
        nativeClientList.add(mockClient);
        verify(mockClient).addDisconnectedHandler(any());
    }

    @Test
    public void testRemoveWhenClosed() {
        nativeClientList.close();
        nativeClientList.remove(mockClient);
        verifyZeroInteractions(mockClient);
    }

    @Test
    public void testRemoveWhenOpen() {
        nativeClientList.open();
        nativeClientList.add(mockClient);
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
        nativeClientList.open();
        nativeClientList.add(mockClient);
        assertFalse(nativeClientList.isEmpty());
    }

    @Test
    public void testClear() {
        nativeClientList.open();
        nativeClientList.add(mockClient);
        assertEquals(1, nativeClientList.snapShot().size());
        nativeClientList.clear();
        assertTrue(nativeClientList.snapShot().isEmpty());
    }

    @Test
    public void testSnapShot() {
        nativeClientList.open();
        nativeClientList.add(mockClient);
        Collection<Client> snapshot = nativeClientList.snapShot();
        assertEquals(1, snapshot.size());
        assertTrue(snapshot.contains(mockClient));
    }

    @Test
    public void testGetClientBySessionFound() {
        nativeClientList.open();
        when(mockClient.getSession()).thenReturn(mockSession);
        nativeClientList.add(mockClient);
        Optional<Client> result = nativeClientList.getClient(mockSession);
        assertTrue(result.isPresent());
        assertEquals(mockClient, result.get());
    }

    @Test
    public void testGetClientBySessionNotFound() {
        nativeClientList.open();
        when(mockClient.getSession()).thenReturn(mockSession);
        nativeClientList.add(mockClient);
        Optional<Client> result = nativeClientList.getClient(mock(Session.class));
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetClientByIDFound() {
        nativeClientList.open();
        when(mockClient.getID()).thenReturn(mockClientID);
        nativeClientList.add(mockClient);
        Optional<Client> result = nativeClientList.getClient(mockClientID);
        assertTrue(result.isPresent());
        assertEquals(mockClient, result.get());
    }

    @Test
    public void testGetClientByIDNotFound() {
        nativeClientList.open();
        when(mockClient.getID()).thenReturn(mockClientID);
        nativeClientList.add(mockClient);
        Optional<Client> result = nativeClientList.getClient(mock(ClientID.class));
        assertFalse(result.isPresent());
    }

    @Test
    public void testStream() {
        nativeClientList.open();
        nativeClientList.add(mockClient);
        Stream<Client> stream = nativeClientList.stream();
        assertNotNull(stream);
    }

    @Test
    public void testSessionStream() {
        nativeClientList.open();
        when(mockClient.getSession()).thenReturn(mockSession);
        nativeClientList.add(mockClient);
        Stream<Session> sessionStream = nativeClientList.sessionStream();
        assertNotNull(sessionStream);
    }

    @Test
    public void testIterator() {
        nativeClientList.open();
        nativeClientList.add(mockClient);
        java.util.Iterator<Client> iterator = nativeClientList.iterator();
        assertNotNull(iterator);
    }
}
