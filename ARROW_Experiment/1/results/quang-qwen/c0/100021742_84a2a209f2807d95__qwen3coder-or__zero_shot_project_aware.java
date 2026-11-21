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
import static org.mockito.Matchers.anyString;
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

    private ArtemisBrokerManager brokerManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        brokerManager = new ArtemisBrokerManager(serverLocator, "testUser", "testPassword");
    }

    @Test
    public void testCreateAddressWhenAddressDoesNotExist() throws Exception {
        // Given
        String addressName = "testAddress";
        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt()))
                .thenReturn(session);
        when(session.addressQuery(any(SimpleString.class))).thenReturn(createAddressQuery(false));
        when(properties.isModifyAddressSettings()).thenReturn(false);

        // When
        brokerManager.createAddress(addressName, properties);

        // Then
        verify(session).start();
        verify(session).createAddress(SimpleString.toSimpleString(addressName), RoutingType.MULTICAST, true);
        verify(session).stop();
    }

    @Test
    public void testCreateAddressWhenAddressAlreadyExists() throws Exception {
        // Given
        String addressName = "testAddress";
        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt()))
                .thenReturn(session);
        when(session.addressQuery(any(SimpleString.class))).thenReturn(createAddressQuery(true));
        when(properties.isModifyAddressSettings()).thenReturn(false);

        // When
        brokerManager.createAddress(addressName, properties);

        // Then
        verify(session).start();
        verify(session, never()).createAddress(any(SimpleString.class), any(RoutingType.class), anyBoolean());
        verify(session).stop();
    }

    @Test
    public void testCreateAddressWithModifySettings() throws Exception {
        // Given
        String addressName = "testAddress";
        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt()))
                .thenReturn(session);
        when(session.addressQuery(any(SimpleString.class))).thenReturn(createAddressQuery(false));
        when(properties.isModifyAddressSettings()).thenReturn(true);
        when(properties.getManagementAddress()).thenReturn("managementAddress");
        when(session.createMessage(false)).thenReturn(message);
        when(session.createRequestor(anyString(), anyString())).thenReturn(requestor);
        when(requestor.request(any(ClientMessage.class))).thenReturn(replyMessage);
        when(replyMessage.getBodyBuffer()).thenReturn(null);
        when(ManagementHelper.getResult(any(ClientMessage.class), any(Class.class))).thenReturn("{}");

        // When
        brokerManager.createAddress(addressName, properties);

        // Then
        verify(session).start();
        verify(session).createAddress(SimpleString.toSimpleString(addressName), RoutingType.MULTICAST, true);
        verify(session).stop();
    }

    @Test
    public void testCreateQueue() throws Exception {
        // Given
        String address = "testAddress";
        String queueName = "testQueue";
        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt()))
                .thenReturn(session);
        when(session.queueQuery(any(SimpleString.class))).thenReturn(createQueueQuery(false));

        // When
        brokerManager.createQueue(address, queueName);

        // Then
        verify(session).createSharedQueue(any(SimpleString.class), any(RoutingType.class),
                any(SimpleString.class), anyBoolean());
    }

    @Test
    public void testCreateQueueWhenQueueAlreadyExists() throws Exception {
        // Given
        String address = "testAddress";
        String queueName = "testQueue";
        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt()))
                .thenReturn(session);
        when(session.queueQuery(any(SimpleString.class))).thenReturn(createQueueQuery(true));

        // When & Then
        try {
            brokerManager.createQueue(address, queueName);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ProvisioningException.class);
        }
    }

    @Test
    public void testCreateQueueWhenQueueExistsUnderDifferentAddress() throws Exception {
        // Given
        String address = "testAddress";
        String queueName = "testQueue";
        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt()))
                .thenReturn(session);
        when(session.queueQuery(any(SimpleString.class))).thenReturn(createQueueQuery(true, "differentAddress"));

        // When & Then
        try {
            brokerManager.createQueue(address, queueName);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ProvisioningException.class);
        }
    }

    @Test
    public void testConfigureAddressWithDLQAndExp() throws Exception {
        // Given
        String address = "testAddress";
        when(properties.isAutoBindDeadLetterAddress()).thenReturn(true);
        when(properties.isAutoBindExpiryAddress()).thenReturn(true);
        when(properties.getManagementAddress()).thenReturn("managementAddress");
        when(session.createMessage(false)).thenReturn(message);
        when(session.createRequestor(anyString(), anyString())).thenReturn(requestor);
        when(requestor.request(any(ClientMessage.class))).thenReturn(replyMessage);
        when(replyMessage.getBodyBuffer()).thenReturn(null);
        when(ManagementHelper.getResult(any(ClientMessage.class), any(Class.class))).thenReturn("{}");

        // When
        brokerManager.configureAddress(session, address, properties);

        // Then
        verify(session, times(2)).createSharedQueue(any(SimpleString.class), any(RoutingType.class),
                any(SimpleString.class), anyBoolean());
    }

    @Test
    public void testUpdateAddressSettings() throws Exception {
        // Given
        String address = "testAddress";
        when(properties.getManagementAddress()).thenReturn("managementAddress");
        when(session.createMessage(false)).thenReturn(message);
        when(session.createRequestor(anyString(), anyString())).thenReturn(requestor);
        when(requestor.request(any(ClientMessage.class))).thenReturn(replyMessage);
        when(replyMessage.getBodyBuffer()).thenReturn(null);
        when(ManagementHelper.getResult(any(ClientMessage.class), any(Class.class))).thenReturn("{}");

        // When
        brokerManager.updateAddressSettings(session, address, properties);

        // Then
        ArgumentCaptor<ClientMessage> messageCaptor = ArgumentCaptor.forClass(ClientMessage.class);
        verify(requestor).request(messageCaptor.capture());
        ClientMessage capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage).isNotNull();
        verify(session).createMessage(false);
    }

    @Test
    public void testGetCurrentAddressSettings() throws Exception {
        // Given
        String address = "testAddress";
        when(session.createMessage(false)).thenReturn(message);
        when(session.createRequestor(anyString(), anyString())).thenReturn(requestor);
        when(requestor.request(any(ClientMessage.class))).thenReturn(replyMessage);
        when(replyMessage.getBodyBuffer()).thenReturn(null);
        when(ManagementHelper.getResult(any(ClientMessage.class), any(Class.class))).thenReturn("{\"name\":\"testAddress\"}");

        // When
        AddressSettingsInfo result = brokerManager.getCurrentAddressSettings(session, requestor, address);

        // Then
        assertThat(result).isNotNull();
        verify(session).createMessage(false);
    }

    @Test
    public void testGetClientSessionWithUsername() throws Exception {
        // Given
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt()))
                .thenReturn(session);

        // When
        ClientSession result = brokerManager.getClientSession(sessionFactory);

        // Then
        assertThat(result).isEqualTo(session);
    }

    @Test
    public void testGetClientSessionWithoutUsername() throws Exception {
        // Given
        ArtemisBrokerManager managerWithoutUser = new ArtemisBrokerManager(serverLocator, null, null);
        when(sessionFactory.createSession()).thenReturn(session);

        // When
        ClientSession result = managerWithoutUser.getClientSession(sessionFactory);

        // Then
        assertThat(result).isEqualTo(session);
    }

    private ClientSession.AddressQuery createAddressQuery(boolean exists) {
        ClientSession.AddressQuery query = mock(ClientSession.AddressQuery.class);
        when(query.isExists()).thenReturn(exists);
        return query;
    }

    private ClientSession.QueueQuery createQueueQuery(boolean exists) {
        return createQueueQuery(exists, "testAddress");
    }

    private ClientSession.QueueQuery createQueueQuery(boolean exists, String address) {
        ClientSession.QueueQuery query = mock(ClientSession.QueueQuery.class);
        when(query.isExists()).thenReturn(exists);
        when(query.getAddress()).thenReturn(SimpleString.toSimpleString(address));
        return query;
    }
}
