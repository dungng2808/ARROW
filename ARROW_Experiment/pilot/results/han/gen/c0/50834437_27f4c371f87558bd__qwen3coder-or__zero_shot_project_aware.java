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

import static org.assertj.core.api.Assertions.assertThat;

public class SuffixedCatalogMapper_ArrowTest {

    @Test
    public void testWithSuffix() {
        String result = SuffixedCatalogMapper.withSuffix("test", "_suffix");
        assertThat(result).isEqualTo("test_suffix");
    }

    @Test
    public void testToCatalog_EmptyCatalog() {
        Catalog input = new Catalog(Collections.emptyList());
        SuffixedCatalogMapper mapper = new SuffixedCatalogMapper("_suffix");
        Catalog result = mapper.toCatalog(input);
        
        assertThat(result).isNotNull();
        assertThat(result.getServiceDefinitions()).isEmpty();
    }

    @Test
    public void testToCatalog_SingleServiceDefinition() {
        Plan plan = Plan.builder()
            .id("plan-id")
            .name("plan-name")
            .description("plan-description")
            .build();
        
        ServiceDefinition serviceDef = ServiceDefinition.builder()
            .id("service-id")
            .name("service-name")
            .description("service-description")
            .bindable(true)
            .planUpdateable(false)
            .plans(Arrays.asList(plan))
            .build();
        
        Catalog input = new Catalog(Arrays.asList(serviceDef));
        SuffixedCatalogMapper mapper = new SuffixedCatalogMapper("_suffix");
        Catalog result = mapper.toCatalog(input);
        
        assertThat(result).isNotNull();
        assertThat(result.getServiceDefinitions()).hasSize(1);
        
        ServiceDefinition resultServiceDef = result.getServiceDefinitions().get(0);
        assertThat(resultServiceDef.getId()).isEqualTo("service-id_suffix");
        assertThat(resultServiceDef.getName()).isEqualTo("service-name_suffix");
        assertThat(resultServiceDef.getDescription()).isEqualTo("service-description");
        assertThat(resultServiceDef.isBindable()).isTrue();
        assertThat(resultServiceDef.isPlanUpdateable()).isFalse();
        assertThat(resultServiceDef.getPlans()).hasSize(1);
        
        Plan resultPlan = resultServiceDef.getPlans().get(0);
        assertThat(resultPlan.getId()).isEqualTo("plan-id_suffix");
        assertThat(resultPlan.getName()).isEqualTo("plan-name_suffix");
        assertThat(resultPlan.getDescription()).isEqualTo("plan-description");
    }

    @Test
    public void testToCatalog_ServiceDefinitionWithNullFields() {
        Plan plan = Plan.builder()
            .id("plan-id")
            .name("plan-name")
            .build();
        
        ServiceDefinition serviceDef = ServiceDefinition.builder()
            .id("service-id")
            .name("service-name")
            .plans(Arrays.asList(plan))
            .build();
        
        Catalog input = new Catalog(Arrays.asList(serviceDef));
        SuffixedCatalogMapper mapper = new SuffixedCatalogMapper("_suffix");
        Catalog result = mapper.toCatalog(input);
        
        assertThat(result).isNotNull();
        assertThat(result.getServiceDefinitions()).hasSize(1);
        
        ServiceDefinition resultServiceDef = result.getServiceDefinitions().get(0);
        assertThat(resultServiceDef.getId()).isEqualTo("service-id_suffix");
        assertThat(resultServiceDef.getName()).isEqualTo("service-name_suffix");
        assertThat(resultServiceDef.getDescription()).isNull();
        assertThat(resultServiceDef.isBindable()).isFalse(); // default value
        assertThat(resultServiceDef.isPlanUpdateable()).isFalse(); // default value
        assertThat(resultServiceDef.getPlans()).hasSize(1);
        
        Plan resultPlan = resultServiceDef.getPlans().get(0);
        assertThat(resultPlan.getId()).isEqualTo("plan-id_suffix");
        assertThat(resultPlan.getName()).isEqualTo("plan-name_suffix");
        assertThat(resultPlan.getDescription()).isNull();
    }

    @Test
    public void testToCatalog_MultipleServiceDefinitions() {
        Plan plan1 = Plan.builder()
            .id("plan1-id")
            .name("plan1-name")
            .build();
            
        Plan plan2 = Plan.builder()
            .id("plan2-id")
            .name("plan2-name")
            .build();
        
        ServiceDefinition serviceDef1 = ServiceDefinition.builder()
            .id("service1-id")
            .name("service1-name")
            .plans(Arrays.asList(plan1))
            .build();
            
        ServiceDefinition serviceDef2 = ServiceDefinition.builder()
            .id("service2-id")
            .name("service2-name")
            .plans(Arrays.asList(plan2))
            .build();
        
        Catalog input = new Catalog(Arrays.asList(serviceDef1, serviceDef2));
        SuffixedCatalogMapper mapper = new SuffixedCatalogMapper("_suffix");
        Catalog result = mapper.toCatalog(input);
        
        assertThat(result).isNotNull();
        assertThat(result.getServiceDefinitions()).hasSize(2);
        
        ServiceDefinition resultServiceDef1 = result.getServiceDefinitions().get(0);
        assertThat(resultServiceDef1.getId()).isEqualTo("service1-id_suffix");
        assertThat(resultServiceDef1.getName()).isEqualTo("service1-name_suffix");
        assertThat(resultServiceDef1.getPlans()).hasSize(1);
        
        ServiceDefinition resultServiceDef2 = result.getServiceDefinitions().get(1);
        assertThat(resultServiceDef2.getId()).isEqualTo("service2-id_suffix");
        assertThat(resultServiceDef2.getName()).isEqualTo("service2-name_suffix");
        assertThat(resultServiceDef2.getPlans()).hasSize(1);
    }

    @Test
    public void testToCatalog_ServiceDefinitionWithTagsAndRequires() {
        Plan plan = Plan.builder()
            .id("plan-id")
            .name("plan-name")
            .build();
        
        List<String> tags = Arrays.asList("tag1", "tag2");
        List<String> requires = Arrays.asList("requirement1", "requirement2");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");
        
        ServiceDefinition serviceDef = ServiceDefinition.builder()
            .id("service-id")
            .name("service-name")
            .tags(tags)
            .requires(requires)
            .metadata(metadata)
            .plans(Arrays.asList(plan))
            .build();
        
        Catalog input = new Catalog(Arrays.asList(serviceDef));
        SuffixedCatalogMapper mapper = new SuffixedCatalogMapper("_suffix");
        Catalog result = mapper.toCatalog(input);
        
        assertThat(result).isNotNull();
        assertThat(result.getServiceDefinitions()).hasSize(1);
        
        ServiceDefinition resultServiceDef = result.getServiceDefinitions().get(0);
        assertThat(resultServiceDef.getId()).isEqualTo("service-id_suffix");
        assertThat(resultServiceDef.getName()).isEqualTo("service-name_suffix");
        assertThat(resultServiceDef.getTags()).containsExactly("tag1", "tag2");
        assertThat(resultServiceDef.getRequires()).containsExactly("requirement1", "requirement2");
        assertThat(resultServiceDef.getMetadata()).containsEntry("key", "value");
    }
}
