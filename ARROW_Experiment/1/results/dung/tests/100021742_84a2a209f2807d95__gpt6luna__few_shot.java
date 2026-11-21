package me.snowdrop.stream.binder.artemis.provisioning;

import me.snowdrop.stream.binder.artemis.properties.ArtemisCommonProperties;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.junit.Test;
import org.springframework.cloud.stream.provisioning.ProvisioningException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class ArtemisBrokerManager_ArrowTest {

    @Test
    public void createQueueCreatesQueueWhenItDoesNotExist() throws Exception {
        ServerLocator locator = mock(ServerLocator.class);
        ClientSessionFactory factory = mock(ClientSessionFactory.class);
        ClientSession session = mock(ClientSession.class);
        ClientSession.QueueQuery query = mock(ClientSession.QueueQuery.class);

        when(locator.createSessionFactory()).thenReturn(factory);
        when(factory.createSession()).thenReturn(session);
        when(session.queueQuery(org.apache.activemq.artemis.api.core.SimpleString.toSimpleString("orders")))
                .thenReturn(query);
        when(query.isExists()).thenReturn(false);

        new ArtemisBrokerManager(locator, null, null).createQueue("orders", "orders");

        verify(session).createSharedQueue(
                org.apache.activemq.artemis.api.core.SimpleString.toSimpleString("orders"),
                org.apache.activemq.artemis.api.core.RoutingType.MULTICAST,
                org.apache.activemq.artemis.api.core.SimpleString.toSimpleString("orders"), true);
    }

    @Test
    public void createQueueDoesNothingWhenQueueAlreadyExistsAtSameAddress() throws Exception {
        ServerLocator locator = mock(ServerLocator.class);
        ClientSessionFactory factory = mock(ClientSessionFactory.class);
        ClientSession session = mock(ClientSession.class);
        ClientSession.QueueQuery query = mock(ClientSession.QueueQuery.class);

        when(locator.createSessionFactory()).thenReturn(factory);
        when(factory.createSession()).thenReturn(session);
        when(session.queueQuery(org.apache.activemq.artemis.api.core.SimpleString.toSimpleString("orders")))
                .thenReturn(query);
        when(query.isExists()).thenReturn(true);
        when(query.getAddress()).thenReturn(org.apache.activemq.artemis.api.core.SimpleString.toSimpleString("orders"));

        new ArtemisBrokerManager(locator, null, null).createQueue("orders", "orders");

        verify(session, never()).createSharedQueue(any(), any(), any(), anyBoolean());
    }

    @Test
    public void createQueueFailsWhenQueueExistsAtAnotherAddress() throws Exception {
        ServerLocator locator = mock(ServerLocator.class);
        ClientSessionFactory factory = mock(ClientSessionFactory.class);
        ClientSession session = mock(ClientSession.class);
        ClientSession.QueueQuery query = mock(ClientSession.QueueQuery.class);

        when(locator.createSessionFactory()).thenReturn(factory);
        when(factory.createSession()).thenReturn(session);
        when(session.queueQuery(org.apache.activemq.artemis.api.core.SimpleString.toSimpleString("orders")))
                .thenReturn(query);
        when(query.isExists()).thenReturn(true);
        when(query.getAddress()).thenReturn(org.apache.activemq.artemis.api.core.SimpleString.toSimpleString("other"));

        try {
            new ArtemisBrokerManager(locator, null, null).createQueue("orders", "orders");
            fail("Expected ProvisioningException");
        } catch (ProvisioningException exception) {
            assertEquals("Failed to create queue 'orders' with address 'orders'. Queue already exists under another address 'other'",
                    exception.getMessage());
        }
    }

    @Test
    public void createQueueWrapsOtherFailures() throws Exception {
        ServerLocator locator = mock(ServerLocator.class);
        when(locator.createSessionFactory()).thenThrow(new RuntimeException("failure"));

        try {
            new ArtemisBrokerManager(locator, null, null).createQueue("orders", "orders");
            fail("Expected ProvisioningException");
        } catch (ProvisioningException exception) {
            assertEquals("Failed to create queue 'orders' with address 'orders'", exception.getMessage());
        }
    }
}
