package rocks.coffeenet.autoconfigure.discovery.service;

import com.netflix.appinfo.InstanceInfo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient.EurekaServiceInstance;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IntegrationCoffeeNetAppService_ArrowTest {

    private DiscoveryClient discoveryClient;
    private IntegrationCoffeeNetAppService service;

    @Before
    public void setUp() {
        discoveryClient = mock(DiscoveryClient.class);
        service = new IntegrationCoffeeNetAppService(discoveryClient);
    }

    @Test
    public void getAppsReturnsEmptyMapWhenNoServicesAvailable() {
        when(discoveryClient.getServices()).thenReturn(emptyList());

        Map<String, List<CoffeeNetApp>> result = service.getApps();

        assertThat(result, is(empty()));
    }

    @Test
    public void getAppsReturnsAppsFromDiscoveryClient() {
        ServiceInstance instance1 = createMockEurekaServiceInstance("app1", "http://app1.example.com", "role1");
        ServiceInstance instance2 = createMockEurekaServiceInstance("app1", "http://app2.example.com", "role2");

        when(discoveryClient.getServices()).thenReturn(singletonList("app1"));
        when(discoveryClient.getInstances("app1")).thenReturn(asList(instance1, instance2));

        Map<String, List<CoffeeNetApp>> result = service.getApps();

        assertThat(result.size(), is(1));
        assertThat(result.containsKey("app1"), is(true));
        assertThat(result.get("app1").size(), is(2));
        assertThat(result.get("app1").get(0).getName(), is("app1"));
        assertThat(result.get("app1").get(0).getHomePageUrl(), is("http://app1.example.com"));
        assertThat(result.get("app1").get(0).getAllowedAuthorities(), hasItem("role1"));
        assertThat(result.get("app1").get(1).getName(), is("app1"));
        assertThat(result.get("app1").get(1).getHomePageUrl(), is("http://app2.example.com"));
        assertThat(result.get("app1").get(1).getAllowedAuthorities(), hasItem("role2"));
    }

    @Test
    public void getAppsFiltersByAppName() {
        ServiceInstance instance1 = createMockEurekaServiceInstance("app1", "http://app1.example.com", "role1");
        ServiceInstance instance2 = createMockEurekaServiceInstance("app2", "http://app2.example.com", "role2");

        when(discoveryClient.getServices()).thenReturn(asList("app1", "app2"));
        when(discoveryClient.getInstances("app1")).thenReturn(singletonList(instance1));
        when(discoveryClient.getInstances("app2")).thenReturn(singletonList(instance2));

        AppQuery query = AppQuery.builder().appNames(asList("app1")).build();
        Map<String, List<CoffeeNetApp>> result = service.getApps(query);

        assertThat(result.size(), is(1));
        assertThat(result.containsKey("app1"), is(true));
        assertThat(result.get("app1").size(), is(1));
        assertThat(result.get("app1").get(0).getName(), is("app1"));
        assertThat(result.get("app1").get(0).getHomePageUrl(), is("http://app1.example.com"));
        assertThat(result.get("app1").get(0).getAllowedAuthorities(), hasItem("role1"));
    }

    @Test
    public void getAppsFiltersByAppRole() {
        ServiceInstance instance1 = createMockEurekaServiceInstance("app1", "http://app1.example.com", "role1, role2");
        ServiceInstance instance2 = createMockEurekaServiceInstance("app1", "http://app2.example.com", "role3");

        when(discoveryClient.getServices()).thenReturn(singletonList("app1"));
        when(discoveryClient.getInstances("app1")).thenReturn(asList(instance1, instance2));

        AppQuery query = AppQuery.builder().appRoles(asList("role1")).build();
        Map<String, List<CoffeeNetApp>> result = service.getApps(query);

        assertThat(result.size(), is(1));
        assertThat(result.get("app1").size(), is(1));
        assertThat(result.get("app1").get(0).getHomePageUrl(), is("http://app1.example.com"));
        assertThat(result.get("app1").get(0).getAllowedAuthorities(), hasItem("role1"));
        assertThat(result.get("app1").get(0).getAllowedAuthorities(), not(hasItem("role3")));
    }

    @Test
    public void getAppsIgnoresNonEurekaInstances() {
        ServiceInstance regularInstance = mock(ServiceInstance.class);
        ServiceInstance eurekaInstance = createMockEurekaServiceInstance("app1", "http://app1.example.com", "role1");

        when(discoveryClient.getServices()).thenReturn(singletonList("app1"));
        when(discoveryClient.getInstances("app1")).thenReturn(asList(regularInstance, eurekaInstance));

        Map<String, List<CoffeeNetApp>> result = service.getApps();

        assertThat(result.size(), is(1));
        assertThat(result.get("app1").size(), is(1));
        assertThat(result.get("app1").get(0).getName(), is("app1"));
        assertThat(result.get("app1").get(0).getHomePageUrl(), is("http://app1.example.com"));
        assertThat(result.get("app1").get(0).getAllowedAuthorities(), hasItem("role1"));
    }

    @Test
    public void getAppsReturnsEmptyListForServiceWithNoInstances() {
        when(discoveryClient.getServices()).thenReturn(singletonList("app1"));
        when(discoveryClient.getInstances("app1")).thenReturn(emptyList());

        Map<String, List<CoffeeNetApp>> result = service.getApps();

        assertThat(result.size(), is(1));
        assertThat(result.get("app1"), is(empty()));
    }

    @Test
    public void getAppsReturnsCaseInsensitiveKeys() {
        ServiceInstance instance = createMockEurekaServiceInstance("App1", "http://app1.example.com", "role1");

        when(discoveryClient.getServices()).thenReturn(singletonList("App1"));
        when(discoveryClient.getInstances("App1")).thenReturn(singletonList(instance));

        Map<String, List<CoffeeNetApp>> result = service.getApps();

        assertThat(result.containsKey("app1"), is(true));
        assertThat(result.containsKey("APP1"), is(true));
        assertThat(result.containsKey("App1"), is(true));
        assertThat(result.get("app1").get(0).getHomePageUrl(), is("http://app1.example.com"));
        assertThat(result.get("app1").get(0).getAllowedAuthorities(), hasItem("role1"));
    }

    private EurekaServiceInstance createMockEurekaServiceInstance(String appName, String homePageUrl, String roles) {
        InstanceInfo instanceInfo = mock(InstanceInfo.class);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("allowedAuthorities", roles);
        when(instanceInfo.getMetadata()).thenReturn(metadata);
        when(instanceInfo.getVIPAddress()).thenReturn(appName);
        when(instanceInfo.getHomePageUrl()).thenReturn(homePageUrl);

        EurekaServiceInstance eurekaServiceInstance = mock(EurekaServiceInstance.class);
        when(eurekaServiceInstance.getInstanceInfo()).thenReturn(instanceInfo);

        return eurekaServiceInstance;
    }
}
