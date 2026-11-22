package ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ApplicationResolver_ArrowTest {

    @Test
    public void resolveReturnsFalseWhenNoProvidedSlaveRelationsExist() {
        ResourceEntity resource = (ResourceEntity) Proxy.newProxyInstance(
                ResourceEntity.class.getClassLoader(),
                new Class<?>[]{ResourceEntity.class},
                (proxy, method, args) -> defaultValue(method.getReturnType()));

        ResourceDependencyResolverService dependencyResolver = (ResourceDependencyResolverService) Proxy.newProxyInstance(
                ResourceDependencyResolverService.class.getClassLoader(),
                new Class<?>[]{ResourceDependencyResolverService.class},
                (proxy, method, args) -> {
                    if ("getProvidedSlaveRelationsForRelease".equals(method.getName())) {
                        return Collections.<ProvidedResourceRelationEntity>emptySet();
                    }
                    return defaultValue(method.getReturnType());
                });

        GenerationOptions options = new GenerationOptions(null);

        ApplicationResolver resolver = new ApplicationResolver(options, resource, dependencyResolver);

        assertFalse(resolver.resolve());
        assertNull(resolver.getApplication());
        assertNull(resolver.getApplicationServer());
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0.0f;
        }
        if (type == double.class) {
            return 0.0d;
        }
        return null;
    }
}
