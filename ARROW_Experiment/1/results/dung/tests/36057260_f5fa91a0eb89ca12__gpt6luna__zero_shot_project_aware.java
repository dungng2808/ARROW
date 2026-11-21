package org.apache.activemq.artemis.core.config.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ConfigurationImpl_ArrowTest {

   @Test
   public void nameCanBeSetAndRetrieved() {
      ConfigurationImpl configuration = new ConfigurationImpl();

      assertSame(configuration, configuration.setName("broker-a"));
      assertEquals("broker-a", configuration.getName());
   }

   @Test
   public void defaultNameIsLocalhost() {
      ConfigurationImpl configuration = new ConfigurationImpl();

      assertEquals("localhost", configuration.getName());
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
   public void journalDirectoryCanBeSetAndRetrieved() {
      ConfigurationImpl configuration = new ConfigurationImpl();

      assertSame(configuration, configuration.setJournalDirectory("journal-data"));
      assertEquals("journal-data", configuration.getJournalDirectory());
   }

   @Test
   public void journalLocationUsesConfiguredDirectory() {
      ConfigurationImpl configuration = new ConfigurationImpl();
      configuration.setJournalDirectory("journal-data");

      assertEquals(new java.io.File("journal-data"), configuration.getJournalLocation());
   }

   @Test
   public void connectorConfigurationsCanBeAddedAndCleared() {
      ConfigurationImpl configuration = new ConfigurationImpl();

      assertNotNull(configuration.getConnectorConfigurations());
      configuration.clearConnectorConfigurations();
      assertTrue(configuration.getConnectorConfigurations().isEmpty());
   }

   @Test
   public void acceptorConfigurationsCanBeCleared() {
      ConfigurationImpl configuration = new ConfigurationImpl();

      configuration.clearAcceptorConfigurations();
      assertTrue(configuration.getAcceptorConfigurations().isEmpty());
   }

   @Test
   public void brokerPluginsStartEmpty() {
      ConfigurationImpl configuration = new ConfigurationImpl();

      assertTrue(configuration.getBrokerPlugins().isEmpty());
   }

   @Test
   public void globalMaxSizeCanBeSetAndRetrieved() {
      ConfigurationImpl configuration = new ConfigurationImpl();

      assertSame(configuration, configuration.setGlobalMaxSize(1234L));
      assertEquals(1234L, configuration.getGlobalMaxSize());
   }

   @Test
   public void globalMaxMessagesCanBeSetAndRetrieved() {
      ConfigurationImpl configuration = new ConfigurationImpl();

      assertSame(configuration, configuration.setGlobalMaxMessages(42L));
      assertEquals(42L, configuration.getGlobalMaxMessages());
   }
}
