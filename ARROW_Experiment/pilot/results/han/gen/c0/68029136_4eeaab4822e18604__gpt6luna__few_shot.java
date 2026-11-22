package ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class ApplicationResolver_ArrowTest {

    @Test
    public void resolveReturnsFalseWhenThereAreNoProvidedSlaveRelations() {
        GenerationOptions options = new GenerationOptions();
        ResourceEntity resource = new ResourceEntity();
        ResourceDependencyResolverService dependencyResolverService = new ResourceDependencyResolverService() {
            @Override
            public Set<ProvidedResourceRelationEntity> getProvidedSlaveRelationsForRelease(
                    ResourceEntity ignored, Object release) {
                return Collections.emptySet();
            }
        };

        ApplicationResolver resolver = new ApplicationResolver(options, resource, dependencyResolverService);

        assertFalse(resolver.resolve());
        assertNull(resolver.getApplication());
        assertNull(resolver.getApplicationServer());
    }
}
