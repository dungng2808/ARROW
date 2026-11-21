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
import org.mockito.ArgumentCaptor;

import me.snowdrop.stream.binder.artemis.properties.ArtemisCommonProperties;
import org.springframework.cloud.stream.provisioning.ProvisioningException;

public class ArtemisBrokerManager_ArrowTest {

    private ArtemisBrokerManager artemisBrokerManager;
    private ServerLocator serverLocator;
    private ClientSessionFactory sessionFactory;
    private ClientSession session;
    private ArtemisCommonProperties properties;

    @Before
    public void setUp() throws Exception {
        serverLocator = mock(ServerLocator.class);
        sessionFactory = mock(ClientSessionFactory.class);
        session = mock(ClientSession.class);
        properties = mock(ArtemisCommonProperties.class);
        
        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession()).thenReturn(session);
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt()))
                .thenReturn(session);
        
        artemisBrokerManager = new ArtemisBrokerManager(serverLocator, "testUser", "testPassword");
    }

    @Test
    public void testCreateAddress_WhenAddressDoesNotExist_ShouldCreateAddressAndConfigure() throws Exception {
        // Given
        String addressName = "testAddress";
        when(session.addressQuery(any(SimpleString.class))).thenReturn(mock(ClientSession.AddressQuery.class));
        when(session.addressQuery(any(SimpleString.class)).isExists()).thenReturn(false);
        when(properties.isModifyAddressSettings()).thenReturn(true);
        
        // When
        artemisBrokerManager.createAddress(addressName, properties);
        
        // Then
        verify(session).start();
        verify(session).createAddress(any(SimpleString.class), eq(RoutingType.MULTICAST), anyBoolean());
        verify(session).stop();
    }

    @Test
    public void testCreateAddress_WhenAddressExists_ShouldNotCreateAddress() throws Exception {
        // Given
        String addressName = "testAddress";
        when(session.addressQuery(any(SimpleString.class))).thenReturn(mock(ClientSession.AddressQuery.class));
        when(session.addressQuery(any(SimpleString.class)).isExists()).thenReturn(true);
        when(properties.isModifyAddressSettings()).thenReturn(false);
        
        // When
        artemisBrokerManager.createAddress(addressName, properties);
        
        // Then
        verify(session).start();
        verify(session, never()).createAddress(any(SimpleString.class), any(RoutingType.class), anyBoolean());
        verify(session).stop();
    }

    @Test
    public void testCreateQueue_WhenQueueDoesNotExist_ShouldCreateQueue() throws Exception {
        // Given
        String address = "testAddress";
        String queueName = "testQueue";
        ClientSession.QueueQuery queueQuery = mock(ClientSession.QueueQuery.class);
        when(session.queueQuery(any(SimpleString.class))).thenReturn(queueQuery);
        when(queueQuery.isExists()).thenReturn(false);
        
        // When
        artemisBrokerManager.createQueue(address, queueName);
        
        // Then
        verify(session).createSharedQueue(any(SimpleString.class), eq(RoutingType.MULTICAST),
                any(SimpleString.class), anyBoolean());
    }

    @Test(expected = ProvisioningException.class)
    public void testCreateQueue_WhenQueueExistsUnderDifferentAddress_ShouldThrowException() throws Exception {
        // Given
        String address = "testAddress";
        String queueName = "testQueue";
        ClientSession.QueueQuery queueQuery = mock(ClientSession.QueueQuery.class);
        when(session.queueQuery(any(SimpleString.class))).thenReturn(queueQuery);
        when(queueQuery.isExists()).thenReturn(true);
        when(queueQuery.getAddress()).thenReturn(SimpleString.toSimpleString("differentAddress"));
        
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
        ClientSession.QueueQuery queueQuery = mock(ClientSession.QueueQuery.class);
        when(session.queueQuery(any(SimpleString.class))).thenReturn(queueQuery);
        when(queueQuery.isExists()).thenReturn(true);
        when(queueQuery.getAddress()).thenReturn(SimpleString.toSimpleString(address));
        
        // When
        artemisBrokerManager.createQueue(address, queueName);
        
        // Then
        verify(session, never()).createSharedQueue(any(SimpleString.class), any(RoutingType.class),
                any(SimpleString.class), anyBoolean());
    }

    @Test
    public void testConfigureAddress_WithAutoBindDLQAndExpiry_ShouldCreateQueues() throws Exception {
        // Given
        String address = "testAddress";
        when(properties.isAutoBindDeadLetterAddress()).thenReturn(true);
        when(properties.isAutoBindExpiryAddress()).thenReturn(true);
        when(properties.getManagementAddress()).thenReturn("managementAddress");
        
        ClientRequestor requestor = mock(ClientRequestor.class);
        when(session.createMessage(anyBoolean())).thenReturn(mock(ClientMessage.class));
        when(session.createMessage(anyBoolean())).thenReturn(mock(ClientMessage.class));
        when(session.createMessage(anyBoolean())).thenReturn(mock(ClientMessage.class));
        
        AddressSettingsInfo addressSettingsInfo = mock(AddressSettingsInfo.class);
        when(addressSettingsInfo.getDeadLetterAddress()).thenReturn("dlq");
        when(addressSettingsInfo.getExpiryAddress()).thenReturn("exp");
        when(addressSettingsInfo.isLastValueQueue()).thenReturn(false);
        when(addressSettingsInfo.getMaxSizeBytes()).thenReturn(-1L);
        when(addressSettingsInfo.getPageSizeBytes()).thenReturn(1024L);
        when(addressSettingsInfo.getPageCacheMaxSize()).thenReturn(5);
        when(addressSettingsInfo.getRedistributionDelay()).thenReturn(-1L);
        when(addressSettingsInfo.getAddressFullMessagePolicy()).thenReturn(null);
        when(addressSettingsInfo.getSlowConsumerThreshold()).thenReturn(-1L);
        when(addressSettingsInfo.getSlowConsumerCheckPeriod()).thenReturn(5L);
        when(addressSettingsInfo.getSlowConsumerPolicy()).thenReturn(null);
        when(addressSettingsInfo.isAutoCreateJmsQueues()).thenReturn(false);
        when(addressSettingsInfo.isAutoDeleteJmsQueues()).thenReturn(false);
        when(addressSettingsInfo.isAutoCreateJmsTopics()).thenReturn(false);
        when(addressSettingsInfo.isAutoDeleteJmsTopics()).thenReturn(false);
        
        // When
        artemisBrokerManager.getClass().getDeclaredMethod("configureAddress", ClientSession.class, String.class, ArtemisCommonProperties.class)
                .invoke(artemisBrokerManager, session, address, properties);
        
        // Then
        verify(session, times(2)).createSharedQueue(any(SimpleString.class), eq(RoutingType.MULTICAST),
                any(SimpleString.class), anyBoolean());
    }

    @Test
    public void testUpdateAddressSettings() throws Exception {
        // Given
        String address = "testAddress";
        when(properties.getManagementAddress()).thenReturn("managementAddress");
        when(properties.getBrokerExpiryDelay()).thenReturn(1000L);
        when(properties.getBrokerMaxDeliveryAttempts()).thenReturn(5);
        when(properties.getBrokerRedeliveryDelay()).thenReturn(1000L);
        when(properties.getBrokerRedeliveryDelayMultiplier()).thenReturn(2.0);
        when(properties.getBrokerMaxRedeliveryDelay()).thenReturn(10000L);
        when(properties.isBrokerSendToDlaOnNoRoute()).thenReturn(true);
        
        ClientRequestor requestor = mock(ClientRequestor.class);
        when(session.createMessage(anyBoolean())).thenReturn(mock(ClientMessage.class));
        when(session.createMessage(anyBoolean())).thenReturn(mock(ClientMessage.class));
        
        AddressSettingsInfo addressSettingsInfo = mock(AddressSettingsInfo.class);
        when(addressSettingsInfo.getDeadLetterAddress()).thenReturn("dlq");
        when(addressSettingsInfo.getExpiryAddress()).thenReturn("exp");
        when(addressSettingsInfo.isLastValueQueue()).thenReturn(false);
        when(addressSettingsInfo.getMaxSizeBytes()).thenReturn(-1L);
        when(addressSettingsInfo.getPageSizeBytes()).thenReturn(1024L);
        when(addressSettingsInfo.getPageCacheMaxSize()).thenReturn(5);
        when(addressSettingsInfo.getRedistributionDelay()).thenReturn(-1L);
        when(addressSettingsInfo.getAddressFullMessagePolicy()).thenReturn(null);
        when(addressSettingsInfo.getSlowConsumerThreshold()).thenReturn(-1L);
        when(addressSettingsInfo.getSlowConsumerCheckPeriod()).thenReturn(5L);
        when(addressSettingsInfo.getSlowConsumerPolicy()).thenReturn(null);
        when(addressSettingsInfo.isAutoCreateJmsQueues()).thenReturn(false);
        when(addressSettingsInfo.isAutoDeleteJmsQueues()).thenReturn(false);
        when(addressSettingsInfo.isAutoCreateJmsTopics()).thenReturn(false);
        when(addressSettingsInfo.isAutoDeleteJmsTopics()).thenReturn(false);
        
        // When
        artemisBrokerManager.getClass().getDeclaredMethod("updateAddressSettings", ClientSession.class, String.class, ArtemisCommonProperties.class)
                .invoke(artemisBrokerManager, session, address, properties);
        
        // Then
        verify(session, times(1)).createMessage(anyBoolean());
        verify(requestor, times(1)).request(any(ClientMessage.class));
    }

    @Test
    public void testGetClientSession_WithNullUsername_ShouldCreateSessionWithoutCredentials() throws Exception {
        // Given
        ArtemisBrokerManager manager = new ArtemisBrokerManager(serverLocator, null, null);
        
        // When
        ClientSession result = manager.getClass().getDeclaredMethod("getClientSession", ClientSessionFactory.class)
                .invoke(manager, sessionFactory);
        
        // Then
        assertEquals(session, result);
    }

    @Test
    public void testGetClientSession_WithUsernameAndPassword_ShouldCreateSessionWithCredentials() throws Exception {
        // Given
        ArtemisBrokerManager manager = new ArtemisBrokerManager(serverLocator, "testUser", "testPass");
        
        // When
        ClientSession result = manager.getClass().getDeclaredMethod("getClientSession", ClientSessionFactory.class)
                .invoke(manager, sessionFactory);
        
        // Then
        assertEquals(session, result);
    }
}
