package ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates;

import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationContext;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwAppServerNodeModel;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ApplicationResolver_ArrowTest {

    private ResourceEntity slave;
    private ResourceDependencyResolverService resolverService;
    private GenerationOptions options;
    private GenerationContext context;
    private ProvidedResourceRelationEntity relation1;
    private ProvidedResourceRelationEntity relation2;
    private ResourceEntity application;
    private ResourceEntity applicationServer;
    private ResourceEntity masterResource;
    private ResourceGroupEntity resourceGroup;
    private AMWTemplateExceptionHandler templateExceptionHandler;
    private AmwAppServerNodeModel model;

    @Before
    public void setUp() {
        slave = mock(ResourceEntity.class);
        resolverService = mock(ResourceDependencyResolverService.class);
        options = mock(GenerationOptions.class);
        context = mock(GenerationContext.class);
        relation1 = mock(ProvidedResourceRelationEntity.class);
        relation2 = mock(ProvidedResourceRelationEntity.class);
        application = mock(ResourceEntity.class);
        applicationServer = mock(ResourceEntity.class);
        masterResource = mock(ResourceEntity.class);
        resourceGroup = mock(ResourceGroupEntity.class);
        templateExceptionHandler = mock(AMWTemplateExceptionHandler.class);
        model = mock(AmwAppServerNodeModel.class);
    }

    @Test
    public void testResolveWithNoProvidedSlaveRelations() {
        // Setup
        when(resolverService.getProvidedSlaveRelationsForRelease(slave, null)).thenReturn(Collections.emptySet());
        when(options.getContext()).thenReturn(context);
        
        ApplicationResolver resolver = new ApplicationResolver(options, slave, resolverService);
        
        // Execute
        boolean result = resolver.resolve();
        
        // Verify
        assertFalse(result);
    }

    @Test
    public void testResolveWithMultipleProvidedSlaveRelations() {
        // Setup
        Set<ProvidedResourceRelationEntity> relations = new HashSet<>();
        relations.add(relation1);
        relations.add(relation2);
        
        when(resolverService.getProvidedSlaveRelationsForRelease(slave, null)).thenReturn(relations);
        when(slave.toString()).thenReturn("slave-resource");
        when(options.getContext()).thenReturn(context);
        
        ApplicationResolver resolver = new ApplicationResolver(options, slave, resolverService);
        
        // Execute & Verify
        try {
            resolver.resolve();
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("slave-resource is provided by multiple other resources"));
        }
    }

    @Test
    public void testResolveWithSingleProvidedSlaveRelation() {
        // Setup
        ProvidedResourceRelationEntity relation = mock(ProvidedResourceRelationEntity.class);
        Set<ProvidedResourceRelationEntity> relations = Collections.singleton(relation);
        
        when(resolverService.getProvidedSlaveRelationsForRelease(slave, null)).thenReturn(relations);
        when(relation.toString()).thenReturn("relation1");
        when(slave.getMasterResourcesOfProvidedSlaveRelations()).thenReturn(Collections.singletonList(masterResource));
        when(masterResource.getResourceGroup()).thenReturn(resourceGroup);
        when(context.getResourceEntityForRelease(resourceGroup)).thenReturn(application);
        when(options.getContext()).thenReturn(context);
        
        ApplicationResolver resolver = new ApplicationResolver(options, slave, resolverService);
        
        // Execute
        boolean result = resolver.resolve();
        
        // Verify
        assertTrue(result);
        assertNotNull(resolver.getApplication());
        assertNotNull(resolver.getApplicationServer());
    }

    @Test
    public void testTransform() {
        // Setup
        ApplicationResolver resolver = new ApplicationResolver(options, null, null);
        
        // Mock the getters
        try {
            // We need to use reflection to set the application and applicationServer fields
            java.lang.reflect.Field appField = ApplicationResolver.class.getDeclaredField("application");
            appField.setAccessible(true);
            appField.set(resolver, application);
            
            java.lang.reflect.Field appServerField = ApplicationResolver.class.getDeclaredField("applicationServer");
            appServerField.setAccessible(true);
            appServerField.set(resolver, applicationServer);
            
        } catch (Exception e) {
            fail("Failed to set fields via reflection");
        }
        
        when(options.getContext()).thenReturn(context);
        when(context.getContext()).thenReturn("test-context");
        
        // Execute
        resolver.transform(templateExceptionHandler, model);
        
        // Verify
        verify(model).setAppProperties(any());
        verify(model).setAppServerProperties(any());
        verify(model).setNodePropertyList(anyList());
    }
}
