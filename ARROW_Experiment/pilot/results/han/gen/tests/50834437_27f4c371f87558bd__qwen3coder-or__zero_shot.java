package com.orange.cloud.servicebroker.filter.core.service.mapper;

import org.junit.Test;
import org.springframework.cloud.servicebroker.model.catalog.Catalog;
import org.springframework.cloud.servicebroker.model.catalog.Plan;
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SuffixedCatalogMapper_ArrowTest {

    @Test
    public void testWithSuffix() {
        String result = SuffixedCatalogMapper.withSuffix("test", "_suffix");
        assertEquals("test_suffix", result);
    }

    @Test
    public void testToSuffixedServiceDefinition() {
        // Given
        String suffix = "_test_suffix";
        SuffixedCatalogMapper mapper = new SuffixedCatalogMapper(suffix);

        ServiceDefinition serviceDefinition = ServiceDefinition.builder()
            .id("service-id")
            .name("service-name")
            .description("service description")
            .bindable(true)
            .planUpdateable(false)
            .tags(Arrays.asList("tag1", "tag2"))
            .requires(Arrays.asList("requirement1"))
            .metadata(new HashMap<>())
            .dashboardClient(null)
            .plans(Collections.singletonList(
                Plan.builder()
                    .id("plan-id")
                    .name("plan-name")
                    .description("plan description")
                    .metadata(new HashMap<>())
                    .build()
            ))
            .build();

        // When
        ServiceDefinition result = mapper.toSuffixedServiceDefinition(serviceDefinition);

        // Then
        assertEquals("service-id_test_suffix", result.getId());
        assertEquals("service-name_test_suffix", result.getName());
        assertEquals("service description", result.getDescription());
        assertEquals(true, result.isBindable());
        assertEquals(false, result.isPlanUpdateable());
        assertNotNull(result.getPlans());
        assertEquals(1, result.getPlans().size());
        assertEquals("plan-id_test_suffix", result.getPlans().get(0).getId());
        assertEquals("plan-name_test_suffix", result.getPlans().get(0).getName());
        assertEquals("plan description", result.getPlans().get(0).getDescription());
        assertEquals(new HashMap<>(), result.getPlans().get(0).getMetadata());
        assertEquals(Arrays.asList("tag1", "tag2"), result.getTags());
        assertEquals(Arrays.asList("requirement1"), result.getRequires());
        assertEquals(new HashMap<>(), result.getMetadata());
    }

    @Test
    public void testToPlan() {
        // Given
        String suffix = "_plan_suffix";
        SuffixedCatalogMapper mapper = new SuffixedCatalogMapper(suffix);

        Plan plan = Plan.builder()
            .id("plan-id")
            .name("plan-name")
            .description("plan description")
            .metadata(new HashMap<>())
            .build();

        // When
        Plan result = mapper.toPlan(plan);

        // Then
        assertEquals("plan-id_plan_suffix", result.getId());
        assertEquals("plan-name_plan_suffix", result.getName());
        assertEquals("plan description", result.getDescription());
        assertEquals(new HashMap<>(), result.getMetadata());
    }

    @Test
    public void testToCatalog() {
        // Given
        String suffix = "_catalog_suffix";
        SuffixedCatalogMapper mapper = new SuffixedCatalogMapper(suffix);

        ServiceDefinition serviceDefinition = ServiceDefinition.builder()
            .id("service-id")
            .name("service-name")
            .description("service description")
            .bindable(true)
            .planUpdateable(false)
            .plans(Collections.singletonList(
                Plan.builder()
                    .id("plan-id")
                    .name("plan-name")
                    .description("plan description")
                    .metadata(new HashMap<>())
                    .build()
            ))
            .build();

        Catalog catalog = new Catalog(Collections.singletonList(serviceDefinition));

        // When
        Catalog result = mapper.toCatalog(catalog);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getServiceDefinitions().size());
        ServiceDefinition resultServiceDef = result.getServiceDefinitions().get(0);
        assertEquals("service-id_catalog_suffix", resultServiceDef.getId());
        assertEquals("service-name_catalog_suffix", resultServiceDef.getName());
        assertEquals(1, resultServiceDef.getPlans().size());
        assertEquals("plan-id_catalog_suffix", resultServiceDef.getPlans().get(0).getId());
        assertEquals("plan-name_catalog_suffix", resultServiceDef.getPlans().get(0).getName());
    }

    @Test
    public void testNullFieldsHandling() {
        // Given
        String suffix = "_null_test";
        SuffixedCatalogMapper mapper = new SuffixedCatalogMapper(suffix);

        ServiceDefinition serviceDefinition = ServiceDefinition.builder()
            .id("service-id")
            .name("service-name")
            .description("service description")
            .bindable(true)
            .planUpdateable(false)
            .plans(Collections.singletonList(
                Plan.builder()
                    .id("plan-id")
                    .name("plan-name")
                    .description("plan description")
                    .build()
            ))
            .build();

        Catalog catalog = new Catalog(Collections.singletonList(serviceDefinition));

        // When
        Catalog result = mapper.toCatalog(catalog);

        // Then
        assertNotNull(result);
        ServiceDefinition resultServiceDef = result.getServiceDefinitions().get(0);
        assertEquals("service-id_null_test", resultServiceDef.getId());
        assertEquals("service-name_null_test", resultServiceDef.getName());
        assertEquals("service description", resultServiceDef.getDescription());
        assertEquals(true, resultServiceDef.isBindable());
        assertEquals(false, resultServiceDef.isPlanUpdateable());
        assertEquals(1, resultServiceDef.getPlans().size());
        assertEquals("plan-id_null_test", resultServiceDef.getPlans().get(0).getId());
        assertEquals("plan-name_null_test", resultServiceDef.getPlans().get(0).getName());
        assertEquals("plan description", resultServiceDef.getPlans().get(0).getDescription());
    }
}
