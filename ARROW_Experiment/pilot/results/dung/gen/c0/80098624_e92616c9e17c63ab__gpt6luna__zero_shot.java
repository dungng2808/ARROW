package rocks.coffeenet.autoconfigure.discovery.service;

import com.netflix.appinfo.InstanceInfo;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient.EurekaServiceInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IntegrationCoffeeNetAppService_ArrowTest {

    @Test
    public void getAppsReturnsEmptyMapWhenThereAreNoServices() {
        DiscoveryClient discoveryClient = new StubDiscoveryClient(Collections.<String>emptyList());
        IntegrationCoffeeNetAppService service = new IntegrationCoffeeNetAppService(discoveryClient);

        assertTrue(service.getApps().isEmpty());
    }

    @Test
    public void getAppsReturnsOnlyEurekaInstancesAndTrimsAllowedAuthorities() {
        InstanceInfo instanceInfo = InstanceInfo.Builder.newBuilder()
            .setVIPAddress("coffee-app")
            .setHomePageUrl("http://localhost/")
            .add("allowedAuthorities", " admin, user ")
            .build();
        EurekaServiceInstance eurekaInstance = new EurekaServiceInstance(instanceInfo);
        ServiceInstance nonEurekaInstance = new StubServiceInstance();

        DiscoveryClient discoveryClient = new StubDiscoveryClient(
            Collections.singletonList("coffee-app"),
            Arrays.<ServiceInstance>asList(eurekaInstance, nonEurekaInstance));
        IntegrationCoffeeNetAppService service = new IntegrationCoffeeNetAppService(discoveryClient);

        Map<String, List<CoffeeNetApp>> apps = service.getApps();

        assertEquals(1, apps.size());
        assertEquals(1, apps.get("coffee-app").size());
        assertEquals("coffee-app", apps.get("coffee-app").get(0).getName());
        assertEquals("http://localhost/", apps.get("coffee-app").get(0).getHomePageUrl());
        assertTrue(apps.get("coffee-app").get(0).isAllowedToAccessBy(Collections.singleton("admin")));
    }

    @Test
    public void getAppsFiltersServicesByRequestedName() {
        InstanceInfo instanceInfo = InstanceInfo.Builder.newBuilder()
            .setVIPAddress("coffee-app")
            .setHomePageUrl("http://localhost/")
            .add("allowedAuthorities", "")
            .build();
        EurekaServiceInstance eurekaInstance = new EurekaServiceInstance(instanceInfo);

        DiscoveryClient discoveryClient = new StubDiscoveryClient(
            Arrays.asList("coffee-app", "other-app"),
            Collections.<ServiceInstance>singletonList(eurekaInstance));
        IntegrationCoffeeNetAppService service = new IntegrationCoffeeNetAppService(discoveryClient);
        AppQuery query = AppQuery.builder().appNames(Collections.singleton("other-app")).build();

        assertTrue(service.getApps(query).isEmpty());
    }

    @Test
    public void getAppsFiltersInstancesByRequestedRole() {
        InstanceInfo instanceInfo = InstanceInfo.Builder.newBuilder()
            .setVIPAddress("coffee-app")
            .setHomePageUrl("http://localhost/")
            .add("allowedAuthorities", "admin")
            .build();
        EurekaServiceInstance eurekaInstance = new EurekaServiceInstance(instanceInfo);

        DiscoveryClient discoveryClient = new StubDiscoveryClient(
            Collections.singletonList("coffee-app"),
            Collections.<ServiceInstance>singletonList(eurekaInstance));
        IntegrationCoffeeNetAppService service = new IntegrationCoffeeNetAppService(discoveryClient);
        AppQuery query = AppQuery.builder().appRoles(Collections.singleton("guest")).build();

        assertTrue(service.getApps(query).isEmpty());
    }

    private static class StubDiscoveryClient implements DiscoveryClient {
        private final List<String> services;
        private final List<ServiceInstance> instances;

        StubDiscoveryClient(List<String> services) {
            this(services, Collections.<ServiceInstance>emptyList());
        }

        StubDiscoveryClient(List<String> services, List<ServiceInstance> instances) {
            this.services = services;
            this.instances = instances;
        }

        @Override
        public String description() {
            return "stub";
        }

        @Override
        public List<ServiceInstance> getInstances(String serviceId) {
            return instances;
        }

        @Override
        public List<String> getServices() {
            return services;
        }
    }

    private static class StubServiceInstance implements ServiceInstance {
        @Override
        public String getServiceId() {
            return "coffee-app";
        }

        @Override
        public String getHost() {
            return "localhost";
        }

        @Override
        public int getPort() {
            return 8080;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public java.net.URI getUri() {
            return java.net.URI.create("http://localhost:8080");
        }

        @Override
        public Map<String, String> getMetadata() {
            return Collections.emptyMap();
        }
    }
}
