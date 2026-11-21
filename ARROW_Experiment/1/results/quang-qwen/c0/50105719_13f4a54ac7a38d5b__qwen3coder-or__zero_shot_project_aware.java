package enkan.component;

import enkan.exception.MisconfigurationException;
import enkan.system.inject.ComponentInjector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ComponentRelationship_ArrowTest {

    @Mock
    private SystemComponent targetComponent;

    @Mock
    private SystemComponent dependencyComponent;

    @Test
    public void testComponentBuilder() {
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1", "dep2");
        assertThat(relationship).isNotNull();
    }

    @Test
    public void testInject_Success() {
        Map<String, SystemComponent> components = new HashMap<>();
        components.put("target", targetComponent);
        components.put("dep1", dependencyComponent);

        Map<String, SystemComponent> dependencies = new HashMap<>();
        when(targetComponent.getAllDependencies()).thenReturn(dependencies);
        doNothing().when(new ComponentInjector(dependencies)).inject(targetComponent);

        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1");
        relationship.inject(components);

        verify(targetComponent).getAllDependencies();
        verify(targetComponent).setDependencies(anyMap());
        verify(new ComponentInjector(dependencies), times(1)).inject(targetComponent);
    }

    @Test
    public void testInject_TargetNotFound() {
        Map<String, SystemComponent> components = new HashMap<>();

        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1");
        assertThatThrownBy(() -> relationship.inject(components))
                .isInstanceOf(MisconfigurationException.class)
                .hasMessageContaining("core.COMPONENT_NOT_FOUND");
    }

    @Test
    public void testInject_DependencyNotFound() {
        Map<String, SystemComponent> components = new HashMap<>();
        components.put("target", targetComponent);

        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1");
        assertThatThrownBy(() -> relationship.inject(components))
                .isInstanceOf(MisconfigurationException.class)
                .hasMessageContaining("core.COMPONENT_NOT_FOUND");
    }

    @Test
    public void testSort_NoReorderingNeeded() {
        LinkedList<String> componentsOrder = new LinkedList<>(Arrays.asList("dep1", "target"));
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1");

        relationship.sort(componentsOrder);
        assertThat(componentsOrder).containsExactly("dep1", "target");
    }

    @Test
    public void testSort_ReorderingNeeded() {
        LinkedList<String> componentsOrder = new LinkedList<>(Arrays.asList("target", "dep1"));
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1");

        relationship.sort(componentsOrder);
        assertThat(componentsOrder).containsExactly("dep1", "target");
    }

    @Test
    public void testSort_MultipleDependencies() {
        LinkedList<String> componentsOrder = new LinkedList<>(Arrays.asList("target", "dep1", "dep2"));
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1", "dep2");

        relationship.sort(componentsOrder);
        assertThat(componentsOrder).containsExactly("dep1", "dep2", "target");
    }

    @Test
    public void testIndex_Found() {
        List<String> list = Arrays.asList("a", "b", "c");
        ComponentRelationship relationship = new ComponentRelationship("target", new ArrayList<>());
        int result = relationship.index(list, "b");
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void testIndex_NotFound() {
        List<String> list = Arrays.asList("a", "b", "c");
        ComponentRelationship relationship = new ComponentRelationship("target", new ArrayList<>());
        int result = relationship.index(list, "d");
        assertThat(result).isEqualTo(-1);
    }
}
