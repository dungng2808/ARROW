package org.apache.activemq.artemis.core.config.impl;

import org.apache.activemq.artemis.api.core.BroadcastGroupConfiguration;
import org.apache.activemq.artemis.api.core.DiscoveryGroupConfiguration;
import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.BridgeConfiguration;
import org.apache.activemq.artemis.core.config.ClusterConnectionConfiguration;
import org.apache.activemq.artemis.core.config.CoreAddressConfiguration;
import org.apache.activemq.artemis.core.config.CoreQueueConfiguration;
import org.apache.activemq.artemis.core.config.DivertConfiguration;
import org.apache.activemq.artemis.core.config.FederationConfiguration;
import org.apache.activemq.artemis.core.config.HAPolicyConfiguration;
import org.apache.activemq.artemis.core.config.MetricsConfiguration;
import org.apache.activemq.artemis.core.config.StoreConfiguration;
import org.apache.activemq.artemis.core.config.TransformerConfiguration;
import org.apache.activemq.artemis.core.config.routing.ConnectionRouterConfiguration;
import org.apache.activemq.artemis.core.config.amqpBrokerConnectivity.AMQPBrokerConnectConfiguration;
import org.apache.activemq.artemis.core.config.ha.ReplicaPolicyConfiguration;
import org.apache.activemq.artemis.core.config.ha.ReplicatedPolicyConfiguration;
import org.apache.activemq.artemis.core.config.storage.DatabaseStorageConfiguration;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.core.server.JournalType;
import org.apache.activemq.artemis.core.server.NetworkHealthCheck;
import org.apache.activemq.artemis.core.server.group.impl.GroupingHandlerConfiguration;
import org.apache.activemq.artemis.core.server.metrics.ActiveMQMetricsPlugin;
import org.apache.activemq.artemis.core.server.plugin.ActiveMQServerAddressPlugin;
import org.apache.activemq.artemis.core.server.plugin.ActiveMQServerBasePlugin;
import org.apache.activemq.artemis.core.server.plugin.ActiveMQServerBindingPlugin;
import org.apache.activemq.artemis.core.server.plugin.ActiveMQServerBridgePlugin;
import org.apache.activemq.artemis.core.server.plugin.ActiveMQServerConnectionPlugin;
import org.apache.activemq.artemis.core.server.plugin.ActiveMQServerConsumerPlugin;
import org.apache.activemq.artemis.core.server.plugin.ActiveMQServerCriticalPlugin;
import org.apache.activemq.artemis.core.server.plugin.ActiveMQServerFederationPlugin;
import org.apache.activemq.artemis.core.server.plugin.ActiveMQServerMessagePlugin;
import org.apache.activemq.artemis.core.server.plugin.ActiveMQServerQueuePlugin;
import org.apache.activemq.artemis.core.server.plugin.ActiveMQServerResourcePlugin;
import org.apache.activemq.artemis.core.server.plugin.ActiveMQServerSessionPlugin;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.activemq.artemis.core.settings.impl.ResourceLimitSettings;
import org.apache.activemq.artemis.utils.JsonLoader;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URI;
import java.util.*;

import static org.junit.Assert.*;

public class ConfigurationImpl_ArrowTest {

    @Test
    public void testGettersAndSetters() {
        ConfigurationImpl config = new ConfigurationImpl();

        // Test basic setters/getters
        assertEquals("localhost", config.getName());
        config.setName("test-broker");
        assertEquals("test-broker", config.getName());

        assertEquals(true, config.isPersistenceEnabled());
        config.setPersistenceEnabled(false);
        assertEquals(false, config.isPersistenceEnabled());

        assertEquals(true, config.isJournalDatasync());
        config.setJournalDatasync(false);
        assertEquals(false, config.isJournalDatasync());

        assertEquals(1000L, config.getFileDeployerScanPeriod());
        config.setFileDeployerScanPeriod(2000L);
        assertEquals(2000L, config.getFileDeployerScanPeriod());

        assertEquals(true, config.isPersistDeliveryCountBeforeDelivery());
        config.setPersistDeliveryCountBeforeDelivery(false);
        assertEquals(false, config.isPersistDeliveryCountBeforeDelivery());

        assertEquals(10, config.getScheduledThreadPoolMaxSize());
        config.setScheduledThreadPoolMaxSize(20);
        assertEquals(20, config.getScheduledThreadPoolMaxSize());

        assertEquals(100, config.getThreadPoolMaxSize());
        config.setThreadPoolMaxSize(200);
        assertEquals(200, config.getThreadPoolMaxSize());

        assertEquals(10000L, config.getSecurityInvalidationInterval());
        config.setSecurityInvalidationInterval(20000L);
        assertEquals(20000L, config.getSecurityInvalidationInterval());

        assertEquals(1000L, config.getAuthenticationCacheSize());
        config.setAuthenticationCacheSize(2000L);
        assertEquals(2000L, config.getAuthenticationCacheSize());

        assertEquals(1000L, config.getAuthorizationCacheSize());
        config.setAuthorizationCacheSize(2000L);
        assertEquals(2000L, config.getAuthorizationCacheSize());

        assertEquals(true, config.isSecurityEnabled());
        config.setSecurityEnabled(false);
        assertEquals(false, config.isSecurityEnabled());

        assertEquals(true, config.isGracefulShutdownEnabled());
        config.setGracefulShutdownEnabled(false);
        assertEquals(false, config.isGracefulShutdownEnabled());

        assertEquals(30000L, config.getGracefulShutdownTimeout());
        config.setGracefulShutdownTimeout(60000L);
        assertEquals(60000L, config.getGracefulShutdownTimeout());

        assertEquals(true, config.isJMXManagementEnabled());
        config.setJMXManagementEnabled(false);
        assertEquals(false, config.isJMXManagementEnabled());

        assertEquals("org.apache.activemq.artemis", config.getJMXDomain());
        config.setJMXDomain("test.domain");
        assertEquals("test.domain", config.getJMXDomain());

        assertEquals(true, config.isJMXUseBrokerName());
        config.setJMXUseBrokerName(false);
        assertEquals(false, config.isJMXUseBrokerName());

        assertEquals(-1L, config.getConnectionTTLOverride());
        config.setConnectionTTLOverride(10000L);
        assertEquals(10000L, config.getConnectionTTLOverride());

        assertEquals(true, config.isAsyncConnectionExecutionEnabled());
        config.setEnabledAsyncConnectionExecution(false);
        assertEquals(false, config.isAsyncConnectionExecutionEnabled());

        assertEquals(30000L, config.getMessageExpiryScanPeriod());
        config.setMessageExpiryScanPeriod(60000L);
        assertEquals(60000L, config.getMessageExpiryScanPeriod());

        assertEquals(1, config.getMessageExpiryThreadPriority());
        config.setMessageExpiryThreadPriority(5);
        assertEquals(5, config.getMessageExpiryThreadPriority());

        assertEquals(10000L, config.getAddressQueueScanPeriod());
        config.setAddressQueueScanPeriod(20000L);
        assertEquals(20000L, config.getAddressQueueScanPeriod());

        assertEquals(1000, config.getIDCacheSize());
        config.setIDCacheSize(2000);
        assertEquals(2000, config.getIDCacheSize());

        assertEquals(true, config.isPersistIDCache());
        config.setPersistIDCache(false);
        assertEquals(false, config.isPersistIDCache());

        assertEquals("data/paging", config.getPagingDirectory());
        config.setPagingDirectory("custom/paging");
        assertEquals("custom/paging", config.getPagingDirectory());

        assertEquals(20, config.getPageMaxConcurrentIO());
        config.setPageMaxConcurrentIO(30);
        assertEquals(30, config.getPageMaxConcurrentIO());

        assertEquals(true, config.isReadWholePage());
        config.setReadWholePage(false);
        assertEquals(false, config.isReadWholePage());

        assertEquals("data/large-messages", config.getLargeMessagesDirectory());
        config.setLargeMessagesDirectory("custom/large-messages");
        assertEquals("custom/large-messages", config.getLargeMessagesDirectory());

        assertEquals("data/bindings", config.getBindingsDirectory());
        config.setBindingsDirectory("custom/bindings");
        assertEquals("custom/bindings", config.getBindingsDirectory());

        assertEquals(true, config.isCreateBindingsDir());
        config.setCreateBindingsDir(false);
        assertEquals(false, config.isCreateBindingsDir());

        assertEquals("data/journal", config.getJournalDirectory());
        config.setJournalDirectory("custom/journal");
        assertEquals("custom/journal", config.getJournalDirectory());

        assertEquals(JournalType.ASYNCIO, config.getJournalType());
        config.setJournalType(JournalType.NIO);
        assertEquals(JournalType.NIO, config.getJournalType());

        assertEquals(true, config.isJournalSyncTransactional());
        config.setJournalSyncTransactional(false);
        assertEquals(false, config.isJournalSyncTransactional());

        assertEquals(true, config.isJournalSyncNonTransactional());
        config.setJournalSyncNonTransactional(false);
        assertEquals(false, config.isJournalSyncNonTransactional());

        assertEquals(102400, config.getJournalFileSize());
        config.setJournalFileSize(204800);
        assertEquals(204800, config.getJournalFileSize());

        assertEquals(5, config.getJournalPoolFiles());
        config.setJournalPoolFiles(10);
        assertEquals(10, config.getJournalPoolFiles());

        assertEquals(2, config.getJournalMinFiles());
        config.setJournalMinFiles(4);
        assertEquals(4, config.getJournalMinFiles());

        assertEquals(true, config.isLogJournalWriteRate());
        config.setLogJournalWriteRate(false);
        assertEquals(false, config.isLogJournalWriteRate());

        assertEquals(true, config.isCreateJournalDir());
        config.setCreateJournalDir(false);
        assertEquals(false, config.isCreateJournalDir());

        assertEquals(true, config.isMessageCounterEnabled());
        config.setMessageCounterEnabled(false);
        assertEquals(false, config.isMessageCounterEnabled());

        assertEquals(10000L, config.getMessageCounterSamplePeriod());
        config.setMessageCounterSamplePeriod(20000L);
        assertEquals(20000L, config.getMessageCounterSamplePeriod());

        assertEquals(5, config.getMessageCounterMaxDayHistory());
        config.setMessageCounterMaxDayHistory(10);
        assertEquals(10, config.getMessageCounterMaxDayHistory());

        assertEquals(600000L, config.getTransactionTimeout());
        config.setTransactionTimeout(1200000L);
        assertEquals(1200000L, config.getTransactionTimeout());

        assertEquals(10000L, config.getTransactionTimeoutScanPeriod());
        config.setTransactionTimeoutScanPeriod(20000L);
        assertEquals(20000L, config.getTransactionTimeoutScanPeriod());

        assertEquals(new SimpleString("activemq.management"), config.getManagementAddress());
        config.setManagementAddress(new SimpleString("custom.management"));
        assertEquals(new SimpleString("custom.management"), config.getManagementAddress());

        assertEquals(new SimpleString("activemq.notifications"), config.getManagementNotificationAddress());
        config.setManagementNotificationAddress(new SimpleString("custom.notifications"));
        assertEquals(new SimpleString("custom.notifications"), config.getManagementNotificationAddress());

        assertEquals("admin", config.getClusterUser());
        config.setClusterUser("testuser");
        assertEquals("testuser", config.getClusterUser());

        assertEquals("admin", config.getClusterPassword());
        config.setClusterPassword("testpass");
        assertEquals("testpass", config.getClusterPassword());

        assertEquals(30000L, config.getServerDumpInterval());
        config.setServerDumpInterval(60000L);
        assertEquals(60000L, config.getServerDumpInterval());

        assertEquals(true, config.isFailoverOnServerShutdown());
        config.setFailoverOnServerShutdown(false);
        assertEquals(false, config.isFailoverOnServerShutdown());

        assertEquals(50, config.getMemoryWarningThreshold());
        config.setMemoryWarningThreshold(75);
        assertEquals(75, config.getMemoryWarningThreshold());

        assertEquals(30000L, config.getMemoryMeasureInterval());
        config.setMemoryMeasureInterval(60000L);
        assertEquals(60000L, config.getMemoryMeasureInterval());

        assertEquals(100, config.getJournalMaxIO_AIO());
        config.setJournalMaxIO_AIO(200);
        assertEquals(200, config.getJournalMaxIO_AIO());

        assertEquals(5000, config.getJournalBufferTimeout_AIO());
        config.setJournalBufferTimeout_AIO(10000);
        assertEquals(10000, config.getJournalBufferTimeout_AIO());

        assertEquals(102400, config.getJournalBufferSize_AIO());
        config.setJournalBufferSize_AIO(204800);
        assertEquals(204800, config.getJournalBufferSize_AIO());

        assertEquals(100, config.getJournalMaxIO_NIO());
        config.setJournalMaxIO_NIO(200);
        assertEquals(200, config.getJournalMaxIO_NIO());

        assertEquals(5000, config.getJournalBufferTimeout_NIO());
        config.setJournalBufferTimeout_NIO(10000);
        assertEquals(10000, config.getJournalBufferTimeout_NIO());

        assertEquals(102400, config.getJournalBufferSize_NIO());
        config.setJournalBufferSize_NIO(204800);
        assertEquals(204800, config.getJournalBufferSize_NIO());

        assertEquals(10, config.getJournalCompactMinFiles());
        config.setJournalCompactMinFiles(20);
        assertEquals(20, config.getJournalCompactMinFiles());

        assertEquals(50, config.getJournalCompactPercentage());
        config.setJournalCompactPercentage(75);
        assertEquals(75, config.getJournalCompactPercentage());

        assertEquals(10, config.getJournalFileOpenTimeout());
        config.setJournalFileOpenTimeout(20);
        assertEquals(20, config.getJournalFileOpenTimeout());

        assertEquals(100, config.getMaxDiskUsage());
        config.setMaxDiskUsage(200);
        assertEquals(200, config.getMaxDiskUsage());

        assertEquals(30000L, config.getConfigurationFileRefreshPeriod());
        config.setConfigurationFileRefreshPeriod(60000L);
        assertEquals(60000L, config.getConfigurationFileRefreshPeriod());

        assertEquals(null, config.getGlobalMaxSize());
        config.setGlobalMaxSize(1000000L);
        assertEquals(1000000L, config.getGlobalMaxSize());

        assertEquals(null, config.getGlobalMaxMessages());
        config.setGlobalMaxMessages(100000L);
        assertEquals(100000L, config.getGlobalMaxMessages());

        assertEquals(true, config.isAmqpUseCoreSubscriptionNaming());
        config.setAmqpUseCoreSubscriptionNaming(false);
        assertEquals(false, config.isAmqpUseCoreSubscriptionNaming());

        assertEquals("activemq.", config.getSystemPropertyPrefix());
        config.setSystemPropertyPrefix("test.");
        assertEquals("test.", config.getSystemPropertyPrefix());

        assertEquals("\"", config.getBrokerPropertiesKeySurround());
        config.setBrokerPropertiesKeySurround("'");
        assertEquals("'", config.getBrokerPropertiesKeySurround());

        assertEquals("127.0.0.1", config.getNetworkCheckList());
        config.setNetworkCheckList("192.168.1.1");
        assertEquals("192.168.1.1", config.getNetworkCheckList());

        assertEquals("http://localhost:8080", config.getNetworkURLList());
        config.setNetworkURLList("http://testhost:8080");
        assertEquals("http://testhost:8080", config.getNetworkURLList());

        assertEquals(30000L, config.getNetworkCheckPeriod());
        config.setNetworkCheckPeriod(60000L);
        assertEquals(60000L, config.getNetworkCheckPeriod());

        assertEquals(10000, config.getNetworkCheckTimeout());
        config.setNetworkCheckTimeout(20000);
        assertEquals(20000, config.getNetworkCheckTimeout());

        assertEquals("eth0", config.getNetworkCheckNIC());
        config.setNetworkCheckNIC("eth1");
        assertEquals("eth1", config.getNetworkCheckNIC());

        assertEquals(NetworkHealthCheck.IPV4_DEFAULT_COMMAND, config.getNetworkCheckPingCommand());
        config.setNetworkCheckPingCommand("ping -c 1 127.0.0.1");
        assertEquals("ping -c 1 127.0.0.1", config.getNetworkCheckPingCommand());

        assertEquals(NetworkHealthCheck.IPV6_DEFAULT_COMMAND, config.getNetworkCheckPing6Command());
        config.setNetworkCheckPing6Command("ping6 -c 1 ::1");
        assertEquals("ping6 -c 1 ::1", config.getNetworkCheckPing6Command());

        assertEquals("activemq.", config.getInternalNamingPrefix());
        config.setInternalNamingPrefix("test.");
        assertEquals("test.", config.getInternalNamingPrefix());

        assertEquals(true, config.isCriticalAnalyzer());
        config.setCriticalAnalyzer(false);
        assertEquals(false, config.isCriticalAnalyzer());

        assertEquals(null, config.getCriticalAnalyzerPolicy());
        config.setCriticalAnalyzerPolicy(CriticalAnalyzerPolicy.FAIL);
        assertEquals(CriticalAnalyzerPolicy.FAIL, config.getCriticalAnalyzerPolicy());

        assertEquals(120000L, config.getCriticalAnalyzerTimeout());
        config.setCriticalAnalyzerTimeout(240000L);
        assertEquals(240000L, config.getCriticalAnalyzerTimeout());

        assertEquals(0L, config.getCriticalAnalyzerCheckPeriod());
        config.setCriticalAnalyzerCheckPeriod(60000L);
        assertEquals(60000L, config.getCriticalAnalyzerCheckPeriod());

        assertEquals(5000, config.getPageSyncTimeout());
        config.setPageSyncTimeout(10000);
        assertEquals(10000, config.getPageSyncTimeout());

        assertEquals("activemq.", config.getTemporaryQueueNamespace());
        config.setTemporaryQueueNamespace("test.");
        assertEquals("test.", config.getTemporaryQueueNamespace());

        assertEquals(30000L, config.getMqttSessionScanInterval());
        config.setMqttSessionScanInterval(60000L);
        assertEquals(60000L, config.getMqttSessionScanInterval());

        assertEquals(false, config.isSuppressSessionNotifications());
        config.setSuppressSessionNotifications(true);
        assertEquals(true, config.isSuppressSessionNotifications());

        assertEquals(true, config.isMaskPassword());
        config.setMaskPassword(false);
        assertEquals(false, config.isMaskPassword());

        assertEquals(false, config.isResolveProtocols());
        config.setResolveProtocols(true);
        assertEquals(true, config.isResolveProtocols());

        assertEquals(true, config.isPopulateValidatedUser());
        config.setPopulateValidatedUser(false);
        assertEquals(false, config.isPopulateValidatedUser());

        assertEquals(true, config.isRejectEmptyValidatedUser());
        config.setRejectEmptyValidatedUser(false);
        assertEquals(false, config.isRejectEmptyValidatedUser());

        assertEquals(30000L, config.getConnectionTtlCheckInterval());
        config.setConnectionTtlCheckInterval(60000L);
        assertEquals(60000L, config.getConnectionTtlCheckInterval());

        assertEquals(0L, config.getJournalLockAcquisitionTimeout());
        config.setJournalLockAcquisitionTimeout(10000L);
        assertEquals(10000L, config.getJournalLockAcquisitionTimeout());

        assertEquals(null, config.getHaPolicyConfiguration());
        config.setHaPolicyConfiguration(Mockito.mock(HAPolicyConfiguration.class));
        assertNotNull(config.getHaPolicyConfiguration());

        assertEquals(null, config.getStoreConfiguration());
        config.setStoreConfiguration(Mockito.mock(StoreConfiguration.class));
        assertNotNull(config.getStoreConfiguration());
    }

    @Test
    public void testCollections() {
        ConfigurationImpl config = new ConfigurationImpl();

        // Test lists
        assertNotNull(config.getIncomingInterceptorClassNames());
        assertNotNull(config.getOutgoingInterceptorClassNames());
        assertNotNull(config.getBridgeConfigurations());
        assertNotNull(config.getDivertConfigurations());
        assertNotNull(config.getConnectionRouters());
        assertNotNull(config.getClusterConfigurations());
        assertNotNull(config.getAMQPConnections());
        assertNotNull(config.getFederationConfigurations());
        assertNotNull(config.getQueueConfigurations());
        assertNotNull(config.getQueueConfigs());
        assertNotNull(config.getAddressConfigurations());
        assertNotNull(config.getConnectorServiceConfigurations());
        assertNotNull(config.getSecuritySettingPlugins());
        assertNotNull(config.getBrokerPlugins());
        assertNotNull(config.getBrokerConnectionPlugins());
        assertNotNull(config.getBrokerSessionPlugins());
        assertNotNull(config.getBrokerConsumerPlugins());
        assertNotNull(config.getBrokerAddressPlugins());
        assertNotNull(config.getBrokerQueuePlugins());
        assertNotNull(config.getBrokerBindingPlugins());
        assertNotNull(config.getBrokerMessagePlugins());
        assertNotNull(config.getBrokerBridgePlugins());
        assertNotNull(config.getBrokerCriticalPlugins());
        assertNotNull(config.getBrokerFederationPlugins());
        assertNotNull(config.getBrokerResourcePlugins());

        // Test maps
        assertNotNull(config.getConnectorConfigurations());
        assertNotNull(config.getAcceptors());
        assertNotNull(config.getAddressSettings());
        assertNotNull(config.getResourceLimitSettings());
        assertNotNull(config.getSecurityRoles());
        assertNotNull(config.getSecurityRoleNameMappings());
        assertNotNull(config.getDiscoveryGroupConfigurations());

        // Test adding elements
        TransportConfiguration transportConfig = new TransportConfiguration(InVMConnectorFactory.class.getName());
        config.addConnectorConfiguration("test", transportConfig);
        assertEquals(1, config.getConnectorConfigurations().size());
        assertTrue(config.getConnectorConfigurations().containsKey("test"));

        config.addAcceptorConfiguration(transportConfig);
        assertEquals(1, config.getAcceptorConfigurations().size());

        BridgeConfiguration bridgeConfig = Mockito.mock(BridgeConfiguration.class);
        config.addBridgeConfiguration(bridgeConfig);
        assertEquals(1, config.getBridgeConfigurations().size());

        DivertConfiguration divertConfig = Mockito.mock(DivertConfiguration.class);
        config.addDivertConfiguration(divertConfig);
        assertEquals(1, config.getDivertConfigurations().size());

        ConnectionRouterConfiguration routerConfig = Mockito.mock(ConnectionRouterConfiguration.class);
        config.addConnectionRouter(routerConfig);
        assertEquals(1, config.getConnectionRouters().size());

        ClusterConnectionConfiguration clusterConfig = Mockito.mock(ClusterConnectionConfiguration.class);
        config.addClusterConfiguration(clusterConfig);
        assertEquals(1, config.getClusterConfigurations().size());

        AMQPBrokerConnectConfiguration amqpConfig = Mockito.mock(AMQPBrokerConnectConfiguration.class);
        config.addAMQPConnection(amqpConfig);
        assertEquals(1, config.getAMQPConnections().size());

        FederationConfiguration federationConfig = Mockito.mock(FederationConfiguration.class);
        config.addFederationConfiguration(federationConfig);
        assertEquals(1, config.getFederationConfigurations().size());

        CoreQueueConfiguration queueConfig = Mockito.mock(CoreQueueConfiguration.class);
        config.addQueueConfiguration(queueConfig);
        assertEquals(1, config.getQueueConfigurations().size());

        QueueConfiguration queueConfig2 = Mockito.mock(QueueConfiguration.class);
        config.addQueueConfiguration(queueConfig2);
        assertEquals(1, config.getQueueConfigs().size());

        CoreAddressConfiguration addressConfig = Mockito.mock(CoreAddressConfiguration.class);
        config.addAddressConfiguration(addressConfig);
        assertEquals(1, config.getAddressConfigurations().size());

        DiscoveryGroupConfiguration discoveryConfig = Mockito.mock(DiscoveryGroupConfiguration.class);
        config.addDiscoveryGroupConfiguration("test", discoveryConfig);
        assertEquals(1, config.getDiscoveryGroupConfigurations().size());

        // Test clearing collections
        config.clearConnectorConfigurations();
        assertEquals(0, config.getConnectorConfigurations().size());

        config.clearAcceptorConfigurations();
        assertEquals(0, config.getAcceptorConfigurations().size());

        config.clearClusterConfigurations();
        assertEquals(0, config.getClusterConfigurations().size());
    }

    @Test
    public void testNullValues() {
        ConfigurationImpl config = new ConfigurationImpl();

        // Test that default values are not null
        assertNotNull(config.getName());
        assertNotNull(config.getJournalDirectory());
        assertNotNull(config.getBindingsDirectory());
        assertNotNull(config.getLargeMessagesDirectory());
        assertNotNull(config.getPagingDirectory());
        assertNotNull(config.getJMXDomain());
        assertNotNull(config.getClusterUser());
        assertNotNull(config.getClusterPassword());
        assertNotNull(config.getManagementAddress());
        assertNotNull(config.getManagementNotificationAddress());
        assertNotNull(config.getSystemPropertyPrefix());
        assertNotNull(config.getBrokerPropertiesKeySurround());
        assertNotNull(config.getNetworkCheckList());
        assertNotNull(config.getNetworkURLList());
        assertNotNull(config.getNetworkCheckNIC());
        assertNotNull(config.getNetworkCheckPingCommand());
        assertNotNull(config.getNetworkCheckPing6Command());
        assertNotNull(config.getInternalNamingPrefix());
        assertNotNull(config.getTemporaryQueueNamespace());
        assertNotNull(config.getJsonStatus());
        assertNotNull(config.getCheckSun());
    }

    @Test
    public void testToString() {
        ConfigurationImpl config = new ConfigurationImpl();
        String str = config.toString();
        assertNotNull(str);
        assertTrue(str.contains("ConfigurationImpl"));
    }

    @Test
    public void testGettersWithDefaults() {
        ConfigurationImpl config = new ConfigurationImpl();

        // Test that all getters return reasonable defaults
        assertEquals("localhost", config.getName());
        assertEquals(true, config.isPersistenceEnabled());
        assertEquals(true, config.isJournalDatasync());
        assertEquals(1000L, config.getFileDeployerScanPeriod());
        assertEquals(true, config.isPersistDeliveryCountBeforeDelivery());
        assertEquals(10, config.getScheduledThreadPoolMaxSize());
        assertEquals(100, config.getThreadPoolMaxSize());
        assertEquals(10000L, config.getSecurityInvalidationInterval());
        assertEquals(1000L, config.getAuthenticationCacheSize());
        assertEquals(1000L, config.getAuthorizationCacheSize());
        assertEquals(true, config.isSecurityEnabled());
        assertEquals(true, config.isGracefulShutdownEnabled());
        assertEquals(30000L, config.getGracefulShutdownTimeout());
        assertEquals(true, config.isJMXManagementEnabled());
        assertEquals("org.apache.activemq.artemis", config.getJMXDomain());
        assertEquals(true, config.isJMXUseBrokerName());
        assertEquals(-1L, config.getConnectionTTLOverride());
        assertEquals(true, config.isAsyncConnectionExecutionEnabled());
        assertEquals(30000L, config.getMessageExpiryScanPeriod());
        assertEquals(1, config.getMessageExpiryThreadPriority());
        assertEquals(10000L, config.getAddressQueueScanPeriod());
        assertEquals(1000, config.getIDCacheSize());
        assertEquals(true, config.isPersistIDCache());
        assertEquals("data/paging", config.getPagingDirectory());
        assertEquals(20, config.getPageMaxConcurrentIO());
        assertEquals(true, config.isReadWholePage());
        assertEquals("data/large-messages", config.getLargeMessagesDirectory());
        assertEquals("data/bindings", config.getBindingsDirectory());
        assertEquals(true, config.isCreateBindingsDir());
        assertEquals("data/journal", config.getJournalDirectory());
        assertEquals(JournalType.ASYNCIO, config.getJournalType());
        assertEquals(true, config.isJournalSyncTransactional());
        assertEquals(true, config.isJournalSyncNonTransactional());
        assertEquals(102400, config.getJournalFileSize());
        assertEquals(5, config.getJournalPoolFiles());
        assertEquals(2, config.getJournalMinFiles());
        assertEquals(true, config.isLogJournalWriteRate());
        assertEquals(true, config.isCreateJournalDir());
        assertEquals(true, config.isMessageCounterEnabled());
        assertEquals(10000L, config.getMessageCounterSamplePeriod());
        assertEquals(5, config.getMessageCounterMaxDayHistory());
        assertEquals(600000L, config.getTransactionTimeout());
        assertEquals(10000L, config.getTransactionTimeoutScanPeriod());
        assertEquals(new SimpleString("activemq.management"), config.getManagementAddress());
        assertEquals(new SimpleString("activemq.notifications"), config.getManagementNotificationAddress());
        assertEquals("admin", config.getClusterUser());
        assertEquals("admin", config.getClusterPassword());
        assertEquals(30000L, config.getServerDumpInterval());
        assertEquals(true, config.isFailoverOnServerShutdown());
        assertEquals(50, config.getMemoryWarningThreshold());
        assertEquals(30000L, config.getMemoryMeasureInterval());
        assertEquals(100, config.getJournalMaxIO_AIO());
        assertEquals(5000, config.getJournalBufferTimeout_AIO());
        assertEquals(102400, config.getJournalBufferSize_AIO());
        assertEquals(100, config.getJournalMaxIO_NIO());
        assertEquals(5000, config.getJournalBufferTimeout_NIO());
        assertEquals(102400, config.getJournalBufferSize_NIO());
        assertEquals(10, config.getJournalCompactMinFiles());
        assertEquals(50, config.getJournalCompactPercentage());
        assertEquals(10, config.getJournalFileOpenTimeout());
        assertEquals(100, config.getMaxDiskUsage());
        assertEquals(30000L, config.getConfigurationFileRefreshPeriod());
        assertEquals(null, config.getGlobalMaxSize());
        assertEquals(null, config.getGlobalMaxMessages());
        assertEquals(true, config.isAmqpUseCoreSubscriptionNaming());
        assertEquals("activemq.", config.getSystemPropertyPrefix());
        assertEquals("\"", config.getBrokerPropertiesKeySurround());
        assertEquals("127.0.0.1", config.getNetworkCheckList());
        assertEquals("http://localhost:8080", config.getNetworkURLList());
        assertEquals(30000L, config.getNetworkCheckPeriod());
        assertEquals(10000, config.getNetworkCheckTimeout());
        assertEquals("eth0", config.getNetworkCheckNIC());
        assertEquals(NetworkHealthCheck.IPV4_DEFAULT_COMMAND, config.getNetworkCheckPingCommand());
        assertEquals(NetworkHealthCheck.IPV6_DEFAULT_COMMAND, config.getNetworkCheckPing6Command());
        assertEquals("activemq.", config.getInternalNamingPrefix());
        assertEquals(true, config.isCriticalAnalyzer());
        assertEquals(null, config.getCriticalAnalyzerPolicy());
        assertEquals(120000L, config.getCriticalAnalyzerTimeout());
        assertEquals(0L, config.getCriticalAnalyzerCheckPeriod());
        assertEquals(5000, config.getPageSyncTimeout());
        assertEquals("activemq.", config.getTemporaryQueueNamespace());
        assertEquals(30000L, config.getMqttSessionScanInterval());
        assertEquals(false, config.isSuppressSessionNotifications());
        assertEquals(true, config.isMaskPassword());
        assertEquals(false, config.isResolveProtocols());
        assertEquals(true, config.isPopulateValidatedUser());
        assertEquals(true, config.isRejectEmptyValidatedUser());
        assertEquals(30000L, config.getConnectionTtlCheckInterval());
        assertEquals(0L, config.getJournalLockAcquisitionTimeout());
        assertEquals(null, config.getHaPolicyConfiguration());
        assertEquals(null, config.getStoreConfiguration());
    }
}
