package org.cognitor.cassandra.migration.keyspace;

import org.junit.Test;

import static org.junit.Assert.*;

public class Keyspace_ArrowTest {

    @Test
    public void should_create_keyspace_with_default_values() {
        Keyspace keyspace = new Keyspace("test_keyspace");
        assertEquals("test_keyspace", keyspace.getKeyspaceName());
        assertTrue(keyspace.isDurableWrites());
        assertTrue(keyspace.getReplicationStrategy() instanceof SimpleStrategy);
    }

    @Test
    public void should_set_durable_writes_to_false() {
        Keyspace keyspace = new Keyspace("test_keyspace").withoutDurableWrites();
        assertFalse(keyspace.isDurableWrites());
    }

    @Test
    public void should_set_custom_replication_strategy() {
        ReplicationStrategy strategy = new SimpleStrategy();
        Keyspace keyspace = new Keyspace("test_keyspace").with(strategy);
        assertEquals(strategy, keyspace.getReplicationStrategy());
    }

    @Test
    public void should_generate_cql_statement_with_default_values() {
        Keyspace keyspace = new Keyspace("test_keyspace");
        String cql = keyspace.getCqlStatement();
        String expected = "CREATE KEYSPACE IF NOT EXISTS test_keyspace WITH REPLICATION = " +
                "(class='SimpleStrategy', replicas=1) AND DURABLE_WRITES = true";
        assertEquals(expected, cql);
    }

    @Test
    public void should_generate_cql_statement_with_disabled_durable_writes() {
        Keyspace keyspace = new Keyspace("test_keyspace").withoutDurableWrites();
        String cql = keyspace.getCqlStatement();
        String expected = "CREATE KEYSPACE IF NOT EXISTS test_keyspace WITH REPLICATION = " +
                "(class='SimpleStrategy', replicas=1) AND DURABLE_WRITES = false";
        assertEquals(expected, cql);
    }

    @Test
    public void should_generate_cql_statement_with_custom_replication_strategy() {
        ReplicationStrategy strategy = new NetworkTopologyStrategy();
        Keyspace keyspace = new Keyspace("test_keyspace").with(strategy);
        String cql = keyspace.getCqlStatement();
        String expected = "CREATE KEYSPACE IF NOT EXISTS test_keyspace WITH REPLICATION = " +
                "(class='NetworkTopologyStrategy') AND DURABLE_WRITES = true";
        assertEquals(expected, cql);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_for_null_keyspace_name() {
        new Keyspace(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_for_empty_keyspace_name() {
        new Keyspace("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_for_null_replication_strategy() {
        new Keyspace("test_keyspace").with(null);
    }
}
