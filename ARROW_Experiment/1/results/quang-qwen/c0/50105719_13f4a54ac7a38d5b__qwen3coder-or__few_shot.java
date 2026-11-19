package enkan.component;

import enkan.exception.MisconfigurationException;
import enkan.system.inject.ComponentInjector;
import enkan.system.inject.SystemComponent;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ComponentRelationship_ArrowTest {

    @Test
    public void testComponentBuilder() {
        ComponentRelationship.ComponentRelationshipBuilder builder = ComponentRelationship.component("target");
        ComponentRelationship relationship = builder.using("dep1", "dep2");
        
        assertNotNull(relationship);
    }

    @Test
    public void testInjectSuccess() {
        // Create mock components
        SystemComponent targetComponent = mock(SystemComponent.class);
        Map<String, SystemComponent> dependencies = new HashMap<>();
        dependencies.put("dep1", mock(SystemComponent.class));
        when(targetComponent.getAllDependencies()).thenReturn(dependencies);
        
        Map<String, SystemComponent> components = new HashMap<>();
        components.put("target", targetComponent);
        components.put("dep1", dependencies.get("dep1"));
        
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1");
        
        // This should not throw an exception
        relationship.inject(components);
        
        // Verify that setDependencies was called
        verify(targetComponent).setDependencies(anyMap());
    }

    @Test(expected = MisconfigurationException.class)
    public void testInjectTargetNotFound() {
        Map<String, SystemComponent> components = new HashMap<>();
        ComponentRelationship relationship = ComponentRelationship.component("nonexistent").using("dep1");
        relationship.inject(components);
    }

    @Test(expected = MisconfigurationException.class)
    public void testInjectDependentNotFound() {
        SystemComponent targetComponent = mock(SystemComponent.class);
        Map<String, SystemComponent> components = new HashMap<>();
        components.put("target", targetComponent);
        
        ComponentRelationship relationship = ComponentRelationship.component("target").using("nonexistent");
        relationship.inject(components);
    }

    @Test
    public void testSortNoReorderingNeeded() {
        LinkedList<String> componentsOrder = new LinkedList<>(Arrays.asList("dep1", "target"));
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1");
        
        relationship.sort(componentsOrder);
        
        assertEquals(Arrays.asList("dep1", "target"), componentsOrder);
    }

    @Test
    public void testSortReorderingNeeded() {
        LinkedList<String> componentsOrder = new LinkedList<>(Arrays.asList("target", "dep1"));
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1");
        
        relationship.sort(componentsOrder);
        
        assertEquals(Arrays.asList("dep1", "target"), componentsOrder);
    }

    @Test
    public void testIndexFound() {
        List<String> list = Arrays.asList("a", "b", "c");
        ComponentRelationship relationship = new ComponentRelationship("target", new ArrayList<>());
        
        int result = relationship.index(list, "b");
        assertEquals(1, result);
    }

    @Test
    public void testIndexNotFound() {
        List<String> list = Arrays.asList("a", "b", "c");
        ComponentRelationship relationship = new ComponentRelationship("target", new ArrayList<>());
        
        int result = relationship.index(list, "d");
        assertEquals(-1, result);
    }
}
