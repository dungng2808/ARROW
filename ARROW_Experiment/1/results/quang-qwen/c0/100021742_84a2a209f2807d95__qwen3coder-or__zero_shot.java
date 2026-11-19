package me.snowdrop.stream.binder.artemis.provisioning;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientRequestor;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.api.core.management.AddressSettingsInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import me.snowdrop.stream.binder.artemis.properties.ArtemisCommonProperties;
import org.springframework.cloud.stream.provisioning.ProvisioningException;

public class ArtemisBrokerManager_ArrowTest {

    @Mock
    private ServerLocator serverLocator;

    @Mock
    private ClientSessionFactory sessionFactory;

    @Mock
    private ClientSession session;

    @Mock
    private ClientRequestor requestor;

    @Mock
    private ClientMessage message;

    @Mock
    private AddressSettingsInfo addressSettingsInfo;

    private ArtemisBrokerManager artemisBrokerManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        artemisBrokerManager = new ArtemisBrokerManager(serverLocator, "testUser", "testPassword");
    }

    @Test
    public void testCreateAddress_WhenAddressDoesNotExist_ShouldCreateAddressAndConfigure() throws Exception {
        // Given
        String addressName = "testAddress";
        ArtemisCommonProperties properties = new ArtemisCommonProperties();
        properties.setModifyAddressSettings(true);
        properties.setAutoBindDeadLetterAddress(true);
        properties.setAutoBindExpiryAddress(true);
        properties.setManagementAddress("management");

        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt())).thenReturn(session);
        when(session.addressQuery(any(SimpleString.class))).thenReturn(mock(ClientSession.AddressQuery.class));
        when(session.addressQuery(any(SimpleString.class)).isExists()).thenReturn(false);
        when(session.createMessage(anyBoolean())).thenReturn(message);
        when(session.createMessage(anyBoolean())).thenReturn(message);
        when(message.getStringProperty(anyString())).thenReturn("test");
        when(session.createAddress(any(SimpleString.class), any(RoutingType.class), anyBoolean())).thenReturn(null);
        when(session.createSharedQueue(any(SimpleString.class), any(RoutingType.class), any(SimpleString.class), anyBoolean())).thenReturn(null);
        when(session.createRequestor(anyString())).thenReturn(requestor);
        when(requestor.request(any(ClientMessage.class))).thenReturn(message);
        when(message.getBodyBuffer()).thenReturn(null);
        when(AddressSettingsInfo.from(anyString())).thenReturn(addressSettingsInfo);

        // When
        artemisBrokerManager.createAddress(addressName, properties);

        // Then
        verify(session, times(1)).createAddress(any(SimpleString.class), any(RoutingType.class), anyBoolean());
        verify(session, times(1)).start();
        verify(session, times(1)).stop();
    }

    @Test
    public void testCreateAddress_WhenAddressExists_ShouldNotCreateAddress() throws Exception {
        // Given
        String addressName = "testAddress";
        ArtemisCommonProperties properties = new ArtemisCommonProperties();
        properties.setModifyAddressSettings(true);

        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt())).thenReturn(session);
        when(session.addressQuery(any(SimpleString.class))).thenReturn(mock(ClientSession.AddressQuery.class));
        when(session.addressQuery(any(SimpleString.class)).isExists()).thenReturn(true);

        // When
        artemisBrokerManager.createAddress(addressName, properties);

        // Then
        verify(session, never()).createAddress(any(SimpleString.class), any(RoutingType.class), anyBoolean());
        verify(session, times(1)).start();
        verify(session, times(1)).stop();
    }

    @Test
    public void testCreateQueue_WhenQueueDoesNotExist_ShouldCreateQueue() throws Exception {
        // Given
        String address = "testAddress";
        String queueName = "testQueue";

        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt())).thenReturn(session);
        when(session.queueQuery(any(SimpleString.class))).thenReturn(mock(ClientSession.QueueQuery.class));
        when(session.queueQuery(any(SimpleString.class)).isExists()).thenReturn(false);
        when(session.createSharedQueue(any(SimpleString.class), any(RoutingType.class), any(SimpleString.class), anyBoolean())).thenReturn(null);

        // When
        artemisBrokerManager.createQueue(address, queueName);

        // Then
        verify(session, times(1)).createSharedQueue(any(SimpleString.class), any(RoutingType.class), any(SimpleString.class), anyBoolean());
        verify(session, times(1)).start();
        verify(session, times(1)).stop();
    }

    @Test(expected = ProvisioningException.class)
    public void testCreateQueue_WhenQueueExistsUnderDifferentAddress_ShouldThrowException() throws Exception {
        // Given
        String address = "testAddress";
        String queueName = "testQueue";

        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt())).thenReturn(session);
        when(session.queueQuery(any(SimpleString.class))).thenReturn(mock(ClientSession.QueueQuery.class));
        when(session.queueQuery(any(SimpleString.class)).isExists()).thenReturn(true);
        when(session.queueQuery(any(SimpleString.class)).getAddress()).thenReturn(SimpleString.toSimpleString("differentAddress"));

        // When
        artemisBrokerManager.createQueue(address, queueName);

        // Then
        // Exception is expected
    }

    @Test
    public void testCreateQueue_WhenQueueExistsUnderSameAddress_ShouldNotCreateQueue() throws Exception {
        // Given
        String address = "testAddress";
        String queueName = "testQueue";

        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt())).thenReturn(session);
        when(session.queueQuery(any(SimpleString.class))).thenReturn(mock(ClientSession.QueueQuery.class));
        when(session.queueQuery(any(SimpleString.class)).isExists()).thenReturn(true);
        when(session.queueQuery(any(SimpleString.class)).getAddress()).thenReturn(SimpleString.toSimpleString(address));

        // When
        artemisBrokerManager.createQueue(address, queueName);

        // Then
        verify(session, never()).createSharedQueue(any(SimpleString.class), any(RoutingType.class), any(SimpleString.class), anyBoolean());
        verify(session, times(1)).start();
        verify(session, times(1)).stop();
    }

    @Test
    public void testGetClientSession_WithNullUsername_ShouldCreateSessionWithoutCredentials() throws Exception {
        // Given
        ArtemisBrokerManager manager = new ArtemisBrokerManager(serverLocator, null, "testPassword");
        when(serverLocator.isPreAcknowledge()).thenReturn(false);
        when(serverLocator.getAckBatchSize()).thenReturn(0);

        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession()).thenReturn(session);

        // When
        ClientSession result = manager.getClientSession(sessionFactory);

        // Then
        assertEquals(session, result);
        verify(sessionFactory, times(1)).createSession();
    }

    @Test
    public void testGetClientSession_WithUsernameAndPassword_ShouldCreateSessionWithCredentials() throws Exception {
        // Given
        when(serverLocator.isPreAcknowledge()).thenReturn(false);
        when(serverLocator.getAckBatchSize()).thenReturn(0);

        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt())).thenReturn(session);

        // When
        ClientSession result = artemisBrokerManager.getClientSession(sessionFactory);

        // Then
        assertEquals(session, result);
        verify(sessionFactory, times(1)).createSession(eq("testUser"), eq("testPassword"), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt());
    }
}
