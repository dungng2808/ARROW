package com.orange.cloud.servicebroker.filter.core.service.mapper;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.springframework.cloud.servicebroker.model.catalog.Catalog;
import org.springframework.cloud.servicebroker.model.catalog.Plan;
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SuffixedCatalogMapper_ArrowTest {

    @Test
    public void toCatalogSuffixesServiceAndPlanIdsAndNames() {
        Plan plan = Plan.builder()
            .id("basic")
            .name("Basic")
            .description("plan description")
            .build();
        ServiceDefinition service = ServiceDefinition.builder()
            .id("service")
            .name("Service")
            .description("service description")
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
        assertEquals("service description", mappedService.getDescription());
        assertEquals(true, mappedService.isBindable());
        assertEquals(false, mappedService.isPlanUpdateable());
        assertEquals(1, mappedService.getPlans().size());
        Plan mappedPlan = mappedService.getPlans().get(0);
        assertEquals("basic-target", mappedPlan.getId());
        assertEquals("Basic-target", mappedPlan.getName());
        assertEquals("plan description", mappedPlan.getDescription());
    }

    @Test
    public void toCatalogPreservesEmptyCatalog() {
        Catalog result = new SuffixedCatalogMapper("-suffix")
            .toCatalog(new Catalog(Collections.<ServiceDefinition>emptyList()));

        assertEquals(Collections.emptyList(), result.getServiceDefinitions());
    }

    @Test
    public void toCatalogPreservesOptionalServiceFieldsWhenPresent() {
        ServiceDefinition service = ServiceDefinition.builder()
            .id("id")
            .name("name")
            .description("description")
            .bindable(false)
            .planUpdateable(true)
            .plans(Collections.<Plan>emptyList())
            .tags(Arrays.asList("one", "two"))
            .build();

        ServiceDefinition result = new SuffixedCatalogMapper("_x")
            .toCatalog(new Catalog(Collections.singletonList(service)))
            .getServiceDefinitions().get(0);

        assertEquals(Arrays.asList("one", "two"), result.getTags());
        assertNull(result.getRequires());
        assertNull(result.getMetadata());
    }
}
