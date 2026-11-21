package org.apache.activemq.artemis.core.config.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.server.JournalType;
import org.junit.Test;

public class ConfigurationImpl_ArrowTest {

   @Test
   public void nameCanBeSetAndRetrieved() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      assertSame(configuration, configuration.setName("broker"));
      assertEquals("broker", configuration.getName());
   }

   @Test
   public void persistenceCanBeDisabled() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      assertSame(configuration, configuration.setPersistenceEnabled(false));
      assertFalse(configuration.isPersistenceEnabled());
   }

   @Test
   public void journalTypeCanBeSetAndRetrieved() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      assertSame(configuration, configuration.setJournalType(JournalType.NIO));
      assertEquals(JournalType.NIO, configuration.getJournalType());
   }

   @Test
   public void journalDirectoryLocationUsesConfiguredDirectory() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      configuration.setJournalDirectory("journal-test");
      assertEquals("journal-test", configuration.getJournalDirectory());
      assertEquals(new java.io.File("journal-test"), configuration.getJournalLocation());
   }

   @Test
   public void pagingDirectoryLocationUsesConfiguredDirectory() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      configuration.setPagingDirectory("paging-test");
      assertEquals("paging-test", configuration.getPagingDirectory());
      assertEquals(new java.io.File("paging-test"), configuration.getPagingLocation());
   }

   @Test
   public void largeMessagesDirectoryLocationUsesConfiguredDirectory() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      configuration.setLargeMessagesDirectory("large-test");
      assertEquals("large-test", configuration.getLargeMessagesDirectory());
      assertEquals(new java.io.File("large-test"), configuration.getLargeMessagesLocation());
   }

   @Test
   public void bindingsDirectoryLocationUsesConfiguredDirectory() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      configuration.setBindingsDirectory("bindings-test");
      assertEquals("bindings-test", configuration.getBindingsDirectory());
      assertEquals(new java.io.File("bindings-test"), configuration.getBindingsLocation());
   }

   @Test
   public void journalRetentionDirectoryAndPeriodCanBeSet() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      configuration.setJournalRetentionDirectory("retention-test");
      configuration.setJournalRetentionPeriod(java.util.concurrent.TimeUnit.SECONDS, 3);
      assertEquals("retention-test", configuration.getJournalRetentionDirectory());
      assertEquals(new java.io.File("retention-test"), configuration.getJournalRetentionLocation());
      assertEquals(3000L, configuration.getJournalRetentionPeriod());
   }

   @Test
   public void journalRetentionMaxBytesCanBeSet() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      assertSame(configuration, configuration.setJournalRetentionMaxBytes(1234L));
      assertEquals(1234L, configuration.getJournalRetentionMaxBytes());
   }

   @Test
   public void globalLimitsCanBeSet() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      configuration.setGlobalMaxSize(100L);
      configuration.setGlobalMaxMessages(20L);
      assertEquals(100L, configuration.getGlobalMaxSize());
      assertEquals(20L, configuration.getGlobalMaxMessages());
   }

   @Test
   public void maxDiskUsageCanBeSet() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      assertSame(configuration, configuration.setMaxDiskUsage(75));
      assertEquals(75, configuration.getMaxDiskUsage());
   }

   @Test
   public void systemPropertyPrefixCanBeSet() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      configuration.setSystemPropertyPrefix("custom.");
      assertEquals("custom.", configuration.getSystemPropertyPrefix());
   }

   @Test
   public void brokerPropertiesKeySurroundCanBeSet() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      configuration.setBrokerPropertiesKeySurround("##");
      assertEquals("##", configuration.getBrokerPropertiesKeySurround());
   }

   @Test
   public void interceptorNamesCanBeSet() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      List<String> incoming = Arrays.asList("in.One", "in.Two");
      List<String> outgoing = Arrays.asList("out.One");
      configuration.setIncomingInterceptorClassNames(incoming);
      configuration.setOutgoingInterceptorClassNames(outgoing);
      assertEquals(incoming, configuration.getIncomingInterceptorClassNames());
      assertEquals(outgoing, configuration.getOutgoingInterceptorClassNames());
   }

   @Test
   public void connectorConfigurationsCanBeAddedAndCleared() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      Map<String, org.apache.activemq.artemis.api.core.TransportConfiguration> connectors =
         new HashMap<>();
      configuration.setConnectorConfigurations(connectors);
      assertTrue(configuration.getConnectorConfigurations().isEmpty());
      configuration.clearConnectorConfigurations();
      assertTrue(configuration.getConnectorConfigurations().isEmpty());
   }

   @Test
   public void acceptorConfigurationsCanBeSetAndCleared() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      Set<org.apache.activemq.artemis.api.core.TransportConfiguration> acceptors = new HashSet<>();
      configuration.setAcceptorConfigurations(acceptors);
      assertTrue(configuration.getAcceptorConfigurations().isEmpty());
      configuration.clearAcceptorConfigurations();
      assertTrue(configuration.getAcceptorConfigurations().isEmpty());
   }

   @Test
   public void queueConfigurationsCanBeSetAndRetrieved() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      List<org.apache.activemq.artemis.api.core.QueueConfiguration> queues = new ArrayList<>();
      configuration.setQueueConfigs(queues);
      assertEquals(queues, configuration.getQueueConfigs());
   }

   @Test
   public void addressSettingsCanBeAddedAndCleared() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      assertTrue(configuration.getAddressSettings().isEmpty());
      configuration.clearAddressSettings();
      assertTrue(configuration.getAddressSettings().isEmpty());
   }

   @Test
   public void securityRolesCanBeSetAndRetrieved() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      Map<String, Set<org.apache.activemq.artemis.core.security.Role>> roles = new HashMap<>();
      configuration.setSecurityRoles(roles);
      assertEquals(roles, configuration.getSecurityRoles());
   }

   @Test
   public void managementAddressesCanBeSetAndRetrieved() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      SimpleString management = SimpleString.of("management");
      SimpleString notification = SimpleString.of("notifications");
      configuration.setManagementAddress(management);
      configuration.setManagementNotificationAddress(notification);
      assertEquals(management, configuration.getManagementAddress());
      assertEquals(notification, configuration.getManagementNotificationAddress());
   }

   @Test
   public void journalFileSizeCanBeSet() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      configuration.setJournalFileSize(4096);
      assertEquals(4096, configuration.getJournalFileSize());
   }

   @Test
   public void journalPoolAndMinimumFilesCanBeSet() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      configuration.setJournalPoolFiles(7);
      configuration.setJournalMinFiles(2);
      assertEquals(7, configuration.getJournalPoolFiles());
      assertEquals(2, configuration.getJournalMinFiles());
   }

   @Test
   public void journalSyncOptionsCanBeSet() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      configuration.setJournalSyncTransactional(false);
      configuration.setJournalSyncNonTransactional(false);
      assertFalse(configuration.isJournalSyncTransactional());
      assertFalse(configuration.isJournalSyncNonTransactional());
   }

   @Test
   public void securityAndGracefulShutdownOptionsCanBeSet() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      configuration.setSecurityEnabled(false);
      configuration.setGracefulShutdownEnabled(true);
      configuration.setGracefulShutdownTimeout(500L);
      assertFalse(configuration.isSecurityEnabled());
      assertTrue(configuration.isGracefulShutdownEnabled());
      assertEquals(500L, configuration.getGracefulShutdownTimeout());
   }

   @Test
   public void jmxOptionsCanBeSet() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      configuration.setJMXManagementEnabled(false);
      configuration.setJMXDomain("example.domain");
      configuration.setJMXUseBrokerName(true);
      assertFalse(configuration.isJMXManagementEnabled());
      assertEquals("example.domain", configuration.getJMXDomain());
      assertTrue(configuration.isJMXUseBrokerName());
   }

   @Test
   public void passwordMaskingOptionsCanBeSet() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      configuration.setMaskPassword(Boolean.FALSE);
      configuration.setPasswordCodec("codec");
      assertEquals(Boolean.FALSE, configuration.isMaskPassword());
      assertEquals("codec", configuration.getPasswordCodec());
   }

   @Test
   public void brokerInstanceCanBeSetAndRetrieved() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      java.io.File instance = new java.io.File("broker-instance");
      configuration.setBrokerInstance(instance);
      assertEquals(instance, configuration.getBrokerInstance());
   }

   @Test
   public void defaultConfigurationHasNameAndJournalType() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      assertEquals("localhost", configuration.getName());
      assertEquals(ConfigurationImpl.DEFAULT_JOURNAL_TYPE, configuration.getJournalType());
   }

   @Test
   public void nullableStoreConfigurationCanBeRetrieved() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      assertNull(configuration.getStoreConfiguration());
   }

   @Test
   public void defaultJsonStatusIsNotNullInStringRepresentation() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      assertNotNull(configuration.toString());
   }
}
