package enkan.component;

import enkan.exception.MisconfigurationException;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class ComponentRelationship_ArrowTest {

    @Test
    public void testComponentBuilder() {
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1", "dep2");
        assertNotNull(relationship);
    }

    @Test
    public void testInjectSuccess() {
        // Create mock components
        SystemComponent targetComponent = new SystemComponent() {
            private Map<String, SystemComponent> dependencies = new HashMap<>();
            
            @Override
            public Map<String, SystemComponent> getAllDependencies() {
                return dependencies;
            }
            
            @Override
            public void setDependencies(Map<String, SystemComponent> dependencies) {
                this.dependencies = dependencies;
            }
            
            @Override
            public void inject() {
                // Mock implementation
            }
        };
        
        SystemComponent depComponent = new SystemComponent() {
            private Map<String, SystemComponent> dependencies = new HashMap<>();
            
            @Override
            public Map<String, SystemComponent> getAllDependencies() {
                return dependencies;
            }
            
            @Override
            public void setDependencies(Map<String, SystemComponent> dependencies) {
                this.dependencies = dependencies;
            }
            
            @Override
            public void inject() {
                // Mock implementation
            }
        };
        
        Map<String, SystemComponent> components = new HashMap<>();
        components.put("target", targetComponent);
        components.put("dep1", depComponent);
        
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1");
        relationship.inject(components);
    }

    @Test(expected = MisconfigurationException.class)
    public void testInjectTargetNotFound() {
        Map<String, SystemComponent> components = new HashMap<>();
        ComponentRelationship relationship = ComponentRelationship.component("nonexistent").using("dep1");
        relationship.inject(components);
    }

    @Test(expected = MisconfigurationException.class)
    public void testInjectDependentNotFound() {
        SystemComponent targetComponent = new SystemComponent() {
            private Map<String, SystemComponent> dependencies = new HashMap<>();
            
            @Override
            public Map<String, SystemComponent> getAllDependencies() {
                return dependencies;
            }
            
            @Override
            public void setDependencies(Map<String, SystemComponent> dependencies) {
                this.dependencies = dependencies;
            }
            
            @Override
            public void inject() {
                // Mock implementation
            }
        };
        
        Map<String, SystemComponent> components = new HashMap<>();
        components.put("target", targetComponent);
        
        ComponentRelationship relationship = ComponentRelationship.component("target").using("nonexistent");
        relationship.inject(components);
    }

    @Test
    public void testSort() {
        LinkedList<String> componentsOrder = new LinkedList<>(Arrays.asList("A", "B", "C"));
        ComponentRelationship relationship = ComponentRelationship.component("C").using("A");
        relationship.sort(componentsOrder);
        assertEquals(Arrays.asList("A", "C", "B"), componentsOrder);
    }

    @Test
    public void testSortNoChange() {
        LinkedList<String> componentsOrder = new LinkedList<>(Arrays.asList("A", "B", "C"));
        ComponentRelationship relationship = ComponentRelationship.component("B").using("A");
        relationship.sort(componentsOrder);
        assertEquals(Arrays.asList("A", "B", "C"), componentsOrder);
    }

    @Test
    public void testIndexFound() {
        List<String> list = Arrays.asList("a", "b", "c");
        ComponentRelationship relationship = ComponentRelationship.component("target").using();
        int result = relationship.index(list, "b");
        assertEquals(1, result);
    }

    @Test
    public void testIndexNotFound() {
        List<String> list = Arrays.asList("a", "b", "c");
        ComponentRelationship relationship = ComponentRelationship.component("target").using();
        int result = relationship.index(list, "d");
        assertEquals(-1, result);
    }
}
