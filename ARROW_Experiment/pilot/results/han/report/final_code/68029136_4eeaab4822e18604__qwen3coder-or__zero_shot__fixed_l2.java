package ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates;

import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwAppServerNodeModel;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ApplicationResolver_ArrowTest {

    @Test
    public void testResolve_NoProvidedSlaveRelations() {
        // Arrange
        ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationOptions options = mock(ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationOptions.class);
        ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.Context context = mock(ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.Context.class);
        when(options.getContext()).thenReturn(context);
        ResourceEntity slave = mock(ResourceEntity.class);
        ResourceDependencyResolverService resolverService = mock(ResourceDependencyResolverService.class);
        
        Set<ProvidedResourceRelationEntity> relations = Collections.emptySet();
        when(resolverService.getProvidedSlaveRelationsForRelease(slave, context.getTargetRelease())).thenReturn(relations);
        
        ApplicationResolver resolver = new ApplicationResolver(options, slave, resolverService);
        
        // Act
        boolean result = resolver.resolve();
        
        // Assert
        assertFalse(result);
    }

    @Test
    public void testResolve_MultipleProvidedSlaveRelations() {
        // Arrange
        ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationOptions options = mock(ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationOptions.class);
        ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.Context context = mock(ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.Context.class);
        when(options.getContext()).thenReturn(context);
        ResourceEntity slave = mock(ResourceEntity.class);
        ResourceDependencyResolverService resolverService = mock(ResourceDependencyResolverService.class);
        
        ProvidedResourceRelationEntity relation1 = mock(ProvidedResourceRelationEntity.class);
        ProvidedResourceRelationEntity relation2 = mock(ProvidedResourceRelationEntity.class);
        Set<ProvidedResourceRelationEntity> relations = new HashSet<>();
        relations.add(relation1);
        relations.add(relation2);
        
        when(resolverService.getProvidedSlaveRelationsForRelease(slave, context.getTargetRelease())).thenReturn(relations);
        when(slave.toString()).thenReturn("slaveResource");
        
        ApplicationResolver resolver = new ApplicationResolver(options, slave, resolverService);
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> resolver.resolve());
    }

    @Test
    public void testResolve_SingleProvidedSlaveRelation_ApplicationMissing() {
        // Arrange
        ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationOptions options = mock(ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationOptions.class);
        ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.Context context = mock(ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.Context.class);
        when(options.getContext()).thenReturn(context);
        ResourceEntity slave = mock(ResourceEntity.class);
        ResourceDependencyResolverService resolverService = mock(ResourceDependencyResolverService.class);
        
        ProvidedResourceRelationEntity relation = mock(ProvidedResourceRelationEntity.class);
        Set<ProvidedResourceRelationEntity> relations = Collections.singleton(relation);
        when(resolverService.getProvidedSlaveRelationsForRelease(slave, context.getTargetRelease())).thenReturn(relations);
        
        ApplicationResolver resolver = new ApplicationResolver(options, slave, resolverService);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> resolver.resolve());
        assertEquals("application missing", exception.getMessage());
    }

    @Test
    public void testResolve_Success() {
        // Arrange
        ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationOptions options = mock(ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationOptions.class);
        ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.Context context = mock(ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.Context.class);
        when(options.getContext()).thenReturn(context);
        ResourceEntity slave = mock(ResourceEntity.class);
        ResourceEntity application = mock(ResourceEntity.class);
        ResourceEntity applicationServer = mock(ResourceEntity.class);
        ResourceDependencyResolverService resolverService = mock(ResourceDependencyResolverService.class);
        
        ProvidedResourceRelationEntity relation = mock(ProvidedResourceRelationEntity.class);
        Set<ProvidedResourceRelationEntity> relations = Collections.singleton(relation);
        when(resolverService.getProvidedSlaveRelationsForRelease(slave, context.getTargetRelease())).thenReturn(relations);
        
        ResourceGroupEntity appGroup = mock(ResourceGroupEntity.class);
        when(application.getResourceGroup()).thenReturn(appGroup);
        when(context.getResourceEntityForRelease(appGroup)).thenReturn(application);
        
        List<ResourceEntity> appServers = Collections.singletonList(applicationServer);
        when(application.getMasterResourcesOfConsumedSlaveRelationByResourceType(DefaultResourceTypeDefinition.APPLICATIONSERVER))
                .thenReturn(appServers);
        
        when(context.getResourceEntityForRelease(any(ResourceGroupEntity.class))).thenReturn(applicationServer);
        
        ApplicationResolver resolver = new ApplicationResolver(options, slave, resolverService);
        
        // Act
        boolean result = resolver.resolve();
        
        // Assert
        assertTrue(result);
        assertEquals(application, resolver.getApplication());
        assertEquals(applicationServer, resolver.getApplicationServer());
    }

    @Test
    public void testTransform() {
        // Arrange
        ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationOptions options = mock(ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationOptions.class);
        ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.Context context = mock(ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.Context.class);
        when(options.getContext()).thenReturn(context);
        ResourceEntity slave = mock(ResourceEntity.class);
        ResourceDependencyResolverService resolverService = mock(ResourceDependencyResolverService.class);
        
        ApplicationResolver resolver = new ApplicationResolver(options, slave, resolverService);
        
        AMWTemplateExceptionHandler handler = mock(AMWTemplateExceptionHandler.class);
        AmwAppServerNodeModel model = mock(AmwAppServerNodeModel.class);
        
        ResourceEntity app = mock(ResourceEntity.class);
        ResourceEntity appServer = mock(ResourceEntity.class);
        
        when(resolver.getApplication()).thenReturn(app);
        when(resolver.getApplicationServer()).thenReturn(appServer);
        
        // Act
        resolver.transform(handler, model);
        
        // Assert
        verify(model).setAppProperties(any());
        verify(model).setAppServerProperties(any());
        verify(model).setNodePropertyList(anyList());
    }

    // Inner classes matching the actual implementation
    static class GenerationOptions {
        private Context context;
        
        public Context getContext() {
            return context;
        }
        
        public void setContext(Context context) {
            this.context = context;
        }
    }
    
    static class Context {
        private Object targetRelease;
        private Object context;
        
        public Object getTargetRelease() {
            return targetRelease;
        }
        
        public void setTargetRelease(Object targetRelease) {
            this.targetRelease = targetRelease;
        }
        
        public Object getContext() {
            return context;
        }
        
        public void setContext(Object context) {
            this.context = context;
        }
        
        public ResourceEntity getResourceEntityForRelease(ResourceGroupEntity resourceGroup) {
            return mock(ResourceEntity.class);
        }
    }
}
