package rocks.coffeenet.autoconfigure.discovery.service;

import com.netflix.appinfo.InstanceInfo;
import org.junit.Test;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient.EurekaServiceInstance;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IntegrationCoffeeNetAppService_ArrowTest {

    @Test
    public void getAppsReturnsEmptyMapWhenNoServicesAreRegistered() {
        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
        when(discoveryClient.getServices()).thenReturn(Collections.<String>emptyList());

        IntegrationCoffeeNetAppService service = new IntegrationCoffeeNetAppService(discoveryClient);

        assertEquals(Collections.emptyMap(), service.getApps());
    }

    @Test
    public void getAppsReturnsEurekaInstancesGroupedByApplicationName() {
        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
        when(discoveryClient.getServices()).thenReturn(Arrays.asList("COFFEENET"));

        EurekaServiceInstance instance = eurekaInstance("COFFEENET", "http://localhost/");
        when(discoveryClient.getInstances("COFFEENET")).thenReturn(
                Collections.<org.springframework.cloud.client.ServiceInstance>singletonList(instance));

        IntegrationCoffeeNetAppService service = new IntegrationCoffeeNetAppService(discoveryClient);
        Map<String, List<CoffeeNetApp>> apps = service.getApps();

        assertEquals(1, apps.size());
        assertEquals(1, apps.get("COFFEENET").size());
        assertEquals("COFFEENET", apps.get("COFFEENET").get(0).getName());
    }

    @Test
    public void getAppsFiltersByRequestedApplicationName() {
        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
        when(discoveryClient.getServices()).thenReturn(Arrays.asList("FIRST", "SECOND"));

        EurekaServiceInstance first = eurekaInstance("FIRST", "http://first/");
        when(discoveryClient.getInstances("FIRST")).thenReturn(
                Collections.<org.springframework.cloud.client.ServiceInstance>singletonList(first));
        EurekaServiceInstance second = eurekaInstance("SECOND", "http://second/");
        when(discoveryClient.getInstances("SECOND")).thenReturn(
                Collections.<org.springframework.cloud.client.ServiceInstance>singletonList(second));

        AppQuery query = AppQuery.builder().build();
        IntegrationCoffeeNetAppService service = new IntegrationCoffeeNetAppService(discoveryClient);

        Map<String, List<CoffeeNetApp>> apps = service.getApps(query);

        assertEquals(2, apps.size());
        assertEquals(true, apps.containsKey("FIRST"));
        assertEquals(true, apps.containsKey("SECOND"));
    }

    @Test
    public void getAppsOmitsInstancesThatAreNotEurekaInstances() {
        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
        when(discoveryClient.getServices()).thenReturn(Collections.singletonList("COFFEENET"));
        when(discoveryClient.getInstances("COFFEENET")).thenReturn(
                Collections.singletonList((org.springframework.cloud.client.ServiceInstance) mock(
                        org.springframework.cloud.client.ServiceInstance.class)));

        IntegrationCoffeeNetAppService service = new IntegrationCoffeeNetAppService(discoveryClient);

        assertEquals(Collections.emptyMap(), service.getApps());
    }

    private EurekaServiceInstance eurekaInstance(String name, String homePageUrl) {
        InstanceInfo instanceInfo = mock(InstanceInfo.class);
        when(instanceInfo.getMetadata()).thenReturn(Collections.<String, String>emptyMap());
        when(instanceInfo.getVIPAddress()).thenReturn(name);
        when(instanceInfo.getHomePageUrl()).thenReturn(homePageUrl);

        EurekaServiceInstance instance = mock(EurekaServiceInstance.class);
        when(instance.getInstanceInfo()).thenReturn(instanceInfo);
        return instance;
    }
}
