package me.snowdrop.stream.binder.artemis.provisioning;

import me.snowdrop.stream.binder.artemis.properties.ArtemisCommonProperties;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.*;
import org.apache.activemq.artemis.api.core.management.AddressSettingsInfo;
import org.apache.activemq.artemis.api.core.management.ManagementHelper;
import org.apache.activemq.artemis.api.core.management.ResourceNames;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

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
    private ClientMessage replyMessage;

    @Mock
    private ArtemisCommonProperties properties;

    private ArtemisBrokerManager artemisBrokerManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        artemisBrokerManager = new ArtemisBrokerManager(serverLocator, "testUser", "testPassword");
    }

    @Test
    public void testCreateAddressWhenAddressDoesNotExist() throws Exception {
        // Given
        String addressName = "testAddress";
        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(any(String.class), any(String.class), eq(true), eq(false),
                eq(false), eq(false), eq(0))).thenReturn(session);
        when(session.addressQuery(any(SimpleString.class))).thenReturn(mock(ClientSession.AddressQuery.class));
        when(session.addressQuery(any(SimpleString.class)).isExists()).thenReturn(false);
        doNothing().when(session).start();
        doNothing().when(session).createAddress(any(SimpleString.class), any(RoutingType.class), eq(true));
        when(properties.isModifyAddressSettings()).thenReturn(false);
        doNothing().when(session).stop();

        // When
        artemisBrokerManager.createAddress(addressName, properties);

        // Then
        verify(session).start();
        verify(session).createAddress(eq(SimpleString.toSimpleString(addressName)), eq(RoutingType.MULTICAST), eq(true));
        verify(session).stop();
    }

    @Test
    public void testCreateAddressWhenAddressAlreadyExists() throws Exception {
        // Given
        String addressName = "testAddress";
        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(any(String.class), any(String.class), eq(true), eq(false),
                eq(false), eq(false), eq(0))).thenReturn(session);
        when(session.addressQuery(any(SimpleString.class))).thenReturn(mock(ClientSession.AddressQuery.class));
        when(session.addressQuery(any(SimpleString.class)).isExists()).thenReturn(true);
        doNothing().when(session).start();
        doNothing().when(session).stop();

        // When
        artemisBrokerManager.createAddress(addressName, properties);

        // Then
        verify(session).start();
        verify(session, never()).createAddress(any(SimpleString.class), any(RoutingType.class), eq(true));
        verify(session).stop();
    }

    @Test
    public void testCreateAddressWithModifyAddressSettings() throws Exception {
        // Given
        String addressName = "testAddress";
        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(any(String.class), any(String.class), eq(true), eq(false),
                eq(false), eq(false), eq(0))).thenReturn(session);
        when(session.addressQuery(any(SimpleString.class))).thenReturn(mock(ClientSession.AddressQuery.class));
        when(session.addressQuery(any(SimpleString.class)).isExists()).thenReturn(false);
        doNothing().when(session).start();
        doNothing().when(session).createAddress(any(SimpleString.class), any(RoutingType.class), eq(true));
        when(properties.isModifyAddressSettings()).thenReturn(true);
        when(properties.getManagementAddress()).thenReturn("managementAddress");
        when(properties.isAutoBindDeadLetterAddress()).thenReturn(false);
        when(properties.isAutoBindExpiryAddress()).thenReturn(false);
        when(session.createMessage(eq(false))).thenReturn(message);
        when(session.createMessage(eq(false))).thenReturn(replyMessage);
        when(serverLocator.isPreAcknowledge()).thenReturn(false);
        when(serverLocator.getAckBatchSize()).thenReturn(0);
        when(session.createRequestor(any(String.class), any(String.class))).thenReturn(requestor);
        when(requestor.request(any(ClientMessage.class))).thenReturn(replyMessage);
        when(ManagementHelper.getResult(any(ClientMessage.class), any(Class.class))).thenReturn("{}");
        doNothing().when(session).stop();

        // When
        artemisBrokerManager.createAddress(addressName, properties);

        // Then
        verify(session).start();
        verify(session).createAddress(eq(SimpleString.toSimpleString(addressName)), eq(RoutingType.MULTICAST), eq(true));
        verify(session).stop();
    }

    @Test
    public void testCreateQueue() throws Exception {
        // Given
        String address = "testAddress";
        String queueName = "testQueue";
        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(any(String.class), any(String.class), eq(true), eq(false),
                eq(false), eq(false), eq(0))).thenReturn(session);
        when(session.queueQuery(any(SimpleString.class))).thenReturn(mock(ClientSession.QueueQuery.class));
        when(session.queueQuery(any(SimpleString.class)).isExists()).thenReturn(false);
        doNothing().when(session).createSharedQueue(any(SimpleString.class), any(RoutingType.class),
                any(SimpleString.class), eq(true));

        // When
        artemisBrokerManager.createQueue(address, queueName);

        // Then
        verify(session).createSharedQueue(eq(SimpleString.toSimpleString(address)),
                eq(RoutingType.MULTICAST), eq(SimpleString.toSimpleString(queueName)), eq(true));
    }

    @Test(expected = ProvisioningException.class)
    public void testCreateQueueWhenQueueExistsUnderDifferentAddress() throws Exception {
        // Given
        String address = "testAddress";
        String queueName = "testQueue";
        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(any(String.class), any(String.class), eq(true), eq(false),
                eq(false), eq(false), eq(0))).thenReturn(session);
        when(session.queueQuery(any(SimpleString.class))).thenReturn(mock(ClientSession.QueueQuery.class));
        when(session.queueQuery(any(SimpleString.class)).isExists()).thenReturn(true);
        when(session.queueQuery(any(SimpleString.class)).getAddress()).thenReturn(SimpleString.toSimpleString("differentAddress"));

        // When
        artemisBrokerManager.createQueue(address, queueName);

        // Then
        // Expecting ProvisioningException
    }

    @Test
    public void testCreateQueueWhenQueueAlreadyExists() throws Exception {
        // Given
        String address = "testAddress";
        String queueName = "testQueue";
        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(any(String.class), any(String.class), eq(true), eq(false),
                eq(false), eq(false), eq(0))).thenReturn(session);
        when(session.queueQuery(any(SimpleString.class))).thenReturn(mock(ClientSession.QueueQuery.class));
        when(session.queueQuery(any(SimpleString.class)).isExists()).thenReturn(true);
        when(session.queueQuery(any(SimpleString.class)).getAddress()).thenReturn(SimpleString.toSimpleString(address));

        // When
        artemisBrokerManager.createQueue(address, queueName);

        // Then
        verify(session, never()).createSharedQueue(any(SimpleString.class), any(RoutingType.class),
                any(SimpleString.class), eq(true));
    }

    @Test
    public void testConfigureAddressWithDLQAndExp() throws Exception {
        // Given
        String address = "testAddress";
        when(properties.isAutoBindDeadLetterAddress()).thenReturn(true);
        when(properties.isAutoBindExpiryAddress()).thenReturn(true);
        when(properties.getManagementAddress()).thenReturn("managementAddress");
        when(session.createMessage(eq(false))).thenReturn(message);
        when(session.createMessage(eq(false))).thenReturn(replyMessage);
        when(session.createRequestor(any(String.class), any(String.class))).thenReturn(requestor);
        when(requestor.request(any(ClientMessage.class))).thenReturn(replyMessage);
        when(ManagementHelper.getResult(any(ClientMessage.class), any(Class.class))).thenReturn("{}");
        when(serverLocator.isPreAcknowledge()).thenReturn(false);
        when(serverLocator.getAckBatchSize()).thenReturn(0);

        // When
        artemisBrokerManager.configureAddress(session, address, properties);

        // Then
        verify(session, times(2)).createSharedQueue(any(SimpleString.class), any(RoutingType.class),
                any(SimpleString.class), eq(true));
    }

    @Test
    public void testUpdateAddressSettings() throws Exception {
        // Given
        String address = "testAddress";
        when(properties.getManagementAddress()).thenReturn("managementAddress");
        when(session.createMessage(eq(false))).thenReturn(message);
        when(session.createRequestor(any(String.class), any(String.class))).thenReturn(requestor);
        when(requestor.request(any(ClientMessage.class))).thenReturn(replyMessage);
        when(ManagementHelper.getResult(any(ClientMessage.class), any(Class.class))).thenReturn("{}");
        when(serverLocator.isPreAcknowledge()).thenReturn(false);
        when(serverLocator.getAckBatchSize()).thenReturn(0);
        when(properties.isAutoBindDeadLetterAddress()).thenReturn(false);
        when(properties.isAutoBindExpiryAddress()).thenReturn(false);
        when(properties.getBrokerExpiryDelay()).thenReturn(-1L);
        when(properties.getBrokerMaxDeliveryAttempts()).thenReturn(-1);
        when(properties.getBrokerRedeliveryDelay()).thenReturn(-1L);
        when(properties.getBrokerRedeliveryDelayMultiplier()).thenReturn(-1.0);
        when(properties.getBrokerMaxRedeliveryDelay()).thenReturn(-1L);
        when(properties.isBrokerSendToDlaOnNoRoute()).thenReturn(false);

        // When
        artemisBrokerManager.updateAddressSettings(session, address, properties);

        // Then
        verify(requestor).request(any(ClientMessage.class));
    }

    @Test
    public void testGetCurrentAddressSettings() throws Exception {
        // Given
        String address = "testAddress";
        when(session.createMessage(eq(false))).thenReturn(message);
        when(session.createRequestor(any(String.class), any(String.class))).thenReturn(requestor);
        when(requestor.request(any(ClientMessage.class))).thenReturn(replyMessage);
        when(ManagementHelper.getResult(any(ClientMessage.class), any(Class.class))).thenReturn("{\"name\":\"testAddress\"}");

        // When
        AddressSettingsInfo result = artemisBrokerManager.getCurrentAddressSettings(session, requestor, address);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("testAddress");
    }

    @Test
    public void testGetClientSessionWithUsername() throws Exception {
        // Given
        when(sessionFactory.createSession(any(String.class), any(String.class), eq(true), eq(false),
                eq(false), eq(false), eq(0))).thenReturn(session);
        when(serverLocator.isPreAcknowledge()).thenReturn(false);
        when(serverLocator.getAckBatchSize()).thenReturn(0);

        // When
        ClientSession result = artemisBrokerManager.getClientSession(sessionFactory);

        // Then
        assertThat(result).isSameAs(session);
    }

    @Test
    public void testGetClientSessionWithoutUsername() throws Exception {
        // Given
        ArtemisBrokerManager manager = new ArtemisBrokerManager(serverLocator, null, null);
        when(sessionFactory.createSession()).thenReturn(session);

        // When
        ClientSession result = manager.getClientSession(sessionFactory);

        // Then
        assertThat(result).isSameAs(session);
    }
}
