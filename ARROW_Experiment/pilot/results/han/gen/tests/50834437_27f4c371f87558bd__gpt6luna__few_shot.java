package com.orange.cloud.servicebroker.filter.core.service.mapper;

import org.junit.Test;
import org.springframework.cloud.servicebroker.model.catalog.Catalog;
import org.springframework.cloud.servicebroker.model.catalog.Plan;
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SuffixedCatalogMapper_ArrowTest {

    @Test
    public void addsSuffixesToServiceAndPlanIdsAndNames() {
        Plan plan = Plan.builder()
                .id("basic")
                .name("Basic")
                .description("Plan description")
                .build();
        ServiceDefinition service = ServiceDefinition.builder()
                .id("service")
                .name("Service")
                .description("Service description")
                .bindable(true)
                .planUpdateable(false)
                .plans(Collections.singletonList(plan))
                .build();
        Catalog catalog = new Catalog(Collections.singletonList(service));

        Catalog result = new SuffixedCatalogMapper("-target").toCatalog(catalog);

        assertEquals(1, result.getServiceDefinitions().size());
        ServiceDefinition mappedService = result.getServiceDefinitions().get(0);
        assertEquals("service-target", mappedService.getId());
        assertEquals("Service-target", mappedService.getName());
        assertEquals("Service description", mappedService.getDescription());
        assertTrue(mappedService.isBindable());
        assertFalse(mappedService.isPlanUpdateable());
        assertEquals(1, mappedService.getPlans().size());
        assertEquals("basic-target", mappedService.getPlans().get(0).getId());
        assertEquals("Basic-target", mappedService.getPlans().get(0).getName());
        assertEquals("Plan description", mappedService.getPlans().get(0).getDescription());
    }

    @Test
    public void mapsEmptyCatalogToEmptyCatalog() {
        Catalog result = new SuffixedCatalogMapper("-suffix")
                .toCatalog(new Catalog(Collections.<ServiceDefinition>emptyList()));

        assertNotNull(result);
        assertTrue(result.getServiceDefinitions().isEmpty());
    }

    @Test
    public void mapsMultipleServicesAndPlans() {
        Plan firstPlan = Plan.builder().id("one").name("One").build();
        Plan secondPlan = Plan.builder().id("two").name("Two").build();
        ServiceDefinition firstService = ServiceDefinition.builder()
                .id("first").name("First").plans(Collections.singletonList(firstPlan)).build();
        ServiceDefinition secondService = ServiceDefinition.builder()
                .id("second").name("Second").plans(Collections.singletonList(secondPlan)).build();

        Catalog result = new SuffixedCatalogMapper("_copy")
                .toCatalog(new Catalog(Arrays.asList(firstService, secondService)));

        assertEquals(2, result.getServiceDefinitions().size());
        assertEquals("first_copy", result.getServiceDefinitions().get(0).getId());
        assertEquals("one_copy", result.getServiceDefinitions().get(0).getPlans().get(0).getId());
        assertEquals("second_copy", result.getServiceDefinitions().get(1).getId());
        assertEquals("two_copy", result.getServiceDefinitions().get(1).getPlans().get(0).getId());
    }
}
