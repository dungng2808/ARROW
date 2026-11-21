package me.snowdrop.stream.binder.artemis.provisioning;

import me.snowdrop.stream.binder.artemis.properties.ArtemisCommonProperties;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class ArtemisBrokerManager_ArrowTest {

    @Test
    public void createsAddressWhenItDoesNotExist() throws Exception {
        ServerLocator locator = mock(ServerLocator.class);
        ClientSessionFactory factory = mock(ClientSessionFactory.class);
        ClientSession session = mock(ClientSession.class);
        ClientSession.AddressQuery query = mock(ClientSession.AddressQuery.class);
        ArtemisCommonProperties properties = mock(ArtemisCommonProperties.class);

        when(locator.createSessionFactory()).thenReturn(factory);
        when(factory.createSession()).thenReturn(session);
        when(session.addressQuery(SimpleString.toSimpleString("orders"))).thenReturn(query);
        when(query.isExists()).thenReturn(false);
        when(properties.isModifyAddressSettings()).thenReturn(false);

        new ArtemisBrokerManager(locator, null, null).createAddress("orders", properties);

        verify(session).start();
        verify(session).createAddress(SimpleString.toSimpleString("orders"), RoutingType.MULTICAST, true);
        verify(session).stop();
    }

    @Test
    public void leavesExistingAddressUnchanged() throws Exception {
        ServerLocator locator = mock(ServerLocator.class);
        ClientSessionFactory factory = mock(ClientSessionFactory.class);
        ClientSession session = mock(ClientSession.class);
        ClientSession.AddressQuery query = mock(ClientSession.AddressQuery.class);
        ArtemisCommonProperties properties = mock(ArtemisCommonProperties.class);

        when(locator.createSessionFactory()).thenReturn(factory);
        when(factory.createSession()).thenReturn(session);
        when(session.addressQuery(SimpleString.toSimpleString("orders"))).thenReturn(query);
        when(query.isExists()).thenReturn(true);

        new ArtemisBrokerManager(locator, null, null).createAddress("orders", properties);

        verify(session).start();
        verify(session, never()).createAddress(any(SimpleString.class), any(RoutingType.class), anyBoolean());
        verify(session).stop();
    }

    @Test
    public void createsQueueWhenItDoesNotExist() throws Exception {
        ServerLocator locator = mock(ServerLocator.class);
        ClientSessionFactory factory = mock(ClientSessionFactory.class);
        ClientSession session = mock(ClientSession.class);
        ClientSession.QueueQuery query = mock(ClientSession.QueueQuery.class);

        when(locator.createSessionFactory()).thenReturn(factory);
        when(factory.createSession()).thenReturn(session);
        when(session.queueQuery(SimpleString.toSimpleString("orders.queue"))).thenReturn(query);
        when(query.isExists()).thenReturn(false);

        new ArtemisBrokerManager(locator, null, null).createQueue("orders", "orders.queue");

        verify(session).createSharedQueue(SimpleString.toSimpleString("orders"), RoutingType.MULTICAST,
                SimpleString.toSimpleString("orders.queue"), true);
    }

    @Test
    public void leavesQueueUnchangedWhenItAlreadyExistsAtSameAddress() throws Exception {
        ServerLocator locator = mock(ServerLocator.class);
        ClientSessionFactory factory = mock(ClientSessionFactory.class);
        ClientSession session = mock(ClientSession.class);
        ClientSession.QueueQuery query = mock(ClientSession.QueueQuery.class);

        when(locator.createSessionFactory()).thenReturn(factory);
        when(factory.createSession()).thenReturn(session);
        when(session.queueQuery(SimpleString.toSimpleString("orders.queue"))).thenReturn(query);
        when(query.isExists()).thenReturn(true);
        when(query.getAddress()).thenReturn(SimpleString.toSimpleString("orders"));

        new ArtemisBrokerManager(locator, null, null).createQueue("orders", "orders.queue");

        verify(session, never()).createSharedQueue(any(SimpleString.class), any(RoutingType.class),
                any(SimpleString.class), anyBoolean());
    }

    @Test(expected = org.springframework.cloud.stream.provisioning.ProvisioningException.class)
    public void rejectsQueueAlreadyAssociatedWithAnotherAddress() throws Exception {
        ServerLocator locator = mock(ServerLocator.class);
        ClientSessionFactory factory = mock(ClientSessionFactory.class);
        ClientSession session = mock(ClientSession.class);
        ClientSession.QueueQuery query = mock(ClientSession.QueueQuery.class);

        when(locator.createSessionFactory()).thenReturn(factory);
        when(factory.createSession()).thenReturn(session);
        when(session.queueQuery(SimpleString.toSimpleString("orders.queue"))).thenReturn(query);
        when(query.isExists()).thenReturn(true);
        when(query.getAddress()).thenReturn(SimpleString.toSimpleString("other"));

        new ArtemisBrokerManager(locator, null, null).createQueue("orders", "orders.queue");
    }
}
