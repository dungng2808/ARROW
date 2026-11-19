package org.apache.activemq.artemis.core.config.impl;

import org.apache.activemq.artemis.core.server.JournalType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ConfigurationImpl_ArrowTest {

    @Test
    public void defaultNameIsLocalhost() {
        ConfigurationImpl configuration = new ConfigurationImpl();

        assertEquals("localhost", configuration.getName());
    }

    @Test
    public void nameCanBeChanged() {
        ConfigurationImpl configuration = new ConfigurationImpl();

        assertSame(configuration, configuration.setName("broker-one"));
        assertEquals("broker-one", configuration.getName());
    }

    @Test
    public void journalTypeCanBeChanged() {
        ConfigurationImpl configuration = new ConfigurationImpl();

        assertSame(configuration, configuration.setJournalType(JournalType.NIO));
        assertEquals(JournalType.NIO, configuration.getJournalType());
    }

    @Test
    public void journalRetentionDirectoryCanBeSetAndResolved() {
        ConfigurationImpl configuration = new ConfigurationImpl();

        assertSame(configuration, configuration.setJournalRetentionDirectory("retention"));
        assertEquals("retention", configuration.getJournalRetentionDirectory());
        assertEquals(new java.io.File("retention"), configuration.getJournalRetentionLocation());
    }

    @Test
    public void journalRetentionMaxBytesCanBeSet() {
        ConfigurationImpl configuration = new ConfigurationImpl();

        assertSame(configuration, configuration.setJournalRetentionMaxBytes(4096L));
        assertEquals(4096L, configuration.getJournalRetentionMaxBytes());
    }

    @Test
    public void journalRetentionPeriodCanBeSetInSeconds() {
        ConfigurationImpl configuration = new ConfigurationImpl();

        configuration.setJournalRetentionPeriod(java.util.concurrent.TimeUnit.SECONDS, 12L);

        assertEquals(12000L, configuration.getJournalRetentionPeriod());
    }

    @Test
    public void globalLimitsCanBeSet() {
        ConfigurationImpl configuration = new ConfigurationImpl();

        assertSame(configuration, configuration.setGlobalMaxSize(1000L));
        assertEquals(1000L, configuration.getGlobalMaxSize());
        assertSame(configuration, configuration.setGlobalMaxMessages(25L));
        assertEquals(25L, configuration.getGlobalMaxMessages());
    }

    @Test
    public void persistenceCanBeDisabled() {
        ConfigurationImpl configuration = new ConfigurationImpl();

        assertSame(configuration, configuration.setPersistenceEnabled(false));
        assertFalse(configuration.isPersistenceEnabled());
    }

    @Test
    public void journalDatasyncCanBeChanged() {
        ConfigurationImpl configuration = new ConfigurationImpl();

        assertSame(configuration, configuration.setJournalDatasync(false));
        assertFalse(configuration.isJournalDatasync());
    }

    @Test
    public void diskUsageCanBeChanged() {
        ConfigurationImpl configuration = new ConfigurationImpl();

        assertSame(configuration, configuration.setMaxDiskUsage(75));
        assertEquals(75, configuration.getMaxDiskUsage());
    }

    @Test
    public void systemPropertyPrefixCanBeChanged() {
        ConfigurationImpl configuration = new ConfigurationImpl();

        configuration.setSystemPropertyPrefix("broker.");

        assertEquals("broker.", configuration.getSystemPropertyPrefix());
    }

    @Test
    public void brokerPropertiesKeySurroundCanBeChanged() {
        ConfigurationImpl configuration = new ConfigurationImpl();

        configuration.setBrokerPropertiesKeySurround("##");

        assertEquals("##", configuration.getBrokerPropertiesKeySurround());
    }

    @Test
    public void brokerInstanceCanBeSet() {
        ConfigurationImpl configuration = new ConfigurationImpl();
        java.io.File instance = new java.io.File("broker-instance");

        configuration.setBrokerInstance(instance);

        assertEquals(instance, configuration.getBrokerInstance());
    }

    @Test
    public void defaultConnectorConfigurationsAreEmpty() {
        ConfigurationImpl configuration = new ConfigurationImpl();

        assertNotNull(configuration.getConnectorConfigurations());
        assertTrue(configuration.getConnectorConfigurations().isEmpty());
    }

    @Test
    public void defaultAcceptorConfigurationsAreEmpty() {
        ConfigurationImpl configuration = new ConfigurationImpl();

        assertNotNull(configuration.getAcceptorConfigurations());
        assertTrue(configuration.getAcceptorConfigurations().isEmpty());
    }

    @Test
    public void defaultJournalRetentionDirectoryIsUnset() {
        ConfigurationImpl configuration = new ConfigurationImpl();

        assertNull(configuration.getJournalRetentionDirectory());
    }
}
