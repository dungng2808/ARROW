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
            .id("database")
            .name("Database")
            .description("service description")
            .bindable(true)
            .planUpdateable(false)
            .plans(Collections.singletonList(plan))
            .build();

        SuffixedCatalogMapper mapper = new SuffixedCatalogMapper("-target");
        Catalog result = mapper.toCatalog(new Catalog(Collections.singletonList(service)));

        assertEquals(1, result.getServiceDefinitions().size());
        ServiceDefinition mappedService = result.getServiceDefinitions().get(0);
        assertEquals("database-target", mappedService.getId());
        assertEquals("Database-target", mappedService.getName());
        assertEquals("service description", mappedService.getDescription());
        assertEquals(true, mappedService.isBindable());
        assertEquals(false, mappedService.isPlanUpdateable());
        assertEquals(1, mappedService.getPlans().size());
        assertEquals("basic-target", mappedService.getPlans().get(0).getId());
        assertEquals("Basic-target", mappedService.getPlans().get(0).getName());
        assertEquals("plan description", mappedService.getPlans().get(0).getDescription());
    }

    @Test
    public void toCatalogWithEmptyCatalogReturnsEmptyCatalog() {
        SuffixedCatalogMapper mapper = new SuffixedCatalogMapper("-target");

        Catalog result = mapper.toCatalog(new Catalog(Collections.<ServiceDefinition>emptyList()));

        assertEquals(Collections.emptyList(), result.getServiceDefinitions());
    }

    @Test
    public void toCatalogPreservesNullOptionalServiceFields() {
        Plan plan = Plan.builder()
            .id("free")
            .name("Free")
            .build();
        ServiceDefinition service = ServiceDefinition.builder()
            .id("storage")
            .name("Storage")
            .plans(Collections.singletonList(plan))
            .build();

        SuffixedCatalogMapper mapper = new SuffixedCatalogMapper("-copy");
        ServiceDefinition mapped = mapper.toCatalog(new Catalog(Arrays.asList(service)))
            .getServiceDefinitions().get(0);

        assertNull(mapped.getTags());
        assertNull(mapped.getRequires());
        assertNull(mapped.getMetadata());
        assertNull(mapped.getDashboardClient());
        assertNull(mapped.getPlans().get(0).getMetadata());
    }
}
