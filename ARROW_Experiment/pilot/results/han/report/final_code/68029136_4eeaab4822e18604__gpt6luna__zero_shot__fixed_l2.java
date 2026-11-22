package ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationContext;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplicationResolver_ArrowTest {

    @Test
    public void resolveReturnsFalseWhenNoProvidedSlaveRelationsExist() {
        ResourceEntity resource = mock(ResourceEntity.class);
        ResourceDependencyResolverService dependencyResolver = mock(ResourceDependencyResolverService.class);
        GenerationContext context = mock(GenerationContext.class);

        when(context.getTargetRelease()).thenReturn(null);
        when(dependencyResolver.getProvidedSlaveRelationsForRelease(resource, null))
                .thenReturn(Collections.<ProvidedResourceRelationEntity>emptySet());

        GenerationOptions options = mock(GenerationOptions.class);
        when(options.getContext()).thenReturn(context);
        ApplicationResolver resolver = new ApplicationResolver(options, resource, dependencyResolver);

        assertFalse(resolver.resolve());
        assertNull(resolver.getApplication());
        assertNull(resolver.getApplicationServer());
    }
}
