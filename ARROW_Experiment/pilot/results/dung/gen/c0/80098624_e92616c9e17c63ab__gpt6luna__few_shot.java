package rocks.coffeenet.autoconfigure.discovery.service;

import com.netflix.appinfo.InstanceInfo;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient.EurekaServiceInstance;

import static org.junit.Assert.assertEquals;
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
        };

        AppQuery query = AppQuery.builder().appNames(Collections.singleton("first")).build();
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
        };

        IntegrationCoffeeNetAppService service = new IntegrationCoffeeNetAppService(discoveryClient);
        assertTrue(service.getApps().isEmpty());
    }
}
