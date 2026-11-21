package org.cognitor.cassandra.migration.keyspace;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class Keyspace_ArrowTest {

    @Test
    public void shouldCreateKeyspaceWithSimpleStrategyAndDurableWrites() {
        Keyspace keyspace = new Keyspace("test_keyspace");
        String cqlStatement = keyspace.getCqlStatement();
        String expected = "CREATE KEYSPACE IF NOT EXISTS test_keyspace WITH REPLICATION = "
                + "(class='SimpleStrategy', replicas=1) AND DURABLE_WRITES = true";
        assertThat(cqlStatement, is(expected));
    }

    @Test
    public void shouldCreateKeyspaceWithCustomReplicationStrategy() {
        Keyspace keyspace = new Keyspace("test_keyspace");
        ReplicationStrategy strategy = mock(ReplicationStrategy.class);
        when(strategy.createCqlStatement()).thenReturn("(class='NetworkTopologyStrategy', dc1=1)");
        keyspace.with(strategy);
        String cqlStatement = keyspace.getCqlStatement();
        String expected = "CREATE KEYSPACE IF NOT EXISTS test_keyspace WITH REPLICATION = "
                + "(class='NetworkTopologyStrategy', dc1=1) AND DURABLE_WRITES = true";
        assertThat(cqlStatement, is(expected));
    }

    @Test
    public void shouldCreateKeyspaceWithoutDurableWrites() {
        Keyspace keyspace = new Keyspace("test_keyspace");
        keyspace.withoutDurableWrites();
        String cqlStatement = keyspace.getCqlStatement();
        String expected = "CREATE KEYSPACE IF NOT EXISTS test_keyspace WITH REPLICATION = "
                + "(class='SimpleStrategy', replicas=1) AND DURABLE_WRITES = false";
        assertThat(cqlStatement, is(expected));
    }

    @Test
    public void shouldReturnKeyspaceName() {
        Keyspace keyspace = new Keyspace("test_keyspace");
        assertThat(keyspace.getKeyspaceName(), is("test_keyspace"));
    }

    @Test
    public void shouldReturnDurableWritesValue() {
        Keyspace keyspace = new Keyspace("test_keyspace");
        assertThat(keyspace.isDurableWrites(), is(true));
        
        keyspace.withoutDurableWrites();
        assertThat(keyspace.isDurableWrites(), is(false));
    }

    @Test
    public void shouldReturnReplicationStrategy() {
        Keyspace keyspace = new Keyspace("test_keyspace");
        assertThat(keyspace.getReplicationStrategy() instanceof SimpleStrategy, is(true));
    }

    @Test
    public void shouldSetReplicationStrategy() {
        Keyspace keyspace = new Keyspace("test_keyspace");
        ReplicationStrategy strategy = mock(ReplicationStrategy.class);
        keyspace.with(strategy);
        assertThat(keyspace.getReplicationStrategy(), is(strategy));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNullKeyspaceName() {
        new Keyspace(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForEmptyKeyspaceName() {
        new Keyspace("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNullReplicationStrategy() {
        Keyspace keyspace = new Keyspace("test_keyspace");
        keyspace.with(null);
    }
}
