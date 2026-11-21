package rocks.coffeenet.autoconfigure.discovery.service;

import com.netflix.appinfo.InstanceInfo;
import org.junit.Test;
import org.junit.Before;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient.EurekaServiceInstance;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.util.StringUtils.commaDelimitedListToSet;

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

        assertThat(result, is(notNullValue()));
        assertThat(result.entrySet(), is(empty()));
    }

    @Test
    public void getAppsReturnsAppsFilteredByAppName() {
        ServiceInstance instance1 = mock(EurekaServiceInstance.class);
        InstanceInfo info1 = mock(InstanceInfo.class);
        when(instance1 instanceof EurekaServiceInstance).thenReturn(true);
        when(((EurekaServiceInstance) instance1).getInstanceInfo()).thenReturn(info1);
        when(info1.getMetadata()).thenReturn(Collections.singletonMap("allowedAuthorities", "ROLE_USER"));
        when(info1.getVIPAddress()).thenReturn("app1");
        when(info1.getHomePageUrl()).thenReturn("http://app1.example.com");

        ServiceInstance instance2 = mock(EurekaServiceInstance.class);
        InstanceInfo info2 = mock(InstanceInfo.class);
        when(instance2 instanceof EurekaServiceInstance).thenReturn(true);
        when(((EurekaServiceInstance) instance2).getInstanceInfo()).thenReturn(info2);
        when(info2.getMetadata()).thenReturn(Collections.singletonMap("allowedAuthorities", "ROLE_ADMIN"));
        when(info2.getVIPAddress()).thenReturn("app2");
        when(info2.getHomePageUrl()).thenReturn("http://app2.example.com");

        when(discoveryClient.getServices()).thenReturn(Arrays.asList("app1", "app2"));
        when(discoveryClient.getInstances("app1")).thenReturn(Arrays.asList(instance1));
        when(discoveryClient.getInstances("app2")).thenReturn(Arrays.asList(instance2));

        AppQuery query = AppQuery.builder().appNames(Arrays.asList("app1")).build();

        Map<String, List<CoffeeNetApp>> result = service.getApps(query);

        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(1));
        assertThat(result.containsKey("app1"), is(true));
        assertThat(result.get("app1").size(), is(1));
        assertThat(result.get("app1").get(0).getName(), is("app1"));
    }

    @Test
    public void getAppsFiltersOutNonEurekaInstances() {
        ServiceInstance nonEurekaInstance = mock(ServiceInstance.class);
        when(nonEurekaInstance instanceof EurekaServiceInstance).thenReturn(false);

        when(discoveryClient.getServices()).thenReturn(Collections.singletonList("app1"));
        when(discoveryClient.getInstances("app1")).thenReturn(Arrays.asList(nonEurekaInstance));

        Map<String, List<CoffeeNetApp>> result = service.getApps();

        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(0));
    }

    @Test
    public void getAppsFiltersByRole() {
        ServiceInstance instance1 = mock(EurekaServiceInstance.class);
        InstanceInfo info1 = mock(InstanceInfo.class);
        when(instance1 instanceof EurekaServiceInstance).thenReturn(true);
        when(((EurekaServiceInstance) instance1).getInstanceInfo()).thenReturn(info1);
        when(info1.getMetadata()).thenReturn(Collections.singletonMap("allowedAuthorities", "ROLE_USER, ROLE_ADMIN"));
        when(info1.getVIPAddress()).thenReturn("app1");
        when(info1.getHomePageUrl()).thenReturn("http://app1.example.com");

        when(discoveryClient.getServices()).thenReturn(Collections.singletonList("app1"));
        when(discoveryClient.getInstances("app1")).thenReturn(Arrays.asList(instance1));

        AppQuery query = AppQuery.builder().appRoles(Arrays.asList("ROLE_USER")).build();

        Map<String, List<CoffeeNetApp>> result = service.getApps(query);

        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(1));
        assertThat(result.get("app1").size(), is(1));
    }

    @Test
    public void getAppsIgnoresInstancesWithoutAllowedAuthorities() {
        ServiceInstance instance1 = mock(EurekaServiceInstance.class);
        InstanceInfo info1 = mock(InstanceInfo.class);
        when(instance1 instanceof EurekaServiceInstance).thenReturn(true);
        when(((EurekaServiceInstance) instance1).getInstanceInfo()).thenReturn(info1);
        when(info1.getMetadata()).thenReturn(Collections.singletonMap("otherKey", "someValue"));
        when(info1.getVIPAddress()).thenReturn("app1");
        when(info1.getHomePageUrl()).thenReturn("http://app1.example.com");

        when(discoveryClient.getServices()).thenReturn(Collections.singletonList("app1"));
        when(discoveryClient.getInstances("app1")).thenReturn(Arrays.asList(instance1));

        Map<String, List<CoffeeNetApp>> result = service.getApps();

        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(0));
    }

    @Test
    public void getAppsHandlesMultipleInstancesOfSameApp() {
        ServiceInstance instance1 = mock(EurekaServiceInstance.class);
        InstanceInfo info1 = mock(InstanceInfo.class);
        when(instance1 instanceof EurekaServiceInstance).thenReturn(true);
        when(((EurekaServiceInstance) instance1).getInstanceInfo()).thenReturn(info1);
        when(info1.getMetadata()).thenReturn(Collections.singletonMap("allowedAuthorities", "ROLE_USER"));
        when(info1.getVIPAddress()).thenReturn("app1");
        when(info1.getHomePageUrl()).thenReturn("http://app1.example.com");

        ServiceInstance instance2 = mock(EurekaServiceInstance.class);
        InstanceInfo info2 = mock(InstanceInfo.class);
        when(instance2 instanceof EurekaServiceInstance).thenReturn(true);
        when(((EurekaServiceInstance) instance2).getInstanceInfo()).thenReturn(info2);
        when(info2.getMetadata()).thenReturn(Collections.singletonMap("allowedAuthorities", "ROLE_ADMIN"));
        when(info2.getVIPAddress()).thenReturn("app1");
        when(info2.getHomePageUrl()).thenReturn("http://app1.example.com");

        when(discoveryClient.getServices()).thenReturn(Collections.singletonList("app1"));
        when(discoveryClient.getInstances("app1")).thenReturn(Arrays.asList(instance1, instance2));

        Map<String, List<CoffeeNetApp>> result = service.getApps();

        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(1));
        assertThat(result.get("app1").size(), is(2));
    }

    @Test
    public void getAppsReturnsCaseInsensitiveKeys() {
        ServiceInstance instance1 = mock(EurekaServiceInstance.class);
        InstanceInfo info1 = mock(InstanceInfo.class);
        when(instance1 instanceof EurekaServiceInstance).thenReturn(true);
        when(((EurekaServiceInstance) instance1).getInstanceInfo()).thenReturn(info1);
        when(info1.getMetadata()).thenReturn(Collections.singletonMap("allowedAuthorities", "ROLE_USER"));
        when(info1.getVIPAddress()).thenReturn("App1");
        when(info1.getHomePageUrl()).thenReturn("http://app1.example.com");

        when(discoveryClient.getServices()).thenReturn(Collections.singletonList("App1"));
        when(discoveryClient.getInstances("App1")).thenReturn(Arrays.asList(instance1));

        Map<String, List<CoffeeNetApp>> result = service.getApps();

        assertThat(result, is(notNullValue()));
        assertThat(result.containsKey("app1"), is(true));
        assertThat(result.containsKey("APP1"), is(true));
        assertThat(result.containsKey("App1"), is(true));
    }
}
