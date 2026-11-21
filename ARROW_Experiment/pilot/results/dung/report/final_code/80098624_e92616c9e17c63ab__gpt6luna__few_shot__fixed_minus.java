package rocks.coffeenet.autoconfigure.discovery.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import static org.junit.Assert.assertTrue;

public class IntegrationCoffeeNetAppService_ArrowTest {

    @Test
    public void getAppsReturnsEmptyMapWhenThereAreNoServices() {
        DiscoveryClient discoveryClient = new DiscoveryClient() {
            @Override
            public List<String> getServices() {
                return Collections.emptyList();
            }

            @Override
            public List<ServiceInstance> getInstances(String serviceId) {
                return Collections.emptyList();
            }

            @Override
            public String description() {
                return "";
            }

            @Override
            public ServiceInstance getLocalServiceInstance() {
                return null;
            }
        };

        IntegrationCoffeeNetAppService service = new IntegrationCoffeeNetAppService(discoveryClient);
        assertTrue(service.getApps().isEmpty());
    }

    @Test
    public void getAppsFiltersServicesByQueryNames() {
        DiscoveryClient discoveryClient = new DiscoveryClient() {
            @Override
            public List<String> getServices() {
                return Arrays.asList("first", "second");
            }

            @Override
            public List<ServiceInstance> getInstances(String serviceId) {
                return Collections.emptyList();
            }

            @Override
            public String description() {
                return "";
            }

            @Override
            public ServiceInstance getLocalServiceInstance() {
                return null;
            }
        };

        AppQuery query = AppQuery.builder().build();
        IntegrationCoffeeNetAppService service = new IntegrationCoffeeNetAppService(discoveryClient);

        assertTrue(service.getApps(query).isEmpty());
    }

    @Test
    public void getAppsIgnoresNonEurekaInstances() {
        DiscoveryClient discoveryClient = new DiscoveryClient() {
            @Override
            public List<String> getServices() {
                return Collections.singletonList("service");
            }

            @Override
            public List<ServiceInstance> getInstances(String serviceId) {
                return Collections.<ServiceInstance>singletonList(null);
            }

            @Override
            public String description() {
                return "";
            }

            @Override
            public ServiceInstance getLocalServiceInstance() {
                return null;
            }
        };

        IntegrationCoffeeNetAppService service = new IntegrationCoffeeNetAppService(discoveryClient);
        assertTrue(service.getApps().isEmpty());
    }
}
