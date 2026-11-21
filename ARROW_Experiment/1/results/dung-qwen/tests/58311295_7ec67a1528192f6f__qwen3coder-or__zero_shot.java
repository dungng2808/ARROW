package org.cognitor.cassandra.migration.keyspace;

import org.junit.Test;
import static org.junit.Assert.*;

public class Keyspace_ArrowTest {

    @Test
    public void shouldCreateKeyspaceWithSimpleStrategyAndDurableWritesByDefault() {
        Keyspace keyspace = new Keyspace("test_keyspace");
        assertEquals("test_keyspace", keyspace.getKeyspaceName());
        assertTrue(keyspace.isDurableWrites());
        assertTrue(keyspace.getReplicationStrategy() instanceof SimpleStrategy);
    }

    @Test
    public void shouldCreateKeyspaceWithCustomReplicationStrategy() {
        Keyspace keyspace = new Keyspace("test_keyspace");
        ReplicationStrategy customStrategy = new ReplicationStrategy() {
            @Override
            public String createCqlStatement() {
                return "'class' : 'NetworkTopologyStrategy', 'dc1' : 3";
            }
        };
        keyspace.with(customStrategy);
        assertEquals(customStrategy, keyspace.getReplicationStrategy());
    }

    @Test
    public void shouldDisableDurableWrites() {
        Keyspace keyspace = new Keyspace("test_keyspace");
        keyspace.withoutDurableWrites();
        assertFalse(keyspace.isDurableWrites());
    }

    @Test
    public void shouldGenerateCorrectCqlStatementWithSimpleStrategy() {
        Keyspace keyspace = new Keyspace("test_keyspace");
        String cql = keyspace.getCqlStatement();
        assertEquals("CREATE KEYSPACE IF NOT EXISTS test_keyspace WITH REPLICATION = "
                + "('class' : 'SimpleStrategy', 'replication_factor' : 1) AND DURABLE_WRITES = true", cql);
    }

    @Test
    public void shouldGenerateCorrectCqlStatementWithDisabledDurableWrites() {
        Keyspace keyspace = new Keyspace("test_keyspace");
        keyspace.withoutDurableWrites();
        String cql = keyspace.getCqlStatement();
        assertEquals("CREATE KEYSPACE IF NOT EXISTS test_keyspace WITH REPLICATION = "
                + "('class' : 'SimpleStrategy', 'replication_factor' : 1) AND DURABLE_WRITES = false", cql);
    }

    @Test
    public void shouldGenerateCorrectCqlStatementWithCustomReplicationStrategy() {
        Keyspace keyspace = new Keyspace("test_keyspace");
        ReplicationStrategy customStrategy = new ReplicationStrategy() {
            @Override
            public String createCqlStatement() {
                return "'class' : 'NetworkTopologyStrategy', 'dc1' : 3";
            }
        };
        keyspace.with(customStrategy);
        keyspace.withoutDurableWrites();
        String cql = keyspace.getCqlStatement();
        assertEquals("CREATE KEYSPACE IF NOT EXISTS test_keyspace WITH REPLICATION = "
                + "'class' : 'NetworkTopologyStrategy', 'dc1' : 3 AND DURABLE_WRITES = false", cql);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenCreatingKeyspaceWithNullName() {
        new Keyspace(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenCreatingKeyspaceWithEmptyName() {
        new Keyspace("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSettingNullReplicationStrategy() {
        Keyspace keyspace = new Keyspace("test_keyspace");
        keyspace.with(null);
    }
}
