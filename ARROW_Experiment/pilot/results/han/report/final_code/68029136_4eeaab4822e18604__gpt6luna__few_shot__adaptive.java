package ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ApplicationResolver_ArrowTest {

    @Test
    public void resolveReturnsFalseWhenThereAreNoProvidedSlaveRelations() {
        GenerationOptions options = org.mockito.Mockito.mock(GenerationOptions.class);
        ResourceEntity resource = new ResourceEntity();
        ResourceDependencyResolverService dependencyResolverService = org.mockito.Mockito.mock(ResourceDependencyResolverService.class);
        org.mockito.Mockito.when(dependencyResolverService.getProvidedSlaveRelationsForRelease(
                org.mockito.ArgumentMatchers.eq(resource), org.mockito.ArgumentMatchers.any()))
                .thenReturn(Collections.<ProvidedResourceRelationEntity>emptySet());

        ApplicationResolver resolver = new ApplicationResolver(options, resource, dependencyResolverService);

        assertFalse(resolver.resolve());
        assertNull(resolver.getApplication());
        assertNull(resolver.getApplicationServer());
    }
}
