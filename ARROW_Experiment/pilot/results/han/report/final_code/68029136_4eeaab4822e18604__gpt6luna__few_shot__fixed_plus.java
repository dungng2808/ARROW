package ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplicationResolver_ArrowTest {

    @Test
    public void resolveReturnsFalseWhenThereAreNoProvidedSlaveRelations() {
        GenerationOptions options = mock(GenerationOptions.class);
        when(options.getContext()).thenReturn(null);

        ResourceEntity resource = new ResourceEntity();
        ResourceDependencyResolverService dependencyResolverService =
                mock(ResourceDependencyResolverService.class);
        when(dependencyResolverService.getProvidedSlaveRelationsForRelease(resource, null))
                .thenReturn(Collections.emptySet());

        ApplicationResolver resolver = new ApplicationResolver(options, resource, dependencyResolverService);

        assertFalse(resolver.resolve());
        assertNull(resolver.getApplication());
        assertNull(resolver.getApplicationServer());
    }
}
