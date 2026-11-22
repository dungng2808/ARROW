package com.orange.cloud.servicebroker.filter.core.service.mapper;

import org.junit.Test;
import org.springframework.cloud.servicebroker.model.catalog.Catalog;
import org.springframework.cloud.servicebroker.model.catalog.Plan;
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SuffixedCatalogMapper_ArrowTest {

    @Test
    public void testToCatalog() {
        // Given
        String suffix = "_suffix";
        SuffixedCatalogMapper mapper = new SuffixedCatalogMapper(suffix);

        ServiceDefinition serviceDefinition = ServiceDefinition.builder()
            .id("service-id")
            .name("service-name")
            .description("service-description")
            .bindable(true)
            .planUpdateable(false)
            .tags(Arrays.asList("tag1", "tag2"))
            .requires(Arrays.asList("requirement1"))
            .metadata(Collections.singletonMap("key", "value"))
            .plans(Arrays.asList(
                Plan.builder()
                    .id("plan-id")
                    .name("plan-name")
                    .description("plan-description")
                    .metadata(Collections.singletonMap("plan-key", "plan-value"))
                    .build()
            ))
            .dashboardClient(null)
            .build();

        Catalog inputCatalog = new Catalog(Arrays.asList(serviceDefinition));

        // When
        Catalog result = mapper.toCatalog(inputCatalog);

        // Then
        assertEquals(1, result.getServiceDefinitions().size());
        ServiceDefinition resultService = result.getServiceDefinitions().get(0);
        assertEquals("service-id_suffix", resultService.getId());
        assertEquals("service-name_suffix", resultService.getName());
        assertEquals("service-description", resultService.getDescription());
        assertEquals(true, resultService.isBindable());
        assertEquals(false, resultService.isPlanUpdateable());
        assertEquals(Arrays.asList("tag1", "tag2"), resultService.getTags());
        assertEquals(Arrays.asList("requirement1"), resultService.getRequires());
        assertEquals(Collections.singletonMap("key", "value"), resultService.getMetadata());
        assertNull(resultService.getDashboardClient());

        assertEquals(1, resultService.getPlans().size());
        Plan resultPlan = resultService.getPlans().get(0);
        assertEquals("plan-id_suffix", resultPlan.getId());
        assertEquals("plan-name_suffix", resultPlan.getName());
        assertEquals("plan-description", resultPlan.getDescription());
        assertEquals(Collections.singletonMap("plan-key", "plan-value"), resultPlan.getMetadata());
    }

    @Test
    public void testToCatalogWithNullFields() {
        // Given
        String suffix = "_test";
        SuffixedCatalogMapper mapper = new SuffixedCatalogMapper(suffix);

        ServiceDefinition serviceDefinition = ServiceDefinition.builder()
            .id("service-id")
            .name("service-name")
            .description("service-description")
            .bindable(false)
            .planUpdateable(true)
            .plans(Collections.singletonList(
                Plan.builder()
                    .id("plan-id")
                    .name("plan-name")
                    .description("plan-description")
                    .build()
            ))
            .build();

        Catalog inputCatalog = new Catalog(Arrays.asList(serviceDefinition));

        // When
        Catalog result = mapper.toCatalog(inputCatalog);

        // Then
        assertEquals(1, result.getServiceDefinitions().size());
        ServiceDefinition resultService = result.getServiceDefinitions().get(0);
        assertEquals("service-id_test", resultService.getId());
        assertEquals("service-name_test", resultService.getName());
        assertEquals("service-description", resultService.getDescription());
        assertEquals(false, resultService.isBindable());
        assertEquals(true, resultService.isPlanUpdateable());
        assertNull(resultService.getTags());
        assertNull(resultService.getRequires());
        assertNull(resultService.getMetadata());
        assertNull(resultService.getDashboardClient());

        assertEquals(1, resultService.getPlans().size());
        Plan resultPlan = resultService.getPlans().get(0);
        assertEquals("plan-id_test", resultPlan.getId());
        assertEquals("plan-name_test", resultPlan.getName());
        assertEquals("plan-description", resultPlan.getDescription());
        assertNull(resultPlan.getMetadata());
    }

    @Test
    public void testWithSuffix() {
        // Given
        String suffix = "_suffix";

        // When
        String result = SuffixedCatalogMapper.withSuffix("test", suffix);

        // Then
        assertEquals("test_suffix", result);
    }

    @Test
    public void testWithSuffixEmptySuffix() {
        // Given
        String suffix = "";

        // When
        String result = SuffixedCatalogMapper.withSuffix("test", suffix);

        // Then
        assertEquals("test", result);
    }
}
