package ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates;

import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.property.entity.AmwAppServerNodeModel;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplicationResolver_ArrowTest {

    @Mock
    private GenerationOptions options;

    @Mock
    private ResourceEntity slave;

    @Mock
    private ResourceDependencyResolverService dependencyResolverService;

    @Mock
    private ResourceEntity application;

    @Mock
    private ResourceEntity applicationServer;

    @Mock
    private ResourceGroupEntity resourceGroup;

    @Mock
    private ProvidedResourceRelationEntity relation1;

    @Mock
    private ProvidedResourceRelationEntity relation2;

    @Mock
    private ResourceEntity masterResource1;

    @Mock
    private ResourceEntity masterResource2;

    @Mock
    private AmwAppServerNodeModel model;

    @Mock
    private AMWTemplateExceptionHandler templateExceptionHandler;

    private ApplicationResolver resolver;

    @BeforeEach
    public void setUp() {
        resolver = new ApplicationResolver(options, slave, dependencyResolverService);
        when(options.getContext()).thenReturn(null);
    }

    @Test
    public void testResolveReturnsFalseWhenNoProvidedSlaveRelations() {
        when(dependencyResolverService.getProvidedSlaveRelationsForRelease(slave, null))
                .thenReturn(Collections.emptySet());

        boolean result = resolver.resolve();

        assertFalse(result);
    }

    @Test
    public void testResolveThrowsRuntimeExceptionWhenMultipleProvidedSlaveRelations() {
        Set<ProvidedResourceRelationEntity> relations = new HashSet<>();
        relations.add(relation1);
        relations.add(relation2);

        when(dependencyResolverService.getProvidedSlaveRelationsForRelease(slave, null))
                .thenReturn(relations);

        assertThrows(RuntimeException.class, () -> resolver.resolve());
    }

    @Test
    public void testResolveReturnsTrueAndSetsApplicationAndApplicationServerWhenOneProvidedSlaveRelation() {
        Set<ProvidedResourceRelationEntity> relations = new HashSet<>();
        relations.add(relation1);

        when(dependencyResolverService.getProvidedSlaveRelationsForRelease(slave, null))
                .thenReturn(relations);
        when(slave.getMasterResourcesOfProvidedSlaveRelations()).thenReturn(Arrays.asList(masterResource1));
        when(masterResource1.getResourceGroup()).thenReturn(resourceGroup);
        when(options.getContext().getResourceEntityForRelease(resourceGroup)).thenReturn(application);
        when(application.getMasterResourcesOfConsumedSlaveRelationByResourceType(DefaultResourceTypeDefinition.APPLICATIONSERVER))
                .thenReturn(Arrays.asList(masterResource2));

        boolean result = resolver.resolve();

        assertTrue(result);
        assertEquals(application, resolver.getApplication());
        assertEquals(masterResource2, resolver.getApplicationServer());
    }

    @Test
    public void testResolveThrowsRuntimeExceptionWhenApplicationIsNull() {
        Set<ProvidedResourceRelationEntity> relations = new HashSet<>();
        relations.add(relation1);

        when(dependencyResolverService.getProvidedSlaveRelationsForRelease(slave, null))
                .thenReturn(relations);
        when(slave.getMasterResourcesOfProvidedSlaveRelations()).thenReturn(Arrays.asList(masterResource1));
        when(masterResource1.getResourceGroup()).thenReturn(resourceGroup);
        when(options.getContext().getResourceEntityForRelease(resourceGroup)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> resolver.resolve());
    }

    @Test
    public void testTransformSetsPropertiesOnModel() {
        when(resolver.getApplication()).thenReturn(application);
        when(resolver.getApplicationServer()).thenReturn(applicationServer);

        resolver.transform(templateExceptionHandler, model);

        verify(model).setAppProperties(any());
        verify(model).setAppServerProperties(any());
        verify(model).setNodePropertyList(anyList());
    }
}
