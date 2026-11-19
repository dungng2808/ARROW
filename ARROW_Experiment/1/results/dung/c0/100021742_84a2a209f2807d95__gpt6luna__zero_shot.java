package me.snowdrop.stream.binder.artemis.provisioning;

import me.snowdrop.stream.binder.artemis.properties.ArtemisCommonProperties;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.junit.Test;
import org.springframework.cloud.stream.provisioning.ProvisioningException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArtemisBrokerManager_ArrowTest {

    @Test
    public void createAddressWrapsSessionFactoryFailure() throws Exception {
        ServerLocator locator = mock(ServerLocator.class);
        when(locator.createSessionFactory()).thenThrow(new RuntimeException("failure"));
        ArtemisBrokerManager manager = new ArtemisBrokerManager(locator, null, null);

        try {
            manager.createAddress("orders", new ArtemisCommonProperties());
            fail("Expected provisioning exception");
        } catch (ProvisioningException exception) {
            assertEquals("Failed to create address 'orders'", exception.getMessage());
        }
    }

    @Test
    public void createQueueWrapsSessionFactoryFailure() throws Exception {
        ServerLocator locator = mock(ServerLocator.class);
        when(locator.createSessionFactory()).thenThrow(new RuntimeException("failure"));
        ArtemisBrokerManager manager = new ArtemisBrokerManager(locator, null, null);

        try {
            manager.createQueue("orders", "orders.queue");
            fail("Expected provisioning exception");
        } catch (ProvisioningException exception) {
            assertEquals("Failed to create queue 'orders.queue' with address 'orders'", exception.getMessage());
        }
    }

    @Test
    public void createQueuePropagatesProvisioningExceptionFromSessionFactory() throws Exception {
        ServerLocator locator = mock(ServerLocator.class);
        ProvisioningException expected = new ProvisioningException("session failure");
        when(locator.createSessionFactory()).thenThrow(expected);
        ArtemisBrokerManager manager = new ArtemisBrokerManager(locator, null, null);

        try {
            manager.createQueue("orders", "orders.queue");
            fail("Expected provisioning exception");
        } catch (ProvisioningException exception) {
            assertEquals(expected, exception);
        }
    }
}
