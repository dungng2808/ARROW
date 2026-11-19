package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class NativeClientList_ArrowTest {

    @Test
    public void startsClosedAndCanBeOpenedAndClosed() {
        NativeClientList clients = new NativeClientList();

        assertFalse(clients.isOpen());
        clients.open();
        assertTrue(clients.isOpen());
        clients.close();
        assertFalse(clients.isOpen());
    }

    @Test
    public void closedListIgnoresAdd() {
        NativeClientList clients = new NativeClientList();

        clients.add(null);

        assertTrue(clients.isEmpty());
        assertEquals(0, clients.snapShot().size());
    }

    @Test
    public void clearEmptiesList() {
        NativeClientList clients = new NativeClientList();
        clients.open();

        clients.clear();

        assertTrue(clients.isEmpty());
        assertEquals(0, clients.stream().count());
    }

    @Test
    public void snapshotIsAnIndependentCollection() {
        NativeClientList clients = new NativeClientList();

        Collection<Client> snapshot = clients.snapShot();

        assertTrue(snapshot.isEmpty());
        assertNotSame(snapshot, clients.snapShot());
    }

    @Test
    public void emptyListQueriesReturnEmptyResults() {
        NativeClientList clients = new NativeClientList();

        assertFalse(clients.getClient((Session) null).isPresent());
        assertFalse(clients.getClient((ClientID) null).isPresent());
        assertEquals(0, clients.sessionStream().count());
    }

    @Test
    public void emptyListProvidesEmptyIteratorAndStream() {
        NativeClientList clients = new NativeClientList();

        Iterator<Client> iterator = clients.iterator();

        assertFalse(iterator.hasNext());
        assertEquals(0, clients.stream().collect(Collectors.toList()).size());
    }

    @Test
    public void stringRepresentationIncludesState() {
        NativeClientList clients = new NativeClientList();

        assertEquals("NativeClientList{core=[], openValue=false}", clients.toString());
    }
}
