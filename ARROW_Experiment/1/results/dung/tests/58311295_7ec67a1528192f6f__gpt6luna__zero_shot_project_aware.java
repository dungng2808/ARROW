package org.cognitor.cassandra.migration.keyspace;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class Keyspace_ArrowTest {

    @Test
    public void constructorSetsNameAndDefaults() {
        Keyspace keyspace = new Keyspace("test_keyspace");

        assertEquals("test_keyspace", keyspace.getKeyspaceName());
        assertTrue(keyspace.isDurableWrites());
        assertNotNull(keyspace.getReplicationStrategy());
        assertEquals(
                "CREATE KEYSPACE IF NOT EXISTS test_keyspace WITH REPLICATION = "
                        + keyspace.getReplicationStrategy().createCqlStatement()
                        + " AND DURABLE_WRITES = true",
                keyspace.getCqlStatement());
    }

    @Test
    public void withoutDurableWritesDisablesDurableWritesAndReturnsSameInstance() {
        Keyspace keyspace = new Keyspace("test_keyspace");

        Keyspace result = keyspace.withoutDurableWrites();

        assertSame(keyspace, result);
        assertFalse(keyspace.isDurableWrites());
        assertEquals(
                "CREATE KEYSPACE IF NOT EXISTS test_keyspace WITH REPLICATION = "
                        + keyspace.getReplicationStrategy().createCqlStatement()
                        + " AND DURABLE_WRITES = false",
                keyspace.getCqlStatement());
    }

    @Test
    public void withSetsReplicationStrategyAndReturnsSameInstance() {
        Keyspace keyspace = new Keyspace("test_keyspace");
        ReplicationStrategy strategy = new SimpleStrategy();

        Keyspace result = keyspace.with(strategy);

        assertSame(keyspace, result);
        assertSame(strategy, keyspace.getReplicationStrategy());
        assertEquals(
                "CREATE KEYSPACE IF NOT EXISTS test_keyspace WITH REPLICATION = "
                        + strategy.createCqlStatement()
                        + " AND DURABLE_WRITES = true",
                keyspace.getCqlStatement());
    }

    @Test(expected = Exception.class)
    public void constructorRejectsNullName() {
        new Keyspace(null);
    }

    @Test(expected = Exception.class)
    public void constructorRejectsEmptyName() {
        new Keyspace("");
    }

    @Test(expected = Exception.class)
    public void withRejectsNullReplicationStrategy() {
        new Keyspace("test_keyspace").with(null);
    }
}
