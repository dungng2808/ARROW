package me.snowdrop.stream.binder.artemis.provisioning;

import me.snowdrop.stream.binder.artemis.properties.ArtemisCommonProperties;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.*;
import org.apache.activemq.artemis.api.core.management.AddressSettingsInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ArtemisBrokerManager_ArrowTest {

    private ServerLocator serverLocator;
    private ArtemisBrokerManager artemisBrokerManager;
    private ArtemisCommonProperties properties;

    @Before
    public void setUp() {
        serverLocator = Mockito.mock(ServerLocator.class);
        artemisBrokerManager = new ArtemisBrokerManager(serverLocator, "testUser", "testPassword");
        properties = new ArtemisCommonProperties();
        properties.setModifyAddressSettings(true);
        properties.setAutoBindDeadLetterAddress(true);
        properties.setAutoBindExpiryAddress(true);
        properties.setBrokerExpiryDelay(1000L);
        properties.setBrokerMaxDeliveryAttempts(5);
        properties.setBrokerRedeliveryDelay(2000L);
        properties.setBrokerRedeliveryDelayMultiplier(2.0);
        properties.setBrokerMaxRedeliveryDelay(10000L);
        properties.setBrokerSendToDlaOnNoRoute(true);
        properties.setManagementAddress("activemq.management");
    }

    @Test
    public void testCreateAddressWhenAddressDoesNotExist() throws Exception {
        ClientSessionFactory sessionFactory = Mockito.mock(ClientSessionFactory.class);
        ClientSession session = Mockito.mock(ClientSession.class);
        ClientSession.AddressQuery addressQuery = Mockito.mock(ClientSession.AddressQuery.class);
        
        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt())).thenReturn(session);
        when(session.addressQuery(any(SimpleString.class))).thenReturn(addressQuery);
        when(addressQuery.isExists()).thenReturn(false);
        
        artemisBrokerManager.createAddress("testAddress", properties);
        
        verify(session).start();
        verify(session).createAddress(any(SimpleString.class), any(), anyBoolean());
        verify(session).stop();
    }

    @Test
    public void testCreateAddressWhenAddressAlreadyExists() throws Exception {
        ClientSessionFactory sessionFactory = Mockito.mock(ClientSessionFactory.class);
        ClientSession session = Mockito.mock(ClientSession.class);
        ClientSession.AddressQuery addressQuery = Mockito.mock(ClientSession.AddressQuery.class);
        
        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt())).thenReturn(session);
        when(session.addressQuery(any(SimpleString.class))).thenReturn(addressQuery);
        when(addressQuery.isExists()).thenReturn(true);
        
        artemisBrokerManager.createAddress("testAddress", properties);
        
        verify(session).start();
        verify(session, never()).createAddress(any(SimpleString.class), any(), anyBoolean());
        verify(session).stop();
    }

    @Test
    public void testCreateQueue() throws Exception {
        ClientSessionFactory sessionFactory = Mockito.mock(ClientSessionFactory.class);
        ClientSession session = Mockito.mock(ClientSession.class);
        
        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt())).thenReturn(session);
        doNothing().when(session).start();
        doNothing().when(session).stop();
        
        artemisBrokerManager.createQueue("testAddress", "testQueue");
        
        verify(session).start();
        verify(session).createSharedQueue(any(SimpleString.class), any(), any(SimpleString.class), anyBoolean());
        verify(session).stop();
    }

    @Test(expected = ProvisioningException.class)
    public void testCreateQueueWithDifferentAddress() throws Exception {
        ClientSessionFactory sessionFactory = Mockito.mock(ClientSessionFactory.class);
        ClientSession session = Mockito.mock(ClientSession.class);
        ClientSession.QueueQuery queueQuery = Mockito.mock(ClientSession.QueueQuery.class);
        
        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt())).thenReturn(session);
        when(session.queueQuery(any(SimpleString.class))).thenReturn(queueQuery);
        when(queueQuery.isExists()).thenReturn(true);
        when(queueQuery.getAddress()).thenReturn(SimpleString.toSimpleString("differentAddress"));
        
        artemisBrokerManager.createQueue("testAddress", "testQueue");
    }

    @Test
    public void testConfigureAddress() throws Exception {
        ClientSessionFactory sessionFactory = Mockito.mock(ClientSessionFactory.class);
        ClientSession session = Mockito.mock(ClientSession.class);
        ClientRequestor requestor = Mockito.mock(ClientRequestor.class);
        AddressSettingsInfo addressSettingsInfo = Mockito.mock(AddressSettingsInfo.class);
        
        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt())).thenReturn(session);
        when(session.createMessage(anyBoolean())).thenReturn(Mockito.mock(ClientMessage.class));
        when(session.createRequestor(anyString())).thenReturn(requestor);
        when(addressSettingsInfo.getDeadLetterAddress()).thenReturn("test.dlq");
        when(addressSettingsInfo.getExpiryAddress()).thenReturn("test.exp");
        when(addressSettingsInfo.isLastValueQueue()).thenReturn(false);
        when(addressSettingsInfo.getMaxSizeBytes()).thenReturn(-1L);
        when(addressSettingsInfo.getPageSizeBytes()).thenReturn(1024L);
        when(addressSettingsInfo.getPageCacheMaxSize()).thenReturn(5);
        when(addressSettingsInfo.getRedistributionDelay()).thenReturn(-1L);
        when(addressSettingsInfo.getAddressFullMessagePolicy()).thenReturn(null);
        when(addressSettingsInfo.getSlowConsumerThreshold()).thenReturn(-1L);
        when(addressSettingsInfo.getSlowConsumerCheckPeriod()).thenReturn(-1L);
        when(addressSettingsInfo.getSlowConsumerPolicy()).thenReturn(null);
        when(addressSettingsInfo.isAutoCreateJmsQueues()).thenReturn(false);
        when(addressSettingsInfo.isAutoDeleteJmsQueues()).thenReturn(false);
        when(addressSettingsInfo.isAutoCreateJmsTopics()).thenReturn(false);
        when(addressSettingsInfo.isAutoDeleteJmsTopics()).thenReturn(false);
        
        doNothing().when(session).start();
        doNothing().when(session).stop();
        doReturn(addressSettingsInfo).when(artemisBrokerManager).getCurrentAddressSettings(any(ClientSession.class), any(ClientRequestor.class), anyString());
        
        artemisBrokerManager.createAddress("testAddress", properties);
        
        verify(session, times(2)).createSharedQueue(any(SimpleString.class), any(), any(SimpleString.class), anyBoolean());
    }

    @Test
    public void testGetClientSessionWithNullUsername() throws Exception {
        ArtemisBrokerManager manager = new ArtemisBrokerManager(serverLocator, null, "testPassword");
        ClientSessionFactory sessionFactory = Mockito.mock(ClientSessionFactory.class);
        ClientSession session = Mockito.mock(ClientSession.class);
        
        when(sessionFactory.createSession()).thenReturn(session);
        ClientSession result = manager.getClientSession(sessionFactory);
        
        assertEquals(session, result);
    }

    @Test
    public void testGetClientSessionWithUsernameAndPassword() throws Exception {
        ClientSessionFactory sessionFactory = Mockito.mock(ClientSessionFactory.class);
        ClientSession session = Mockito.mock(ClientSession.class);
        
        when(sessionFactory.createSession(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt())).thenReturn(session);
        ClientSession result = artemisBrokerManager.getClientSession(sessionFactory);
        
        assertEquals(session, result);
    }
}
