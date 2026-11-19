package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class NativeClientList_ArrowTest {

    @Test
    public void startsClosedAndCanBeOpenedAndClosed() {
        NativeClientList list = new NativeClientList();

        assertFalse(list.isOpen());
        list.open();
        assertTrue(list.isOpen());
        list.close();
        assertFalse(list.isOpen());
    }

    @Test
    public void emptyListHasEmptyViews() {
        NativeClientList list = new NativeClientList();

        assertTrue(list.isEmpty());
        assertTrue(list.snapShot().isEmpty());
        assertEquals(0, list.stream().count());
        assertEquals(0, list.sessionStream().count());
        assertFalse(list.getClient((Session) null).isPresent());
        assertFalse(list.getClient((ClientID) null).isPresent());
    }

    @Test
    public void snapshotIsAnIndependentCollection() {
        NativeClientList list = new NativeClientList();
        Collection<Client> snapshot = list.snapShot();

        assertNotSame(list.snapShot(), snapshot);
        assertTrue(snapshot.isEmpty());
    }

    @Test
    public void iteratorOverEmptyListHasNoElements() {
        NativeClientList list = new NativeClientList();
        Iterator<Client> iterator = list.iterator();

        assertFalse(iterator.hasNext());
    }

    @Test
    public void clearLeavesListEmpty() {
        NativeClientList list = new NativeClientList();
        list.clear();

        assertTrue(list.isEmpty());
        assertTrue(list.snapShot().isEmpty());
    }

    @Test
    public void closedListIgnoresAddAndRemove() {
        NativeClientList list = new NativeClientList();

        list.add(null);
        assertTrue(list.isEmpty());

        list.remove(null);
        assertTrue(list.isEmpty());
    }

    @Test
    public void stringRepresentationIncludesEmptyCoreAndClosedState() {
        NativeClientList list = new NativeClientList();

        assertEquals("NativeClientList{core=[], openValue=false}", list.toString());
    }

    @Test
    public void openingChangesStringRepresentation() {
        NativeClientList list = new NativeClientList();
        list.open();

        assertEquals("NativeClientList{core=[], openValue=true}", list.toString());
    }
}
