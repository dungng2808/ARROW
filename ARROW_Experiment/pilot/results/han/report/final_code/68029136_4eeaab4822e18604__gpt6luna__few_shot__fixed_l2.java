package ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationContext;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplicationResolver_ArrowTest {

    @Test
    public void resolveReturnsFalseWhenThereAreNoProvidedSlaveRelations() {
        GenerationContext context = mock(GenerationContext.class);
        when(context.getTargetRelease()).thenReturn(null);
        GenerationOptions options = new GenerationOptions(context);

        ResourceEntity resource = mock(ResourceEntity.class);
        ResourceDependencyResolverService dependencyResolverService =
                mock(ResourceDependencyResolverService.class);
        Set<ProvidedResourceRelationEntity> noRelations =
                Collections.<ProvidedResourceRelationEntity>emptySet();
        when(dependencyResolverService.getProvidedSlaveRelationsForRelease(resource, null))
                .thenReturn(noRelations);

        ApplicationResolver resolver = new ApplicationResolver(options, resource, dependencyResolverService);

        boolean resolved = resolver.resolve();

        assertFalse(resolved);
        assertNull(resolver.getApplication());
        assertNull(resolver.getApplicationServer());
    }
}
