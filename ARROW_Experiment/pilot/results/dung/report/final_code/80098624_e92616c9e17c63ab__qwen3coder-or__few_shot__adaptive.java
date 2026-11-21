package rocks.coffeenet.autoconfigure.discovery.service;

import com.netflix.appinfo.InstanceInfo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient.EurekaServiceInstance;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
        when(discoveryClient.getServices()).thenReturn(Collections.emptyList());

        Map<String, List<CoffeeNetApp>> result = service.getApps();

        assertThat(result.size(), is(0));
    }

    @Test
    public void getAppsReturnsAppsFilteredByQuery() {
        ServiceInstance instance1 = createMockEurekaServiceInstance("app1", "http://app1.example.com", "role1, role2");
        ServiceInstance instance2 = createMockEurekaServiceInstance("app2", "http://app2.example.com", "role2");

        when(discoveryClient.getServices()).thenReturn(asList("app1", "app2"));
        when(discoveryClient.getInstances("app1")).thenReturn(singletonList(instance1));
        when(discoveryClient.getInstances("app2")).thenReturn(singletonList(instance2));

        AppQuery query = AppQuery.builder().appNames(new HashSet<>(asList("app1"))).build();
        Map<String, List<CoffeeNetApp>> result = service.getApps(query);

        assertThat(result.size(), is(1));
        assertThat(result.containsKey("app1"), is(true));
        assertThat(result.get("app1").size(), is(1));
        assertThat(result.get("app1").get(0).getName(), is("app1"));
        assertThat(result.get("app1").get(0).getUrl(), is("http://app1.example.com"));
        assertThat(result.get("app1").get(0).getAuthorities(), is(new HashSet<>(asList("role1", "role2"))));
    }

    @Test
    public void getAppsFiltersOutAppsNotMatchingRoleQuery() {
        ServiceInstance instance1 = createMockEurekaServiceInstance("app1", "http://app1.example.com", "role1, role2");
        ServiceInstance instance2 = createMockEurekaServiceInstance("app2", "http://app2.example.com", "role3");

        when(discoveryClient.getServices()).thenReturn(asList("app1", "app2"));
        when(discoveryClient.getInstances("app1")).thenReturn(singletonList(instance1));
        when(discoveryClient.getInstances("app2")).thenReturn(singletonList(instance2));

        AppQuery query = AppQuery.builder().appRoles(new HashSet<>(singletonList("role1"))).build();
        Map<String, List<CoffeeNetApp>> result = service.getApps(query);

        assertThat(result.size(), is(1));
        assertThat(result.containsKey("app1"), is(true));
        assertThat(result.get("app1").size(), is(1));
        assertThat(result.get("app1").get(0).getName(), is("app1"));
        assertThat(result.get("app1").get(0).getUrl(), is("http://app1.example.com"));
        assertThat(result.get("app1").get(0).getAuthorities(), is(new HashSet<>(asList("role1", "role2"))));
    }

    @Test
    public void getAppsHandlesNonEurekaServiceInstances() {
        ServiceInstance nonEurekaInstance = mock(ServiceInstance.class);
        when(discoveryClient.getServices()).thenReturn(singletonList("app1"));
        when(discoveryClient.getInstances("app1")).thenReturn(singletonList(nonEurekaInstance));

        Map<String, List<CoffeeNetApp>> result = service.getApps();

        assertThat(result.size(), is(0));
    }

    @Test
    public void getAppsReturnsMultipleInstancesForSameAppName() {
        ServiceInstance instance1 = createMockEurekaServiceInstance("app1", "http://app1.example.com", "role1");
        ServiceInstance instance2 = createMockEurekaServiceInstance("app1", "http://app1-second.example.com", "role2");

        when(discoveryClient.getServices()).thenReturn(singletonList("app1"));
        when(discoveryClient.getInstances("app1")).thenReturn(asList(instance1, instance2));

        Map<String, List<CoffeeNetApp>> result = service.getApps();

        assertThat(result.size(), is(1));
        assertThat(result.containsKey("app1"), is(true));
        assertThat(result.get("app1").size(), is(2));
        assertThat(result.get("app1").get(0).getName(), is("app1"));
        assertThat(result.get("app1").get(1).getName(), is("app1"));
    }

    private EurekaServiceInstance createMockEurekaServiceInstance(String vipAddress, String homePageUrl, String authorities) {
        InstanceInfo instanceInfo = mock(InstanceInfo.class);
        when(instanceInfo.getVIPAddress()).thenReturn(vipAddress);
        when(instanceInfo.getHomePageUrl()).thenReturn(homePageUrl);
        when(instanceInfo.getMetadata()).thenReturn(singletonMap("allowedAuthorities", authorities));

        EurekaServiceInstance eurekaServiceInstance = mock(EurekaServiceInstance.class);
        when(eurekaServiceInstance.getInstanceInfo()).thenReturn(instanceInfo);
        return eurekaServiceInstance;
    }
}
