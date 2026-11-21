package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NativeClientList_ArrowTest {

    @Test
    public void newListIsClosedAndEmpty() {
        NativeClientList list = new NativeClientList();

        assertFalse(list.isOpen());
        assertTrue(list.isEmpty());
        assertTrue(list.snapShot().isEmpty());
    }

    @Test
    public void openAndCloseUpdateOpenState() {
        NativeClientList list = new NativeClientList();

        list.open();
        assertTrue(list.isOpen());

        list.close();
        assertFalse(list.isOpen());
    }

    @Test
    public void addWhileClosedDoesNotAddClient() {
        NativeClientList list = new NativeClientList();
        Client client = mock(Client.class);

        list.add(client);

        assertTrue(list.isEmpty());
        verify(client, never()).addDisconnectedHandler(any());
    }

    @Test
    public void addWhileOpenAddsClientAndHandler() {
        NativeClientList list = new NativeClientList();
        Client client = mock(Client.class);
        list.open();

        list.add(client);

        assertEquals(1, list.snapShot().size());
        assertSame(client, list.snapShot().iterator().next());
        verify(client).addDisconnectedHandler(any());
    }

    @Test
    public void removeWhileClosedLeavesClientAndHandlerAlone() {
        NativeClientList list = new NativeClientList();
        Client client = mock(Client.class);
        list.open();
        list.add(client);
        list.close();

        list.remove(client);

        assertEquals(1, list.snapShot().size());
        verify(client, never()).removeDisconnectedHandler(any());
    }

    @Test
    public void removeWhileOpenRemovesClientAndHandler() {
        NativeClientList list = new NativeClientList();
        Client client = mock(Client.class);
        list.open();
        list.add(client);

        list.remove(client);

        assertTrue(list.isEmpty());
        verify(client).removeDisconnectedHandler(any());
    }

    @Test
    public void clearRemovesClientsEvenWhenClosed() {
        NativeClientList list = new NativeClientList();
        Client client = mock(Client.class);
        list.open();
        list.add(client);
        list.close();

        list.clear();

        assertTrue(list.isEmpty());
    }

    @Test
    public void snapshotIsIndependentOfLaterChanges() {
        NativeClientList list = new NativeClientList();
        Client client = mock(Client.class);
        list.open();
        list.add(client);
        Collection<Client> snapshot = list.snapShot();

        list.clear();

        assertEquals(1, snapshot.size());
        assertSame(client, snapshot.iterator().next());
    }

    @Test
    public void getClientBySessionFindsMatchingClient() {
        NativeClientList list = new NativeClientList();
        Client client = mock(Client.class);
        Session session = mock(Session.class);
        when(client.getSession()).thenReturn(session);
        list.open();
        list.add(client);

        Optional<Client> result = list.getClient(session);

        assertTrue(result.isPresent());
        assertSame(client, result.get());
    }

    @Test
    public void getClientByIdFindsMatchingClient() {
        NativeClientList list = new NativeClientList();
        Client client = mock(Client.class);
        ClientID id = mock(ClientID.class);
        when(client.getID()).thenReturn(id);
        list.open();
        list.add(client);

        Optional<Client> result = list.getClient(id);

        assertTrue(result.isPresent());
        assertSame(client, result.get());
    }

    @Test
    public void streamContainsAddedClients() {
        NativeClientList list = new NativeClientList();
        Client first = mock(Client.class);
        Client second = mock(Client.class);
        list.open();
        list.add(first);
        list.add(second);

        assertEquals(Arrays.asList(first, second), list.stream().collect(Collectors.toList()));
    }

    @Test
    public void sessionStreamContainsClientSessions() {
        NativeClientList list = new NativeClientList();
        Client client = mock(Client.class);
        Session session = mock(Session.class);
        when(client.getSession()).thenReturn(session);
        list.open();
        list.add(client);

        assertEquals(Arrays.asList(session), list.sessionStream().collect(Collectors.toList()));
    }

    @Test
    public void iteratorContainsAddedClient() {
        NativeClientList list = new NativeClientList();
        Client client = mock(Client.class);
        list.open();
        list.add(client);

        Iterator<Client> iterator = list.iterator();

        assertTrue(iterator.hasNext());
        assertSame(client, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void toStringIncludesOpenStateAndContents() {
        NativeClientList list = new NativeClientList();
        Client client = mock(Client.class);
        list.open();
        list.add(client);

        assertEquals("NativeClientList{core=[" + client + "], openValue=true}", list.toString());
    }
}
