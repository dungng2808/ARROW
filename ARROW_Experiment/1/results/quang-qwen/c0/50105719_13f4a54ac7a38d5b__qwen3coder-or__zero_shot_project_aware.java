package enkan.component;

import enkan.exception.MisconfigurationException;
import enkan.system.inject.ComponentInjector;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class ComponentRelationship_ArrowTest {

    @Test
    public void testComponentBuilder() {
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1", "dep2");
        assertThat(relationship).isNotNull();
        assertThat(relationship.getClass().getSimpleName()).isEqualTo("ComponentRelationship");
    }

    @Test
    public void testInject_Success() {
        // Setup
        Map<String, SystemComponent> components = new HashMap<>();
        SystemComponent targetComponent = Mockito.mock(SystemComponent.class);
        Map<String, SystemComponent> dependencies = new HashMap<>();
        when(targetComponent.getAllDependencies()).thenReturn(dependencies);
        components.put("target", targetComponent);
        SystemComponent depComponent = Mockito.mock(SystemComponent.class);
        components.put("dep1", depComponent);

        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1");

        // Execute
        relationship.inject(components);

        // Verify
        verify(targetComponent).setDependencies(anyMap());
        verify(new ComponentInjector(anyMap())).inject(targetComponent);
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
    public void testInject_DepNotFound() {
        Map<String, SystemComponent> components = new HashMap<>();
        SystemComponent targetComponent = Mockito.mock(SystemComponent.class);
        components.put("target", targetComponent);

        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1");

        assertThatThrownBy(() -> relationship.inject(components))
                .isInstanceOf(MisconfigurationException.class)
                .hasMessageContaining("core.COMPONENT_NOT_FOUND");
    }

    @Test
    public void testSort_NormalCase() {
        LinkedList<String> componentsOrder = new LinkedList<>(Arrays.asList("A", "B", "C"));
        ComponentRelationship relationship = ComponentRelationship.component("C").using("A");

        relationship.sort(componentsOrder);

        assertThat(componentsOrder).containsExactly("A", "B", "C");
    }

    @Test
    public void testSort_NoChangeNeeded() {
        LinkedList<String> componentsOrder = new LinkedList<>(Arrays.asList("A", "B", "C"));
        ComponentRelationship relationship = ComponentRelationship.component("B").using("A");

        relationship.sort(componentsOrder);

        assertThat(componentsOrder).containsExactly("A", "B", "C");
    }

    @Test
    public void testSort_WithMultipleDeps() {
        LinkedList<String> componentsOrder = new LinkedList<>(Arrays.asList("A", "B", "C", "D"));
        ComponentRelationship relationship = ComponentRelationship.component("D").using("A", "B");

        relationship.sort(componentsOrder);

        assertThat(componentsOrder).containsExactly("A", "B", "C", "D");
    }

    @Test
    public void testIndex_Found() {
        List<String> list = Arrays.asList("A", "B", "C");
        ComponentRelationship relationship = new ComponentRelationship("target", new ArrayList<>());

        int result = relationship.index(list, "B");

        assertThat(result).isEqualTo(1);
    }

    @Test
    public void testIndex_NotFound() {
        List<String> list = Arrays.asList("A", "B", "C");
        ComponentRelationship relationship = new ComponentRelationship("target", new ArrayList<>());

        int result = relationship.index(list, "D");

        assertThat(result).isEqualTo(-1);
    }
}
